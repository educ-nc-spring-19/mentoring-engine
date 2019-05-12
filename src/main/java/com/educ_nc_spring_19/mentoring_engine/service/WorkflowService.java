package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.DirectionDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.enums.StageType;
import com.educ_nc_spring_19.mentoring_engine.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class WorkflowService {

    private final CauldronService cauldronService;
    private final GroupService groupService;
    private final LectureService lectureService;
    private final MasterDataClient masterDataClient;
    private final PoolService poolService;
    private final StageService stageService;

    public Map<String, List<?>> init() {

        // delete all data
        lectureService.deleteAll();
        poolService.deleteAll();
        cauldronService.deleteAll();
        groupService.deleteAll();
        log.log(Level.INFO, "All lectures, pools, cauldrons and groups were deleted");

        Map<String, List<?>> result = new HashMap<>();

        Set<DirectionDTO> directionDTOS = new HashSet<>(masterDataClient.getAllDirections());
        Set<MentorDTO> mentorDTOS = new HashSet<>(masterDataClient.getAllMentors());
        Set<StudentDTO> studentDTOS = new HashSet<>(masterDataClient.getAllStudents());

        if (CollectionUtils.isEmpty(directionDTOS)
                || CollectionUtils.isEmpty(mentorDTOS)
                || CollectionUtils.isEmpty(studentDTOS)) {
            log.log(Level.WARN, "Directions or Mentors, or Students is empty! Check Master Data.");
            return result;
        }

        // create cauldrons
        Set<String> deptNames = mentorDTOS.stream()
                .map(MentorDTO::getDeptName)
                .collect(Collectors.toSet());

        Map<UUID, StudentDTO> vacantStudentIdDTOMap = new HashMap<>(studentDTOS.size());
        studentDTOS.forEach(studentDTO -> vacantStudentIdDTOMap.put(studentDTO.getId(), studentDTO));

        Set<Cauldron> cauldrons = new HashSet<>();
        deptNames.forEach(name -> {
            Cauldron cauldron = new Cauldron();
            cauldron.setName(name + "'s cauldron");
            cauldron.setDescription(name + " description");

            Set<UUID> cauldronMentorIds = mentorDTOS.stream()
                    .filter(mentorDTO -> mentorDTO.getDeptName().equals(name))
                    .map(MentorDTO::getId)
                    .collect(Collectors.toSet());
            cauldron.setMentors(cauldronMentorIds);

            Set<UUID> cauldronStudentIds = studentDTOS.stream()
                    .filter(studentDTO -> cauldronMentorIds.contains(studentDTO.getInterviewerId()))
                    .map(StudentDTO::getId)
                    .collect(Collectors.toSet());
            cauldron.setStudents(cauldronStudentIds);

            cauldronStudentIds.forEach(vacantStudentIdDTOMap::remove);

            cauldrons.add(cauldron);
        });

        List<Cauldron> createdCauldrons = cauldronService.saveAll(cauldrons);
        log.log(Level.INFO, "Cauldrons created: " + createdCauldrons);
        result.put("cauldrons", createdCauldrons);
        // end create cauldrons

        // create pools
        List<Pool> pools = new ArrayList<>();
        directionDTOS.forEach(directionDTO -> {
            Pool pool = new Pool();
            pool.setDirectionId(directionDTO.getId());

            Set<UUID> poolStudentIds = new HashSet<>();
            vacantStudentIdDTOMap.forEach((studentId, studentDTO) -> {
                if (studentDTO.getDirectionId().equals(directionDTO.getId())) {
                    poolStudentIds.add(studentId);
                }
            });
            poolStudentIds.forEach(vacantStudentIdDTOMap::remove);

            pool.setStudents(poolStudentIds);
            pools.add(pool);
        });

        List<Pool> createdPools = poolService.saveAll(pools);
        log.log(Level.INFO, "Pools created: " + createdPools);
        result.put("pools", createdPools);
        // end create pools

        // create lectures
        List<Lecture> lectures = new ArrayList<>();
        directionDTOS.forEach(directionDTO -> {
            Lecture lecture = new Lecture();
            lecture.setDirectionId(directionDTO.getId());
            lectures.add(lecture);
        });

        List<Lecture> createdLectures = lectureService.saveAll(lectures);
        log.log(Level.INFO, "Lectures created: " + createdLectures);
        result.put("lectures", createdLectures);
        // end create lectures

        // create groups
        Optional<Stage> stage = stageService.findByType(StageType.DISTRIBUTION);
        if (!stage.isPresent()) {
            log.log(Level.WARN, "Stage(type=" + StageType.DISTRIBUTION.name() + ") doesn't exist! No groups created.");
            return result;
        }

        List<Group> groups = new ArrayList<>();
        mentorDTOS.forEach(mentorDTO -> {
            Group group = new Group();
            group.setName(mentorDTO.getAcronym() + "'s group");
            group.setMentorId(mentorDTO.getId());
            group.setStage(stage.get());
            group.setStageId(stage.get().getId());
            groups.add(group);
        });

        List<Group> createdGroups = groupService.saveAll(groups);
        log.log(Level.INFO, "Groups created: " + createdGroups);
        result.put("groups", createdGroups);
        // end create groups

        return result;
    }
}
