package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.GroupDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.mentoring_engine.mapper.GroupMapper;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping("/mentoring-engine/rest/api/v1/group")
public class GroupController {
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final ObjectMapper objectMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity find(@RequestParam(value = "id", required = false) List<UUID> ids) {

        Set<Group> groupsToResponse = new HashSet<>();

        if (CollectionUtils.isEmpty(ids)) {
            groupsToResponse.addAll(groupService.findAll());
        } else {
            groupsToResponse.addAll(groupService.findAllById(ids));
        }

        return ResponseEntity.status(HttpStatus.OK).body(groupMapper.toGroupsDTO(new ArrayList<>(groupsToResponse)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity findById(@PathVariable(name = "id") UUID id) {
        Optional<Group> group = groupService.findById(id);
        return group.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(groupMapper.toGroupDTO(group.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(objectMapper.createObjectNode().put("message", "Group(id=" + id + ") not found"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity create(@RequestBody GroupDTO groupDTO) {

        String groupName = groupDTO.getName();
        if (StringUtils.isBlank(groupName)) {
            log.log(Level.WARN, "create(): groupDTO.name is 'null'");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(objectMapper.createObjectNode().put("message","body param 'name' is 'null'"));
        }

        GroupDTO responseGroupDTO;
        try {
            responseGroupDTO = groupMapper.toGroupDTO(groupService.create(groupName));
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responseGroupDTO);
    }

    @PatchMapping(path = "/backup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setBackup(@RequestBody GroupDTO groupDTO) {
        if (groupDTO.getBackupId() == null) {
            log.log(Level.WARN, "setBackup(): groupDTO.backupId is 'null'");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(objectMapper.createObjectNode().put("message","body param 'backupId' is 'null'"));
        }

        GroupDTO responseGroupDTO;
        try {
            responseGroupDTO = groupMapper.toGroupDTO(groupService.setBackupId(groupDTO.getBackupId()));
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseGroupDTO);
    }

    @PatchMapping(path = "/add-student",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addStudentId(@RequestBody StudentDTO studentDTO) {
        if (studentDTO.getId() == null) {
            log.log(Level.WARN, "addStudentId(): studentDTO.id is 'null'");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(objectMapper.createObjectNode().put("message","body param 'id' is 'null'"));
        }

        GroupDTO groupDTO;
        try {
            groupDTO = groupMapper.toGroupDTO(groupService.addStudentId(studentDTO.getId()));
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(groupDTO);
    }

    @PatchMapping(path = "/remove-student",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeStudentId(@RequestBody StudentDTO studentDTO) {
        if (studentDTO.getId() == null) {
            log.log(Level.WARN, "removeStudentId(): studentDTO.id is 'null'");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(objectMapper.createObjectNode().put("message","body param 'id' is 'null'"));
        }

        GroupDTO groupDTO;
        try {
            groupDTO = groupMapper.toGroupDTO(groupService.removeStudentId(studentDTO.getId()));
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(groupDTO);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deleteById(@PathVariable(name = "id") UUID id) {
        try {
            groupService.deleteById(id);
        } catch (EmptyResultDataAccessException eRDAE) {
            log.log(Level.WARN, eRDAE);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
