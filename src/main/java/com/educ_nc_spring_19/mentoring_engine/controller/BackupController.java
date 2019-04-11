package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.service.BackupService;
import com.educ_nc_spring_19.mentoring_engine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/mentoring-engine/rest/api/v1/backup")
public class BackupController {

    private final BackupService backupService;
    private final MasterDataClient masterDataClient;
    private final UserService userService;

    @GetMapping("/dropdown")
    public ResponseEntity<List<MentorDTO>> findVacantBackups() {
        List<UUID> allEmployedBackupsIds = backupService.findAll();
        log.log(Level.INFO, "allEmployedBackupsIds: " + allEmployedBackupsIds);

        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(userService.getCurrentUserId());
        UUID currentMentorDirectionId = currentMentorDTO.getDirectionId();
        log.log(Level.INFO, "currentMentorDirectionId: " + currentMentorDirectionId.toString());

        List<MentorDTO> mentorsByDirectionId = masterDataClient.getMentorsByDirectoryId(currentMentorDirectionId);

        if (CollectionUtils.isEmpty(mentorsByDirectionId)) {
            log.log(Level.WARN, "mentorsByDirectionId is empty");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Collections.emptyList());
        }

        List<MentorDTO> responseMentors = mentorsByDirectionId.stream()
                .filter(mentorDTO -> mentorDTO.getDirectionId().equals(currentMentorDirectionId)
                        && !allEmployedBackupsIds.contains(mentorDTO.getId())
                        && !mentorDTO.getId().equals(currentMentorDTO.getId())
                )
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseMentors);
    }
}
