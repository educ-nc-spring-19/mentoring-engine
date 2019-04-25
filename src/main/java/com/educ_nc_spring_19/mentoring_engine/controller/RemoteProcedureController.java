package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.mentoring_engine.mapper.CauldronMapper;
import com.educ_nc_spring_19.mentoring_engine.mapper.GroupMapper;
import com.educ_nc_spring_19.mentoring_engine.mapper.PoolMapper;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/mentoring-engine/rest/api/v1/rpc")
public class RemoteProcedureController {
    private final WorkflowService workflowService;

    private final PoolMapper poolMapper;
    private final CauldronMapper cauldronMapper;
    private final GroupMapper groupMapper;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/workflow-init", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity workflowInit() {
        Map<String, List<?>> resultOfInit = workflowService.init();
        if (MapUtils.isEmpty(resultOfInit)) {
            log.log(Level.WARN, "result of workflow init is empty");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        Map<String, List<?>> response = new HashMap<>();

        // Unchecked casts, but we trust to map keys returned from WorkflowService
        if (CollectionUtils.isNotEmpty(resultOfInit.get("pools"))) {
            response.put("pools", poolMapper.toPoolsDTO((List<Pool>) resultOfInit.get("pools")));
        }

        if (CollectionUtils.isNotEmpty(resultOfInit.get("cauldrons"))) {
            response.put("cauldrons", cauldronMapper.toCauldronsDTO((List<Cauldron>) resultOfInit.get("cauldrons")));
        }

        if (CollectionUtils.isNotEmpty(resultOfInit.get("groups"))) {
            response.put("groups", groupMapper.toGroupsDTO((List<Group>) resultOfInit.get("groups")));
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
