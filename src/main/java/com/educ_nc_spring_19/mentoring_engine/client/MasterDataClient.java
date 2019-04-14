package com.educ_nc_spring_19.mentoring_engine.client;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Component
public class MasterDataClient {
    private final String MASTER_DATA_URL = "127.0.0.1";
    private final String MASTER_DATA_PORT ="55000";
    private final RestTemplate restTemplate;

    public MasterDataClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public MentorDTO getMentorByUserId(UUID mentorUserId) {
        ResponseEntity<List<MentorDTO>> response = restTemplate.exchange(
                UriComponentsBuilder.newInstance().scheme("http").host(MASTER_DATA_URL).port(MASTER_DATA_PORT)
                        .path("/master-data/rest/api/v1/mentor")
                        .query("{userId}")
                        .buildAndExpand(mentorUserId.toString()).toUri(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MentorDTO>>(){});



        log.log(Level.INFO, "getMentorByUserId, response status = " + response.getStatusCode().toString());
        if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null && !response.getBody().isEmpty()) {
            return response.getBody().get(0);
        }

        return null;
    }

    public List<MentorDTO> getMentorsByDirectoryId(UUID directoryId) {
        if (directoryId == null) {
            log.log(Level.WARN, "directoryId is null");
            return Collections.emptyList();
        }

        ResponseEntity<List<MentorDTO>> response = restTemplate.exchange(
                UriComponentsBuilder.newInstance().scheme("http").host(MASTER_DATA_URL).port(MASTER_DATA_PORT)
                            .path("/master-data/rest/api/v1/mentor")
                            .queryParam("{directoryId}", directoryId.toString())
                            .build().toUri(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MentorDTO>>(){});

        log.log(Level.INFO, "getMentorsByDirectoryId, response status = " + response.getStatusCode().toString());

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }

    public StudentDTO getStudentById(UUID studentId) {
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                UriComponentsBuilder.newInstance().scheme("http").host(MASTER_DATA_URL).port(MASTER_DATA_PORT)
                        .path("/master-data/rest/api/v1/student/{id}")
                        .buildAndExpand(studentId.toString()).toUri(),
                StudentDTO.class
        );

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        log.log(Level.WARN, "studentId: \'" + studentId.toString()
                + "\', statusCode: \'" + response.getStatusCode().toString() + "\'");
        return null;
    }

    public List<StudentDTO> getStudentsById(List<UUID> studentsId) {
        if (CollectionUtils.isEmpty(studentsId)) {
            log.log(Level.WARN, "List of studentsId is null or empty");
            return Collections.emptyList();
        }

        ResponseEntity<List<StudentDTO>> response = restTemplate.exchange(
                UriComponentsBuilder.newInstance().scheme("http").host(MASTER_DATA_URL).port(MASTER_DATA_PORT)
                        .path("/master-data/rest/api/v1/student")
                        .queryParam("{id}", studentsId.stream().map(UUID::toString).collect(Collectors.joining(",")))
                        .build().toUri(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<StudentDTO>>(){});

        log.log(Level.INFO, "getStudentsById, response status = " + response.getStatusCode().toString());

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }
}
