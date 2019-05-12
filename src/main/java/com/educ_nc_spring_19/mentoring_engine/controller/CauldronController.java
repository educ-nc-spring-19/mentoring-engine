package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.CauldronDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.mentoring_engine.mapper.CauldronMapper;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import com.educ_nc_spring_19.mentoring_engine.service.CauldronService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/mentoring-engine/rest/api/v1/cauldron")
public class CauldronController {
    private final CauldronMapper cauldronMapper;
    private final CauldronService cauldronService;
    private final ObjectMapper objectMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity find(@RequestParam(value = "id", required = false) List<UUID> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(cauldronMapper.toCauldronsDTO(cauldronService.findAll()));
        } else if (ids.size() == 1) {
            return findById(ids.get(0));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(cauldronMapper.toCauldronsDTO(cauldronService.findAllById(ids)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity findById(@PathVariable(name = "id") UUID id) {
        Optional<Cauldron> cauldron = cauldronService.findById(id);
        return cauldron.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(cauldronMapper.toCauldronDTO(cauldron.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(objectMapper.createObjectNode().put("message","Cauldron(id=" + id + ") not found"));
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

        CauldronDTO cauldronDTO;
        try {
            cauldronDTO = cauldronMapper.toCauldronDTO(cauldronService.removeStudentId(studentDTO.getId()));
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(cauldronDTO);
    }
}
