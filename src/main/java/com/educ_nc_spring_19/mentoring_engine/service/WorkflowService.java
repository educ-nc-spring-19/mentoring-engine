package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.DirectionDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Stage;
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

    private final MasterDataClient masterDataClient;

    private final CauldronService cauldronService;
    private final GroupService groupService;
    private final PoolService poolService;
    private final StageService stageService;

    // TO DO: Addition vacant students to pools
    public Map<String, List<?>> init() {

        // delete all data
        poolService.deleteAll();
        cauldronService.deleteAll();
        groupService.deleteAll();
        log.log(Level.INFO, "All pools, cauldrons and groups were deleted");

        Map<String, List<?>> result = new HashMap<>();

        List<DirectionDTO> directionDTOS = masterDataClient.getAllDirections();
        if (CollectionUtils.isNotEmpty(directionDTOS)) {
            // create pools
            List<Pool> pools = new LinkedList<>();
            directionDTOS.forEach(directionDTO -> {
                Pool pool = new Pool();
                pool.setDirectionId(directionDTO.getId());
                pools.add(pool);
            });

            List<Pool> createdPools = poolService.saveAll(pools);
            log.log(Level.INFO, "Pools created: " + createdPools);
            result.put("pools", createdPools);
        } else {
            log.log(Level.WARN, "There are no directions found!");
            return result;
        }

        List<MentorDTO> mentorDTOS = masterDataClient.getAllMentors();
        List<StudentDTO> studentDTOS = masterDataClient.getAllStudents();

        if (CollectionUtils.isEmpty(mentorDTOS)) {
            log.log(Level.WARN, "There are no mentors found!");
            return result;
        } else if (CollectionUtils.isEmpty(studentDTOS)) {
            log.log(Level.WARN, "There are no students found!");
            return result;
        } else {
            Set<String> deptNames = new HashSet<>();
            mentorDTOS.forEach(mentorDTO -> deptNames.add(mentorDTO.getDeptName()));
            // create cauldrons
            List<Cauldron> cauldrons = new LinkedList<>();
            deptNames.forEach(name -> {
                Cauldron cauldron = new Cauldron();
                cauldron.setName(name + "'s cauldron");
                cauldron.setDescription(name + " description");

                List<UUID> cauldronMentorIds = mentorDTOS.stream()
                        .filter(mentorDTO -> mentorDTO.getDeptName().equals(name))
                        .map(MentorDTO::getId)
                        .collect(Collectors.toList());
                cauldron.setMentors(cauldronMentorIds);

                List<UUID> cauldronStudentIds = studentDTOS.stream()
                        .filter(studentDTO -> cauldronMentorIds.contains(studentDTO.getInterviewerId()))
                        .map(StudentDTO::getId)
                        .collect(Collectors.toList());
                cauldron.setStudents(cauldronStudentIds);

                cauldrons.add(cauldron);
            });

            List<Cauldron> createdCauldrons = cauldronService.saveAll(cauldrons);
            log.log(Level.INFO, "Cauldrons created: " + createdCauldrons);
            result.put("cauldrons", createdCauldrons);
        }

        // TO DO CREATE GROUPS
        final UUID FIRST_STAGE_ID = UUID.fromString("390748bf-2b6a-4b4e-93c5-51f431eae1db");
        Optional<Stage> stage = stageService.findById(FIRST_STAGE_ID);
        if (!stage.isPresent()) {
            log.log(Level.WARN, "Stage with ID=" + FIRST_STAGE_ID + " doesn't exist! No groups created.");
            return result;
        }

        List<Group> groups = new LinkedList<>();
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

        return result;
    }
}
