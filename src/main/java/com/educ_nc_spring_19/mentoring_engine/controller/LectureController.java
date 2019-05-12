package com.educ_nc_spring_19.mentoring_engine.controller;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.LectureDTO;
import com.educ_nc_spring_19.mentoring_engine.mapper.LectureMapper;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Lecture;
import com.educ_nc_spring_19.mentoring_engine.service.LectureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
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
@RequestMapping("/mentoring-engine/rest/api/v1/lecture")
public class LectureController {
    private final LectureMapper lectureMapper;
    private final LectureService lectureService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/add-day",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addLectureDayTime(@RequestBody ObjectNode body) {
        LectureDTO lectureDTO;
        try {
            if (!body.has("id")) {
                throw new IllegalArgumentException("Provided request body has no 'id' field");
            } else if (!body.has("day")) {
                throw new IllegalArgumentException("Provided request body has no 'day' field");
            } else if (!body.has("time")) {
                throw new IllegalArgumentException("Provided request body has no 'time' time");
            }

            JsonNode idField = body.get("id");
            if (idField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'id' field is null");
            }

            JsonNode dayField = body.get("day");
            if (dayField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'day' field is null");
            }

            JsonNode timeField = body.get("time");
            if (timeField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'time' field is null");
            }

            UUID id = UUID.fromString(idField.textValue());
            DayOfWeek day = DayOfWeek.valueOf(dayField.textValue());
            OffsetTime time = OffsetTime.parse(timeField.textValue(), DateTimeFormatter.ISO_OFFSET_TIME);

            lectureDTO = lectureMapper.toLectureDTO(lectureService.addLectureDayTime(id, day, time));

        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.log(Level.WARN, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(lectureDTO);
    }

    @PatchMapping(path = "/delete-day",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteLectureDay(@RequestBody ObjectNode body) {
        LectureDTO lectureDTO;
        try {
            if (!body.has("id")) {
                throw new IllegalArgumentException("Provided request body has no 'id' field");
            } else if (!body.has("day")) {
                throw new IllegalArgumentException("Provided request body has no 'day' field");
            }

            JsonNode idField = body.get("id");
            if (idField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'id' field is null");
            }

            JsonNode dayField = body.get("day");
            if (dayField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'day' field is null");
            }

            UUID id = UUID.fromString(idField.textValue());
            DayOfWeek day = DayOfWeek.valueOf(dayField.textValue());

            lectureDTO = lectureMapper.toLectureDTO(lectureService.deleteLectureDay(id, day));

        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iAE);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(lectureDTO);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity find(@RequestParam(value = "id", required = false) List<UUID> ids,
                               @RequestParam(value = "directionId", required = false) UUID directionId) {
        Set<Lecture> lectures = new HashSet<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            lectures.addAll(lectureService.findAllById(ids));
        }
        if (directionId != null) {
            lectureService.findByDirectionId(directionId).ifPresent(lectures::add);
        }
        if (CollectionUtils.isEmpty(ids) && directionId == null) {
            lectures.addAll(lectureService.findAll());
        }

        // returning response
        if (lectures.size() == 1) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(lectureMapper.toLectureDTO(new ArrayList<>(lectures).get(0)));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(lectureMapper.toLecturesDTO(new ArrayList<>(lectures)));
        }
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity findById(@PathVariable(name = "id") UUID id) {
        Optional<Lecture> optionalLecture = lectureService.findById(id);
        return optionalLecture.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(lectureMapper.toLectureDTO(optionalLecture.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(objectMapper.createObjectNode().put("message", "Lecture(id=" + id + ") not found"));
    }

    @PatchMapping(path = "/first-lecture",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setFirstLecture(@RequestBody ObjectNode body) {
        LectureDTO lectureDTO;

        try {
            if (!body.has("id")) {
                throw new IllegalArgumentException("Provided request body has no 'id' field");
            } else if (!body.has("firstLecture")) {
                throw new IllegalArgumentException("Provided request body has no 'firstLecture' field");
            }

            JsonNode idField = body.get("id");
            if (idField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'id' field is null");
            }

            JsonNode firstLectureField = body.get("firstLecture");
            if (firstLectureField.isNull()) {
                throw new IllegalArgumentException("Provided request body's 'firstLecture' field is null");
            }

            UUID id = UUID.fromString(idField.textValue());
            OffsetDateTime firstLecture = OffsetDateTime.parse(firstLectureField.textValue(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            lectureDTO = lectureMapper.toLectureDTO(lectureService.setFirstLectureDay(id, firstLecture));

        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.log(Level.WARN, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        } catch (NoSuchElementException nSEE) {
            log.log(Level.WARN, nSEE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(nSEE);
        }

        return ResponseEntity.status(HttpStatus.OK).body(lectureDTO);
    }
}
