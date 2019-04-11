package com.educ_nc_spring_19.stud_spreading_service.service;

import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import com.educ_nc_spring_19.stud_spreading_service.model.entity.Stage;
import com.educ_nc_spring_19.stud_spreading_service.service.repo.GroupRepository;
import com.educ_nc_spring_19.stud_spreading_service.service.repo.StageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final StageService stageService;

    public Optional<Group> findByMentorId(UUID mentorId) {
        return groupRepository.findByMentorId(mentorId);
    }

    public Optional<Group> findById(UUID id) {
        return groupRepository.findById(id);
    }

    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAll().forEach(groups::add);
        return groups;
    }

    public List<Group> findAllById(Iterable<UUID> ids) {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAllById(ids).forEach(groups::add);
        return groups;
    }

    public Group create(String name, UUID mentorId) throws IllegalArgumentException {
        Group group = new Group();
        group.setName(name);
        group.setMentorId(mentorId);

        final UUID FIRST_STAGE_ID = UUID.fromString("390748bf-2b6a-4b4e-93c5-51f431eae1db");
        Optional<Stage> stage = stageService.findById(FIRST_STAGE_ID);
        if (stage.isPresent()) {
            group.setStage(stage.get());
            group.setStageId(FIRST_STAGE_ID);
        } else {
            throw new IllegalArgumentException("Can't create new group cause illegal UUID in \'FIRST_STAGE_ID\': \'"
                    + FIRST_STAGE_ID.toString() + "\'");
        }

        return groupRepository.save(group);
    }

    public void delete(UUID id) {
        groupRepository.deleteById(id);
    }

    public Group setBackupId(UUID groupId, UUID backupId) throws IllegalArgumentException {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            group.setBackupId(backupId);
            return groupRepository.save(group);
        } else {
            throw new IllegalArgumentException("Group with id=" + groupId + " doesn't exist");
        }
    }
}
