package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.mentoring_engine.mapper.PoolMapper;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.service.PoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/mentoring-engine/rest/api/v1/pool")
public class PoolController {
    private final ObjectMapper objectMapper;
    private final PoolMapper poolMapper;
    private final PoolService poolService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity find(@RequestParam(value = "id", required = false) List<UUID> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(poolMapper.toPoolsDTO(poolService.findAll()));
        } else if (ids.size() == 1) {
            return findById(ids.get(0));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(poolMapper.toPoolsDTO(poolService.findAllById(ids)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity findById(@PathVariable(name = "id") UUID id) {
        Optional<Pool> pool = poolService.findById(id);
        return pool.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(poolMapper.toPoolDTO(pool.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(objectMapper.createObjectNode().put("message","Pool(id=" + id + ") not found"));
    }
}
