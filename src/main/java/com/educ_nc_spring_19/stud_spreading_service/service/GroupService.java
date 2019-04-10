package com.educ_nc_spring_19.stud_spreading_service.service;

import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import com.educ_nc_spring_19.stud_spreading_service.service.repo.GroupRepository;
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
}
