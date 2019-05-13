package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.GroupDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.mentoring_engine.mapper.GroupMapper;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.service.GroupService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/mentoring-engine/rest/api/v1/group")
public class GroupController {
    private final GroupMapper groupMapper;
    private final GroupService groupService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/add-day",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addMeetingDayTime(@RequestBody ObjectNode body) {
        GroupDTO groupDTO;
        try {
            if (!body.has("day")) {
                throw new IllegalArgumentException("Provided request body has no 'day' field");
            } else if (!body.has("time")) {
                throw new IllegalArgumentException("Provided request body has no 'time' time");
            }

            JsonNode dayField = body.get("day");
            if (dayField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'day' field is null");
            }

            JsonNode timeField = body.get("time");
            if (timeField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'time' field is null");
            }

            DayOfWeek day = DayOfWeek.valueOf(dayField.textValue());
            OffsetTime time = OffsetTime.parse(timeField.textValue(), DateTimeFormatter.ISO_OFFSET_TIME);

            groupDTO = groupMapper.toGroupDTO(groupService.addMeetingDayTime(day, time));

        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.log(Level.WARN, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(groupDTO);
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

    @PatchMapping(path = "/delete-day",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteMeetingDay(@RequestBody ObjectNode body) {
        GroupDTO groupDTO;
        try {
            if (!body.has("day")) {
                throw new IllegalArgumentException("Provided request body has no 'day' field");
            }

            JsonNode dayField = body.get("day");
            if (dayField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'day' field is null");
            }

            DayOfWeek day = DayOfWeek.valueOf(dayField.textValue());

            groupDTO = groupMapper.toGroupDTO(groupService.deleteMeetingDay(day));

        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(groupDTO);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity find(@RequestParam(value = "id", required = false) List<UUID> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(groupMapper.toGroupsDTO(groupService.findAll()));
        } else if (ids.size() == 1) {
            return findById(ids.get(0));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(groupMapper.toGroupsDTO(groupService.findAllById(ids)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity findById(@PathVariable(name = "id") UUID id) {
        Optional<Group> optionalGroup = groupService.findById(id);
        return optionalGroup.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(groupMapper.toGroupDTO(optionalGroup.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(objectMapper.createObjectNode().put("message", "Group(id=" + id + ") not found"));
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

    @PatchMapping(path = "/first-meeting-date",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setFirstMeetingDate(@RequestBody ObjectNode body) {
        GroupDTO groupDTO;
        try {
            if (!body.has("firstMeetingDate")) {
                throw new IllegalArgumentException("Provided request body has no 'firstMeetingDate' field");
            }

            JsonNode firstMeetingDateField = body.get("firstMeetingDate");
            if (firstMeetingDateField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'firstMeetingDate' field is null");
            }

            OffsetDateTime firstMeetingDate = OffsetDateTime.parse(firstMeetingDateField.textValue(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            groupDTO = groupMapper.toGroupDTO(groupService.setFirstMeetingDate(firstMeetingDate));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.log(Level.WARN, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        } catch (IllegalStateException iSE) {
            log.log(Level.WARN, iSE);
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(iSE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(groupDTO);
    }

    @GetMapping(path = "/first-meeting-stage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setFirstMeetingStage() {
        GroupDTO groupDTO;
        try {
            groupDTO = groupMapper.toGroupDTO(groupService.setFirstMeetingStage());
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (IllegalStateException iSE) {
            log.log(Level.WARN, iSE);
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(iSE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(groupDTO);
    }
}
