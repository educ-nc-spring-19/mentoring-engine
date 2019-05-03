package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.StudentStatusBind;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.enums.StudentStatus;
import com.educ_nc_spring_19.mentoring_engine.enums.InviteState;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.util.Encryption;
import com.educ_nc_spring_19.mentoring_engine.util.InviteLinkPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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

    private static final Integer MESSAGE_ARGS_QUANTITY = 5;
    private static final String ENCRYPTION_KEY = "ASWz8Eie5TN3aQsrITKQo9Dv8uyjhtzl";
    private static final String APP_BASE_URL = "http://localhost:55010";
    private static final String INVITE_PATH = "/mentoring-engine/rest/api/v1/rpc/invite?link=";

    private final GroupService groupService;

    @SuppressWarnings("Duplicates")
    public Map<UUID, InviteLinkPair> getInviteLinks(Group group) throws IllegalArgumentException {
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
                            InviteService.getLinkMessage(InviteState.ACCEPT, studentId, group.getId()));
                    String encryptedRejectMessage = Encryption.encrypt(ENCRYPTION_KEY,
                            InviteService.getLinkMessage(InviteState.REJECT, studentId, group.getId()));

                    studentIdLinks.put(
                            studentId,
                            new InviteLinkPair(
                                    new URL(APP_BASE_URL
                                            + INVITE_PATH
                                            + URLEncoder.encode(encryptedAcceptMessage, "UTF-8")),
                                    new URL(APP_BASE_URL
                                            + INVITE_PATH
                                            + URLEncoder.encode(encryptedRejectMessage, "UTF-8"))
                            )
                    );

                } catch (NoSuchAlgorithmException
                        | NoSuchPaddingException
                        | InvalidKeyException
                        | IllegalBlockSizeException
                        | BadPaddingException
                        | UnsupportedEncodingException
                        | MalformedURLException e) {
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

    @SuppressWarnings("Duplicates")
    public InviteLinkPair forceInviteLinks(UUID studentId, Group group)
            throws IllegalArgumentException,
            IllegalStateException,
            NoSuchElementException {
        if (group == null) {
            String errorMessage = "Group is 'null'";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (studentId == null) {
            String errorMessage = "studentId is 'null'";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        StudentStatusBind student = group.getStudents().stream()
                .filter(studentStatusBind -> studentStatusBind.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = "Can't find Student(id=" + studentId
                            + ") in Group(id=" + group.getId() + ")";
                    log.log(Level.WARN, errorMessage);
                    throw new NoSuchElementException(errorMessage);
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
                    InviteService.getLinkMessage(InviteState.ACCEPT, studentId, group.getId()));
            String encryptedRejectMessage = Encryption.encrypt(ENCRYPTION_KEY,
                    InviteService.getLinkMessage(InviteState.REJECT, studentId, group.getId()));

            inviteLinkPair = new InviteLinkPair(
                    new URL(APP_BASE_URL
                            + INVITE_PATH
                            + URLEncoder.encode(encryptedAcceptMessage, "UTF-8")),
                    new URL(APP_BASE_URL
                            + INVITE_PATH
                            + URLEncoder.encode(encryptedRejectMessage, "UTF-8"))
            );

            // you must save updated group in calling method
            if (!student.getStatus().equals(StudentStatus.INVITED)) {
                log.log(Level.INFO, "Set Student(id=" + studentId + ") status to '" + StudentStatus.INVITED + "'");
                student.setStatus(StudentStatus.INVITED);
            }
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException
                | UnsupportedEncodingException
                | MalformedURLException e) {
            log.log(Level.WARN, "Can't create invite links for Student(id=" + studentId
                    + ") in cause of exception: " + e.getMessage());
            // fucking crutch, but it maybe better, than throwing new Exception(e)
            inviteLinkPair = new InviteLinkPair(null, null);
        }

        return inviteLinkPair;
    }

    public InviteState processInviteLink(String link) throws Exception {
        if (StringUtils.isBlank(link)) {
            String errorMessage = "Argument string is blank";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        InviteState resultState;

        try {
            String decodedMessage = Encryption.decrypt(ENCRYPTION_KEY, link);

            List<String> splittedMessage = Arrays.asList(decodedMessage.split("\\|"));
            if (splittedMessage.size() < MESSAGE_ARGS_QUANTITY) {
                String errorMessage = "splittedMessage args quantity less than " + MESSAGE_ARGS_QUANTITY;
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            InviteState state = InviteState.valueOf(splittedMessage.get(0));
            UUID studentId = UUID.fromString(splittedMessage.get(1));
            UUID groupId = UUID.fromString(splittedMessage.get(2));

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
                            throw new NoSuchElementException(errorMessage);
                        });

                if (currentStudent.getStatus().equals(StudentStatus.EXPELLED)) {
                    log.log(Level.WARN, "Student(id=" + studentId
                            + ") can't do anything with the invitation in cause of his status '" + StudentStatus.EXPELLED + "'");
                    throw new IllegalStateException("You can't do anything with the invitation");
                } else if (currentStudent.getStatus().equals(StudentStatus.ACCEPTED) && state.equals(InviteState.ACCEPT)) {
                    log.log(Level.WARN, "Student(id=" + studentId
                            + ") has already accepted the invitation in cause of his status '" + StudentStatus.ACCEPTED + "'");
                    throw new IllegalStateException("You have already accepted the invitation");
                } else if (currentStudent.getStatus().equals(StudentStatus.REJECTED) && state.equals(InviteState.REJECT)) {
                    log.log(Level.WARN, "Student(id=" + studentId
                            + ") has already rejected the invitation in cause of his status '" + StudentStatus.REJECTED + "'");
                    throw new IllegalStateException("You have already rejected the invitation");
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
                | BadPaddingException e) {
            String errorMessage = "Can't processing decryption of link=" + link;
            log.log(Level.WARN, errorMessage);
            throw new Exception(errorMessage, e);
        }
        return resultState;
    }

    private static String getLinkMessage(InviteState inviteState, UUID studentId, UUID groupId) {

        return inviteState + "|" +
                studentId + "|" +
                groupId + "|" +
                OffsetDateTime.now() + "|" +
                UUID.randomUUID();
    }
}
