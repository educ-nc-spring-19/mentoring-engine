package com.educ_nc_spring_19.stud_spreading_service.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.GroupDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.stud_spreading_service.client.MasterDataClient;
import com.educ_nc_spring_19.stud_spreading_service.mapper.GroupMapper;
import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import com.educ_nc_spring_19.stud_spreading_service.service.GroupService;
import com.educ_nc_spring_19.stud_spreading_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/stud-spreading-service/rest/api/v1/group")
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final UserService userService;
    private final MasterDataClient masterDataClient;

    @GetMapping
    public ResponseEntity<List<GroupDTO>> find(
            @RequestParam(value = "id", required = false) List<UUID> ids) {

        Set<Group> groupsToResponse = new HashSet<>();

        if (CollectionUtils.isNotEmpty(ids)) {
            groupsToResponse.addAll(groupService.findAllById(ids));
        }
        if (CollectionUtils.isEmpty(ids)) {
            groupsToResponse.addAll(groupService.findAll());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(groupMapper.toGroupsDTO(new ArrayList<>(groupsToResponse)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> findById(@PathVariable(name = "id") UUID id) {
        Optional<Group> group = groupService.findById(id);
        return group.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(groupMapper.toGroupDTO(group.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDTO> create(@RequestBody GroupDTO groupDTO) {

        String groupName = groupDTO.getName();
        if (StringUtils.isBlank(groupName)) {
            log.log(Level.WARN, "groupDTO.name is empty");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        UUID currentUserId = userService.getCurrentUserId();
        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(currentUserId);
        if (currentMentorDTO == null) {
            log.log(Level.WARN, "Can't find Mentor by userId=" + currentUserId.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UUID mentorId = currentMentorDTO.getId();
        if (groupService.findByMentorId(mentorId).isPresent()) {
            log.log(Level.WARN, "Group with mentorId=" + mentorId + " is already exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        GroupDTO responseGroupDTO = groupMapper.toGroupDTO(groupService.create(groupName, mentorId));
        log.log(Level.INFO, "New group created: " + responseGroupDTO.toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseGroupDTO);
    }

    @PatchMapping(path = "/backup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupDTO> setBackup(@RequestBody GroupDTO groupDTO) {

        UUID backupId = groupDTO.getBackupId();
        if (backupId == null) {
            log.log(Level.WARN, "groupDTO.backupId is empty");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        UUID currentUserId = userService.getCurrentUserId();
        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(currentUserId);
        if (currentMentorDTO == null) {
            log.log(Level.WARN, "Can't find Mentor by userId=" + currentUserId.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UUID mentorId = currentMentorDTO.getId();
        Optional<Group> group = groupService.findByMentorId(mentorId);

        if (!group.isPresent()) {
            log.log(Level.WARN, "Group for mentorId=" + mentorId.toString() + " doesn't exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        GroupDTO responseGroupDTO = groupMapper.toGroupDTO(groupService.setBackupId(group.get().getId(), backupId));
        log.log(Level.INFO, "Set backupId=" + backupId.toString()
                + " to group with id=" + group.get().getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseGroupDTO);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deleteById(@PathVariable(name = "id") UUID id) {
        try {
            groupService.delete(id);
        } catch (EmptyResultDataAccessException eRDAE) {
            log.log(Level.WARN, eRDAE.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        log.log(Level.INFO, "group with id " + id.toString() + " deleted");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
