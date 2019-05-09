package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.StudentStatusBind;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.enums.StudentStatus;
import com.educ_nc_spring_19.mentoring_engine.client.MailingServiceClient;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.enums.InviteState;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.util.Encryption;
import com.educ_nc_spring_19.mentoring_engine.util.InviteLinkPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class InviteService {

    private static final String ENCRYPTION_KEY = "ASWz8Eie5TN3aQsrITKQo9Dv8uyjhtzl";
    private static final Integer MESSAGE_ARGS_QUANTITY = 5;
    private static final String APP_BASE_URL = "http://localhost:55010";
    private static final String INVITE_PATH = "/mentoring-engine/rest/api/v1/rpc/invite?link=";

    private final GroupService groupService;
    private final MailingServiceClient mailingServiceClient;
    private final MasterDataClient masterDataClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @SuppressWarnings("Duplicates")
    public InviteLinkPair forceInviteLinks(UUID studentId)
            throws IllegalArgumentException,
            IllegalStateException,
            NoSuchElementException {

        if (studentId == null) {
            String errorMessage = "studentId is 'null'";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        Optional<Group> optionalGroup = groupService.findByStudentsIs(studentId);

        if (!optionalGroup.isPresent()) {
            String errorMessage = "Group isn't present for Student(id=" + studentId + ")";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Group group = optionalGroup.get();

        StudentStatusBind student = group.getStudents().stream()
                .filter(studentStatusBind -> studentStatusBind.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = "Can't find Student(id=" + studentId
                            + ") in Group(id=" + group.getId() + ")";
                    log.log(Level.WARN, errorMessage);
                    return new NoSuchElementException(errorMessage);
                });

        if (student.getStatus().equals(StudentStatus.ACCEPTED) || student.getStatus().equals(StudentStatus.EXPELLED)) {
            String errorMessage = "Student(id=" + studentId
                    + ") in Group(id=" + group.getId()
                    + ") has illegal status '" + student.getStatus() + "'";
            log.log(Level.WARN, errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        InviteLinkPair inviteLinkPair;
        try {
            String encryptedAcceptMessage = Encryption.encrypt(ENCRYPTION_KEY,
                    this.getLinkMessage(InviteState.ACCEPT, studentId, group.getId()));
            String encryptedRejectMessage = Encryption.encrypt(ENCRYPTION_KEY,
                    this.getLinkMessage(InviteState.REJECT, studentId, group.getId()));

            inviteLinkPair = new InviteLinkPair(
                    new URL(APP_BASE_URL
                            + INVITE_PATH
                            + URLEncoder.encode(encryptedAcceptMessage, "UTF-8")),
                    new URL(APP_BASE_URL
                            + INVITE_PATH
                            + URLEncoder.encode(encryptedRejectMessage, "UTF-8"))
            );

            if (!student.getStatus().equals(StudentStatus.INVITED)) {
                log.log(Level.INFO, "Set Student(id=" + studentId + ") status to '" + StudentStatus.INVITED + "'");
                student.setStatus(StudentStatus.INVITED);
                Group updatedGroup = groupService.save(group);
                log.log(Level.INFO, "Group(id=" + updatedGroup.getId() + ") saved");
            }
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException
                | UnsupportedEncodingException
                | MalformedURLException
                | JsonProcessingException e) {
            log.log(Level.WARN, "Can't create invite links for Student(id=" + studentId
                    + ") in cause of exception: " + e.getMessage());
            // fucking crutch, but it maybe better, than throwing new Exception(e)
            inviteLinkPair = new InviteLinkPair(null, null);
        }

        return inviteLinkPair;
    }

    public Map<UUID, InviteLinkPair> getGroupInviteLinks() throws IllegalArgumentException, NoSuchElementException {

        UUID currentUserId = userService.getCurrentUserId();
        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(currentUserId);

        if (currentMentorDTO == null) {
            log.log(Level.WARN, "Can't find Mentor by User(id=" + currentUserId + ")");
            throw new NoSuchElementException("Can't find Mentor by User(id=" + currentUserId + ")");
        }

        Optional<Group> optionalGroup = groupService.findByMentorId(currentMentorDTO.getId());
        if (!optionalGroup.isPresent()) {
            String errorMessage = "Can't find group for Mentor(id=" + currentMentorDTO.getId() + ")";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Group group = optionalGroup.get();
        Map<UUID, InviteLinkPair> result = getGroupInviteLinks(group);
        if (MapUtils.isNotEmpty(result)) {
            // save after students status update
            group = groupService.save(group);
            log.log(Level.INFO, "Group(id=" + group.getId() + ") saved");
        } else {
            log.log(Level.INFO, "There are no students with updated statuses. Returning empty map");
        }

        return result;
    }

    public Map<UUID, InviteLinkPair> getGroupInviteLinks(Group group) throws IllegalArgumentException {

        if (group == null) {
            String errorMessage = "Group is 'null'";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        List<StudentStatusBind> students = group.getStudents();
        if (CollectionUtils.isEmpty(students)) {
            String errorMessage = "Group(id=" + group.getId() + ") students is empty";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Map<UUID, InviteLinkPair> studentIdLinks = new HashMap<>(students.size());

        for (StudentStatusBind student : students) {
            if (student.getStatus().equals(StudentStatus.SELECTED)) {
                UUID studentId = student.getId();

                try {
                    String encryptedAcceptMessage = Encryption.encrypt(ENCRYPTION_KEY,
                            this.getLinkMessage(InviteState.ACCEPT, studentId, group.getId()));
                    String encryptedRejectMessage = Encryption.encrypt(ENCRYPTION_KEY,
                            this.getLinkMessage(InviteState.REJECT, studentId, group.getId()));

                    InviteLinkPair inviteLinkPair = new InviteLinkPair(
                            new URL(APP_BASE_URL
                                    + INVITE_PATH
                                    + URLEncoder.encode(encryptedAcceptMessage, "UTF-8")),
                            new URL(APP_BASE_URL
                                    + INVITE_PATH
                                    + URLEncoder.encode(encryptedRejectMessage, "UTF-8"))
                    );

                    studentIdLinks.put(studentId, inviteLinkPair);

                    mailingServiceClient.sendInviteEmail(studentId, group.getMentorId(), group.getBackupId(), inviteLinkPair);

                    log.log(Level.INFO, "Create links for Student(id=" + studentId + ")");

                } catch (NoSuchAlgorithmException
                        | NoSuchPaddingException
                        | InvalidKeyException
                        | IllegalBlockSizeException
                        | BadPaddingException
                        | UnsupportedEncodingException
                        | MalformedURLException
                        | JsonProcessingException e) {
                    log.log(Level.WARN, "Can't create invite links for Student(id=" + studentId
                            + ") in cause of exception: " + e.getMessage());
                    continue;
                }

                student.setStatus(StudentStatus.INVITED);
                log.log(Level.INFO, "Set Student(id=" + studentId + ") status to '" + StudentStatus.INVITED + "'");
            }
        }

        return studentIdLinks;
    }

    private String getLinkMessage(InviteState inviteState, UUID studentId, UUID groupId) throws JsonProcessingException {

        ObjectNode messageNode = objectMapper.getNodeFactory().objectNode();
        messageNode.put("state", inviteState.name());
        messageNode.put("studentId", studentId.toString());
        messageNode.put("groupId", groupId.toString());
        messageNode.put("send", OffsetDateTime.now().toString());
        messageNode.put("salt", UUID.randomUUID().toString());

        return objectMapper.writeValueAsString(messageNode);
    }

    @SuppressWarnings("unchecked")
    public InviteState processInviteLink(String link) throws Exception {
        if (StringUtils.isBlank(link)) {
            String errorMessage = "Argument string is blank";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        InviteState resultState;
        try {
            String decodedMessage = Encryption.decrypt(ENCRYPTION_KEY, link);

            Map<String, String> jsonMap = objectMapper.readValue(decodedMessage, Map.class);
            if (jsonMap.size() < MESSAGE_ARGS_QUANTITY) {
                String errorMessage = "jsonMap args quantity less than " + MESSAGE_ARGS_QUANTITY;
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            InviteState state = InviteState.valueOf(jsonMap.get("state"));
            UUID studentId = UUID.fromString(jsonMap.get("studentId"));
            UUID groupId = UUID.fromString(jsonMap.get("groupId"));

            Optional<Group> optionalGroup = groupService.findById(groupId);
            if (optionalGroup.isPresent()) {
                Group group = optionalGroup.get();
                List<StudentStatusBind> students = group.getStudents();
                if (CollectionUtils.isEmpty(students)) {
                    String errorMessage = "Group(id=" + groupId + ") students is empty";
                    log.log(Level.WARN, errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }

                StudentStatusBind currentStudent = students.stream()
                        .filter(student -> student.getId().equals(studentId))
                        .findAny()
                        .orElseThrow(() -> {
                            String errorMessage = "Student(id=" + studentId
                                    + ") doesn't belong to Group(id=" + groupId + ")";
                            log.log(Level.WARN, errorMessage);
                            return new NoSuchElementException(errorMessage);
                        });

                if (currentStudent.getStatus().equals(StudentStatus.EXPELLED)) {
                    String errorMessage = "Student(id=" + studentId
                            + ") can't do anything with the invitation in cause of his status '" + StudentStatus.EXPELLED + "'";
                    log.log(Level.WARN, errorMessage);
                    throw new IllegalStateException(errorMessage);
                } else if (currentStudent.getStatus().equals(StudentStatus.ACCEPTED) && state.equals(InviteState.ACCEPT)) {
                    String errorMessage = "Student(id=" + studentId
                            + ") has already accepted the invitation in cause of his status '" + StudentStatus.ACCEPTED + "'";
                    log.log(Level.WARN, errorMessage);
                    throw new IllegalStateException(errorMessage);
                } else if (currentStudent.getStatus().equals(StudentStatus.REJECTED) && state.equals(InviteState.REJECT)) {
                    String errorMessage = "Student(id=" + studentId
                            + ") has already rejected the invitation in cause of his status '" + StudentStatus.REJECTED + "'";
                    log.log(Level.WARN, errorMessage);
                    throw new IllegalStateException(errorMessage);
                } else {
                    switch (state) {
                        case ACCEPT:
                            currentStudent.setStatus(StudentStatus.ACCEPTED);
                            log.log(Level.INFO, "Set Student(id=" + studentId + ") status to '" + StudentStatus.ACCEPTED + "'");
                            break;
                        case REJECT:
                            currentStudent.setStatus(StudentStatus.REJECTED);
                            log.log(Level.INFO, "Set Student(id=" + studentId + ") status to '" + StudentStatus.REJECTED + "'");
                            break;
                        default:
                            log.log(Level.WARN, "Illegal InviteState '" + state + "'");
                            throw new IllegalArgumentException("Illegal InviteState '" + state + "'");
                    }
                }

                // saving updated group
                groupService.save(group);
                resultState = state;
                log.log(Level.INFO, "Student(id="+ studentId + ") in Group(id=" + groupId
                        + ") updated with InviteState '" + resultState + "'");
            } else {
                String errorMessage = "Can't found Group(id=" + groupId + ")";
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException
                | IOException e) {
            String errorMessage = "Can't processing decryption of link=" + link;
            log.log(Level.WARN, errorMessage);
            throw new Exception(errorMessage, e);
        }
        return resultState;
    }
}
