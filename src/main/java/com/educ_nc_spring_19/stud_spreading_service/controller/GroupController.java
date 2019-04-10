package com.educ_nc_spring_19.stud_spreading_service.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.GroupDTO;
import com.educ_nc_spring_19.stud_spreading_service.mapper.GroupMapper;
import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import com.educ_nc_spring_19.stud_spreading_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
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
}
