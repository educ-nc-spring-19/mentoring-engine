package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.StudentStatusBind;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.enums.StudentStatus;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.service.repo.GroupRepository;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final StageService stageService;
    private final MasterDataClient masterDataClient;
    private final PoolService poolService;
    private final CauldronService cauldronService;
    private final UserService userService;

    public Optional<Group> findByMentorId(UUID mentorId) {
        Optional<Group> optionalGroup = groupRepository.findByMentorId(mentorId);
        optionalGroup.ifPresent(group ->
                log.log(Level.DEBUG,"Group(id=" + group.getId() + ") found by Mentor(id=" + mentorId + ")"));
        return optionalGroup;
    }

    public Optional<Group> findById(UUID id) {
        Optional<Group> optionalGroup = groupRepository.findById(id);
        optionalGroup.ifPresent(group ->
                log.log(Level.DEBUG, "Group(id=" + group.getId() + ") found by id"));
        return optionalGroup;
    }

    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAll().forEach(groups::add);

        log.log(Level.DEBUG,
                "Groups found by findAll(): " + groups.stream().map(Group::getId).collect(Collectors.toList()));
        return groups;
    }

    public List<Group> findAllById(Iterable<UUID> ids) {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAllById(ids).forEach(groups::add);

        log.log(Level.DEBUG,
                "Groups found by findAllById(): " + groups.stream().map(Group::getId).collect(Collectors.toList()));
        return groups;
    }

    public Group create(String name) throws IllegalArgumentException, NoSuchElementException {
        MentorDTO currentMentorDTO = getCurrentMentorDTO();
        UUID mentorId = currentMentorDTO.getId();

        Optional<Group> optionalGroup = findByMentorId(mentorId);
        if (optionalGroup.isPresent()) {
            throw new IllegalArgumentException("Mentor(id=" + mentorId
                    + ") already own Group(id=" + optionalGroup.get().getId() + ")");
        }

        Group group = new Group();
        group.setName(name);
        group.setMentorId(mentorId);

        final UUID FIRST_STAGE_ID = UUID.fromString("390748bf-2b6a-4b4e-93c5-51f431eae1db");
        Optional<Stage> stage = stageService.findById(FIRST_STAGE_ID);
        if (stage.isPresent()) {
            group.setStage(stage.get());
            group.setStageId(FIRST_STAGE_ID);
        } else {
            throw new IllegalArgumentException("Can't create new group cause illegal UUID in 'FIRST_STAGE_ID': '"
                    + FIRST_STAGE_ID.toString() + "'");
        }

        group = groupRepository.save(group);
        log.log(Level.INFO, "Group(id=" + group.getId() + ") created for Mentor(id=" + mentorId + ")");
        return group;
    }

    public void delete(UUID id) {
        groupRepository.deleteById(id);
        log.log(Level.INFO, "Group(id=" + id + ") deleted");
    }

    public void deleteAll() {
        groupRepository.deleteAll();
    }

    private MentorDTO getCurrentMentorDTO() throws NoSuchElementException {
        UUID currentUserId = userService.getCurrentUserId();
        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(currentUserId);

        if (currentMentorDTO == null) {
            log.log(Level.WARN, "Can't find Mentor by user(id=" + currentUserId.toString() + ")");
            throw new NoSuchElementException("Can't find Mentor by user(id=" + currentUserId.toString() + ")");
        }

        return currentMentorDTO;
    }

    // TO DO (??): Check in Master Data for backup existence
    public Group setBackupId(UUID backupId) throws IllegalArgumentException, NoSuchElementException {
        MentorDTO currentMentorDTO = getCurrentMentorDTO();
        UUID mentorId = currentMentorDTO.getId();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(mentorId);

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            group.setBackupId(backupId);
            group = groupRepository.save(group);

            log.log(Level.INFO, "Mentor(id=" + backupId + ") was set as backup for Group(id=" + group.getId() + ")");
            return group;
        } else {
            throw new IllegalArgumentException("Group for Mentor(id=" + mentorId + ") doesn't exist");
        }
    }

    public List<Group> saveAll(Iterable<Group> groups) {
        return IterableUtils.toList(groupRepository.saveAll(groups));
    }

    public Group addStudentId(UUID studentId) throws IllegalArgumentException, NoSuchElementException {

        MentorDTO currentMentorDTO = getCurrentMentorDTO();
        UUID mentorId = currentMentorDTO.getId();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(mentorId);

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            if (CollectionUtils.isNotEmpty(group.getStudents())
                    && group.getStudents().stream().map(StudentStatusBind::getId).anyMatch(id -> id.equals(studentId))) {
                throw new IllegalArgumentException("Student(id=" + studentId + ") already in group id=" + group.getId());
            }

            StudentDTO studentDTO = masterDataClient.getStudentById(studentId);
            if (studentDTO == null) {
                throw new IllegalArgumentException("Can't get Student(id=" + studentId + ") from master-data service.");
            }

            UUID studentDirectionId = studentDTO.getDirectionId();
            if (studentDirectionId == null) {
                throw new NoSuchElementException("Student(id=" + studentId + ") direction 'null' is null");
            }

            // delete student from pool, if it present
            poolService.findByDirectionIdAndStudentsIs(studentDirectionId, studentId).ifPresent(pool -> {
                pool.getStudents().remove(studentId);
                Pool savedPool = poolService.save(pool);
                log.log(Level.INFO, "Student(id=" + studentId
                        + ") removed from Pool(id=" + savedPool.getId().toString() + ")");
            });

            // delete student from current mentor cauldron, if it present
            cauldronService.findByMentorsIsAndStudentsIs(mentorId, studentId).ifPresent(cauldron -> {
                cauldron.getStudents().remove(studentId);
                Cauldron savedCauldron = cauldronService.save(cauldron);
                log.log(Level.INFO, "Student(id=" + studentId
                        + ") removed from Cauldron(id=" + savedCauldron.getId().toString() + ")");
            });

            // Add student to group with SELECTED status
            group.getStudents().add(new StudentStatusBind(studentId, StudentStatus.SELECTED));

            group = groupRepository.save(group);
            log.log(Level.INFO, "Student(id=" + studentId + ") added to Group(id=" + group.getId() + ")");
            return group;
        } else {
            throw new NoSuchElementException("Group for Mentor(id=" + mentorId.toString() + ") doesn't exist");
        }
    }
}
