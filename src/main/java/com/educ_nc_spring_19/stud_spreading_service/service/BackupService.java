package com.educ_nc_spring_19.stud_spreading_service.service;

import com.educ_nc_spring_19.stud_spreading_service.service.repo.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BackupService {
    private final GroupRepository groupRepository;

    public List<UUID> findAll() {
        List<UUID> backups = new LinkedList<>();
        groupRepository.findAll().forEach(group -> backups.add(group.getBackupId()));
        return backups;
    }
}
