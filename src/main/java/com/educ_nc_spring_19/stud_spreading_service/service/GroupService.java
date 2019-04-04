package com.educ_nc_spring_19.stud_spreading_service.service;

import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import com.educ_nc_spring_19.stud_spreading_service.service.repo.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
