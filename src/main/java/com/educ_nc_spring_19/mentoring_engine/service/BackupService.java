package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.service.repo.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class BackupService {
    private final GroupRepository groupRepository;

    public Set<UUID> findAll() {
        Set<UUID> backups = new HashSet<>();
        groupRepository.findAll().forEach(group -> backups.add(group.getBackupId()));
        return backups;
    }
}
