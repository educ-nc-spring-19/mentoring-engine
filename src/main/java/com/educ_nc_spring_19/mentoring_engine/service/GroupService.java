package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.DayOfWeekTime;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.StudentStatusBind;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.enums.StudentStatus;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.enums.StageType;
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

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class GroupService {

    private final CauldronService cauldronService;
    private final GroupRepository groupRepository;
    private final MasterDataClient masterDataClient;
    private final PoolService poolService;
    private final StageService stageService;
    private final UserService userService;

    public Group addMeetingDayTime(DayOfWeek day, OffsetTime time)
            throws IllegalArgumentException, NoSuchElementException {
        if (day == null) {
            String errorMessage = "Provided day is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (time == null) {
            String errorMessage = "Provided time is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // get current mentor
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();

            Set<DayOfWeekTime> meetings = group.getMeetings();
            if (meetings.stream().map(DayOfWeekTime::getDay).anyMatch(day::equals)) {
                String errorMessage = "Provided day '" + day.name()
                        + "' is currently in Group(id" + group.getId() + ")'s meetings";
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            meetings.add(new DayOfWeekTime(day, time));
            // saving updated lecture
            group = groupRepository.save(group);
            log.log(Level.INFO, "DayOfWeekTime(day=" + day.name()
                    + ", time=" + time + ") added to Group(id=" + group.getId() + ")");
            return group;
        } else {
            throw new NoSuchElementException("Group for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist");
        }
    }

    public Group addStudentId(UUID studentId) throws IllegalArgumentException, NoSuchElementException {
        // get current mentor
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            if (CollectionUtils.isNotEmpty(group.getStudents())
                    && group.getStudents().stream().map(StudentStatusBind::getId).anyMatch(id -> id.equals(studentId))) {
                throw new IllegalArgumentException("Student(id=" + studentId
                        + ") already in Group(id=" + group.getId() + ")");
            }

            StudentDTO studentDTO = masterDataClient.getStudentById(studentId);
            if (studentDTO == null) {
                throw new IllegalArgumentException("Can't get Student(id=" + studentId + ") from master-data service.");
            } else if (studentDTO.getDirectionId() == null) {
                throw new NoSuchElementException("Student(id=" + studentId + ") direction is null");
            } else if (!studentDTO.getDirectionId().equals(currentMentorDTO.getDirectionId())) {
                throw new IllegalArgumentException("Can't add Student(id=" + studentId
                        + ") with Direction(id=" + studentDTO.getDirectionId()
                        + ") to Group(id=" + group.getId()
                        + ") with Direction(id=" + currentMentorDTO.getDirectionId());
            }

            // delete student from pool, if it present
            poolService.findByDirectionIdAndStudentsIs(studentDTO.getDirectionId(), studentId).ifPresent(pool -> {
                pool.getStudents().remove(studentId);
                Pool savedPool = poolService.save(pool);
                log.log(Level.INFO, "Student(id=" + studentId
                        + ") removed from Pool(id=" + savedPool.getId().toString() + ")");
            });

            // delete student from current mentor cauldron, if it present
            cauldronService.findByMentorsIsAndStudentsIs(currentMentorDTO.getId(), studentId).ifPresent(cauldron -> {
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
            throw new NoSuchElementException("Group for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist");
        }
    }

    public Group create(String name) throws IllegalArgumentException, NoSuchElementException {
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        UUID mentorId = currentMentorDTO.getId();

        Optional<Group> optionalGroup = findByMentorId(mentorId);
        if (optionalGroup.isPresent()) {
            throw new IllegalArgumentException("Mentor(id=" + mentorId
                    + ") already own Group(id=" + optionalGroup.get().getId() + ")");
        }

        Group group = new Group();
        group.setName(name);
        group.setMentorId(mentorId);

        Optional<Stage> stage = stageService.findByType(StageType.DISTRIBUTION);
        if (stage.isPresent()) {
            group.setStage(stage.get());
            group.setStageId(stage.get().getId());
        } else {
            throw new IllegalArgumentException("Can't create new group in cause of absent Stage(type="
                    + StageType.DISTRIBUTION.name() + ")");
        }

        group = groupRepository.save(group);
        log.log(Level.INFO, "Group(id=" + group.getId() + ") created for Mentor(id=" + mentorId + ")");
        return group;
    }

    public void deleteAll() {
        groupRepository.deleteAll();
        log.log(Level.INFO, "All Groups deleted");
    }

    public void deleteById(UUID id) throws NoSuchElementException {
        Optional<Group> optionalGroup = groupRepository.findById(id);
        if (!optionalGroup.isPresent()) {
            throw new NoSuchElementException("Group(id=" + id + ") doesn't exist");
        }

        MentorDTO mentorDTO = this.getCurrentMentorDTO();
        Group group = optionalGroup.get();
        if (mentorDTO.getDirectionId() == null) {
            throw new NoSuchElementException("Mentor(id=" + mentorDTO.getId() + ") direction is 'null'");
        } else if (CollectionUtils.isNotEmpty(group.getStudents())) {
            Optional<Pool> optionalPool = poolService.findByDirectionId(mentorDTO.getDirectionId());
            if (optionalPool.isPresent()) {
                Pool pool = optionalPool.get();

                Set<UUID> studentIdsToAdd = group.getStudents().stream()
                        .map(StudentStatusBind::getId)
                        .collect(Collectors.toSet());

                pool.getStudents().addAll(studentIdsToAdd);
                pool = poolService.save(pool);

                log.log(Level.INFO, "Pool(id=" + pool.getId() + ") updated with Students: " + studentIdsToAdd);
            } else {
                throw new NoSuchElementException("Pool for Direction(id=" + mentorDTO.getDirectionId() + ") is absent");
            }
        }

        groupRepository.delete(group);
        log.log(Level.INFO, "Group(id=" + id + ") deleted");
    }

    public Group deleteMeetingDay(DayOfWeek day) throws IllegalArgumentException, NoSuchElementException {
        if (day == null) {
            String errorMessage = "Provided day is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // get current mentor
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            Set<DayOfWeekTime> meetings = group.getMeetings();
            if (CollectionUtils.isEmpty(meetings) ||  meetings.stream().map(DayOfWeekTime::getDay).noneMatch(day::equals)) {
                String errorMessage = "Provided day '" + day.name()
                        + "' is absent in Group(id=" + group.getId() + ")'s meetings";
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            meetings.removeIf(meeting -> meeting.getDay().equals(day));

            // saving updated lecture
            group = groupRepository.save(group);
            log.log(Level.INFO, "DayOfWeek(day=" + day.name()
                    + ") removed from Group(id=" + group.getId() + ")");
            return group;
        } else {
            throw new NoSuchElementException("Group for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist");
        }
    }

    public List<Group> findAll() {
        List<Group> groups = IterableUtils.toList(groupRepository.findAll());

        log.log(Level.DEBUG,
                "Groups found by findAll(): " + groups.stream().map(Group::getId).collect(Collectors.toList()));
        return groups;
    }

    public List<Group> findAllById(Iterable<UUID> ids) {
        List<Group> groups = IterableUtils.toList(groupRepository.findAllById(ids));

        log.log(Level.DEBUG,
                "Groups found by findAllById(): " + groups.stream().map(Group::getId).collect(Collectors.toList()));
        return groups;
    }

    public List<Group> findAllByStageId(UUID stageId) {
        List<Group> groups = IterableUtils.toList(groupRepository.findAllByStageId(stageId));

        log.log(Level.DEBUG,
                "Groups found by findAllById(): " + groups.stream().map(Group::getId).collect(Collectors.toList()));
        return groups;
    }

    public Optional<Group> findById(UUID id) {
        Optional<Group> optionalGroup = groupRepository.findById(id);
        optionalGroup.ifPresent(group ->
                log.log(Level.DEBUG, "Group(id=" + group.getId() + ") found by id"));
        return optionalGroup;
    }

    public Optional<Group> findByMentorId(UUID mentorId) {
        Optional<Group> optionalGroup = groupRepository.findByMentorId(mentorId);
        optionalGroup.ifPresent(group ->
                log.log(Level.DEBUG,"Group(id=" + group.getId() + ") found by Mentor(id=" + mentorId + ")"));
        return optionalGroup;
    }

    public Optional<Group> findByStudentsIs(UUID studentId) {
        Optional<Group> optionalGroup = groupRepository.findByStudentsIs(studentId);
        optionalGroup.ifPresent(group ->
                log.log(Level.DEBUG,"Group(id=" + group.getId() + ") found by Student(id=" + studentId + ")"));
        return optionalGroup;
    }

    private MentorDTO getCurrentMentorDTO() throws NoSuchElementException {
        UUID currentUserId = userService.getCurrentUserId();
        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(currentUserId);

        if (currentMentorDTO == null) {
            log.log(Level.WARN, "Can't find Mentor by User(id=" + currentUserId + ")");
            throw new NoSuchElementException("Can't find Mentor by User(id=" + currentUserId + ")");
        }

        return currentMentorDTO;
    }

    public Group removeStudentId(UUID studentId) throws IllegalArgumentException, NoSuchElementException {
        // get current mentor
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();

            Set<StudentStatusBind> studentStatusBinds = group.getStudents();
            if (CollectionUtils.isEmpty(studentStatusBinds)
                    || studentStatusBinds.stream().map(StudentStatusBind::getId)
                    .noneMatch(id -> id.equals(studentId))) {
                throw new IllegalArgumentException("Student(id=" + studentId
                        + ") is absent in Group(id=" + group.getId() + ")");
            }

            StudentDTO studentDTO = masterDataClient.getStudentById(studentId);
            if (studentDTO == null) {
                throw new IllegalArgumentException("Can't get Student(id=" + studentId + ") from master-data service");
            } else if (studentDTO.getDirectionId() == null) {
                throw new NoSuchElementException("Student(id=" + studentId + ") direction is null");
            }

            Optional<Pool> optionalPool = poolService.findByDirectionId(studentDTO.getDirectionId());
            if (!optionalPool.isPresent()) {
                throw new NoSuchElementException("There is no Pool for Direction(id=" + studentDTO.getDirectionId() + ")");
            }

            // remove  student from group
            studentStatusBinds.remove(studentStatusBinds.stream()
                    .filter(bind -> studentId.equals(bind.getId()))
                    .findAny()
                    .orElseThrow(() ->
                            new IllegalArgumentException("Student(id=" + studentId.toString() + ") is absent in Group")
                    )
            );

            // add student to his direction pool
            Pool pool = optionalPool.get();
            pool.getStudents().add(studentId);

            // save data to DB
            group = save(group);
            pool = poolService.save(pool);

            log.log(Level.INFO, "Student(id=" + studentId
                    + ") removed from Group(id=" + group.getId()
                    + ") and added to Pool(id=" + pool.getId() + ")");

            return group;
        } else {
            throw new NoSuchElementException("Group for Mentor(id=" + currentMentorDTO.getId().toString() + ") doesn't exist");
        }
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public List<Group> saveAll(Iterable<Group> groups) {
        return IterableUtils.toList(groupRepository.saveAll(groups));
    }

    // TO DO (??): Check in Master Data for backup existence
    public Group setBackupId(UUID backupId) throws IllegalArgumentException, NoSuchElementException {
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            group.setBackupId(backupId);
            group = groupRepository.save(group);

            log.log(Level.INFO, "Mentor(id=" + backupId + ") was set as backup for Group(id=" + group.getId() + ")");
            return group;
        } else {
            throw new IllegalArgumentException("Group for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist");
        }
    }

    public Group setFirstMeetingDate(OffsetDateTime date)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException {
        if (date == null) {
            String errorMessage = "Provided date is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // get current mentor
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            Stage groupStage = group.getStage();

            // Setting of first meeting date is possible only for Stage types DISTRIBUTION and FIRST_MEETING
            if (groupStage.getType().equals(StageType.PROJECT_WORKS)
                    || groupStage.getType().equals(StageType.PROJECT_PROTECTION)) {
                String errorMessage = "Group(id=" + group.getId()
                        + ") has an illegal Stage(type=" + groupStage.getType().name()
                        + "). Setting of group first meeting date aborted";
                log.log(Level.WARN, errorMessage);
                throw new IllegalStateException(errorMessage);
            }

            Stage firstMeetingStage;
            if (!groupStage.getType().equals(StageType.FIRST_MEETING)) {
                Optional<Stage> optionalFirstMeetingStage = stageService.findByType(StageType.FIRST_MEETING);
                if (!optionalFirstMeetingStage.isPresent()) {
                    throw new IllegalArgumentException("Can't find Stage(type=" + StageType.DISTRIBUTION.name() + ")");
                }
                firstMeetingStage = optionalFirstMeetingStage.get();
                log.log(Level.INFO, "Group(id=" + group.getId() + ") has Stage(type=" + groupStage.getType().name()
                        + "). Set firstMeetingStage variable to Stage(type=" + firstMeetingStage.getType().name()
                        + ") found from database");
            } else {
                firstMeetingStage = groupStage;
                log.log(Level.INFO, "Group(id=" + group.getId() + ") has Stage(type=" + groupStage.getType().name()
                        + "). Set firstMeetingStage variable to Stage(type=" + firstMeetingStage.getType().name()
                        + ") from groupStage variable");
            }

            OffsetDateTime firstMeetingStageDeadline = firstMeetingStage.getDeadline();
            // Maybe throwing the exception in this case is rude, but who knows...
            if (firstMeetingStageDeadline == null) {
                String errorMessage = "Stage(id=" + firstMeetingStage.getId()
                        + ", type=" + firstMeetingStage.getType().name()
                        + ")'s deadline is null. Setting of first meeting date for Group(id=" +
                        group.getId() + ") aborted";
                log.log(Level.WARN, errorMessage);
                throw new NoSuchElementException(errorMessage);
            }

            if (date.isBefore(firstMeetingStageDeadline) && date.isAfter(OffsetDateTime.now())) {
                // set group's first meeting date, if it before First Meeting Stage deadline and after current date-time
                group.setFirstMeetingDate(date);
                // saving updated group
                group = groupRepository.save(group);

                log.log(Level.INFO, "Group(id=" + group.getId() + ")'s first meeting date set to '"
                        + group.getFirstMeetingDate() + "'");
                return group;
            } else {
                String errorMessage = "Provided date '" + date + "' is later than Stage(type="
                        + firstMeetingStage.getType().name() + ") deadline '" + firstMeetingStageDeadline
                        + "' or earlier than current date-time";
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        } else {
            String errorMessage = "Group for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist";
            log.log(Level.WARN, errorMessage);
            throw new NoSuchElementException(errorMessage);
        }
    }

    public Group setFirstMeetingStage() throws IllegalArgumentException, IllegalStateException, NoSuchElementException {
        // get current mentor
        MentorDTO currentMentorDTO = this.getCurrentMentorDTO();
        // find Group for current mentor
        Optional<Group> optionalGroup = groupRepository.findByMentorId(currentMentorDTO.getId());
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            if (!(StageType.DISTRIBUTION).equals(group.getStage().getType())) {
                throw new IllegalStateException("Group(id=" + group.getId() + ") has an illegal Stage(type="
                        + group.getStage().getType().name()
                        + "). Required Stage(type=" + StageType.DISTRIBUTION.name() + ")");
            }

            Optional<Stage> optionalStage = stageService.findByType(StageType.FIRST_MEETING);
            if (!optionalStage.isPresent()) {
                throw new IllegalArgumentException("Can't find Stage(type=" + StageType.FIRST_MEETING.name() + ")");
            }

            // setting stage
            GroupService.setStage(group, optionalStage.get());

            // saving updated group
            group = groupRepository.save(group);

            log.log(Level.INFO, "Group(id=" + group.getId() + ") stage changed to Stage(id=" + optionalStage.get().getId() + ")");
            return group;
        } else {
            throw new NoSuchElementException("Group for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist");
        }
    }

    public List<Group> setFirstMeetingStageBulk() throws IllegalArgumentException {

        Optional<Stage> optionalDistributionStage = stageService.findByType(StageType.DISTRIBUTION);
        if (!optionalDistributionStage.isPresent()) {
            throw new IllegalArgumentException("Can't find Stage(type=" + StageType.DISTRIBUTION.name() + ")");
        }

        List<Group> groups = IterableUtils.toList(groupRepository.findAllByStageId(optionalDistributionStage.get().getId()));
        if (CollectionUtils.isEmpty(groups)) {
            log.log(Level.INFO, "There are no groups found for Stage(id=" + optionalDistributionStage.get().getId() + ")");
            return Collections.emptyList();
        }

        Optional<Stage> optionalStage = stageService.findByType(StageType.FIRST_MEETING);
        if (!optionalStage.isPresent()) {
            throw new IllegalArgumentException("Can't find Stage(type=" + StageType.FIRST_MEETING.name() + ")");
        }

        // setting stage to groups
        groups.forEach(group -> GroupService.setStage(group, optionalStage.get()));

        // saving updated groups
        groups = IterableUtils.toList(groupRepository.saveAll(groups));
        return groups;
    }

    private static void setStage(Group group, Stage stage) {
        group.setStage(stage);
        group.setStageId(stage.getId());
        log.log(Level.INFO, "Set Stage(id=" + stage.getId() + ") to Group(id=" + group.getId() + ")");
    }
}
