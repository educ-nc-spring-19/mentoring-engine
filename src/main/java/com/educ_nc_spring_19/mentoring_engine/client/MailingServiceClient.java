package com.educ_nc_spring_19.mentoring_engine.client;

import com.educ_nc_spring_19.mentoring_engine.util.InviteLinkPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Log4j2
@Component
public class MailingServiceClient {
    private final String MAILING_SERVICE_URL = "127.0.0.1";
    private final String MAILING_SERVICE_PORT ="55060";
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public MailingServiceClient(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    private ObjectNode createInviteEmailJSON(UUID studentId, UUID mentorId, UUID backupId, InviteLinkPair linkPair) {

        ObjectNode inviteEmailBody = objectMapper.createObjectNode();
        inviteEmailBody.put("receiver_id", studentId.toString());
        inviteEmailBody.put("receiver_type", "student");
        inviteEmailBody.put("type", "congratulations");

        ObjectNode argsNode = objectMapper.createObjectNode();
        argsNode.put("Student name", studentId.toString());
        if (mentorId != null) {
            argsNode.put("Mentor name", mentorId.toString());
        }
        if (backupId != null) {
            argsNode.put("Backup name", backupId.toString());
        }
        if (linkPair.getAcceptLink() != null) {
            argsNode.put("Accept link", linkPair.getAcceptLink().toString());
        }
        if (linkPair.getRejectLink() != null) {
            argsNode.put("Reject link", linkPair.getRejectLink().toString());
        }

        inviteEmailBody.set("Args", argsNode);

        log.log(Level.INFO, "createInviteEmailJSON() result: " + inviteEmailBody.toString());

        return inviteEmailBody;
    }

    public void sendInviteEmail(UUID studentId, UUID mentorId, UUID backupId, InviteLinkPair linkPair) {
        ObjectNode request = createInviteEmailJSON(studentId, mentorId, backupId, linkPair);

        try {
            ResponseEntity<ObjectNode> response = restTemplate.exchange(
                    UriComponentsBuilder.newInstance().scheme("http").host(MAILING_SERVICE_URL).port(MAILING_SERVICE_PORT)
                            .path("/mailing-service/rest/api/v1/letter/send")
                            .build().toUri(),
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ObjectNode>(){});

            log.log(Level.INFO, "sendInviteEmail() http status = " + response.getStatusCodeValue());
            if (response.getStatusCode().equals(HttpStatus.OK)
                    && response.getBody() != null
                    && response.getBody().has("message")) {
                log.log(Level.INFO, "Mailing Service response message: " + response.getBody().get("message").asText());
            } else if (response.getBody() != null && response.getBody().has("message")) {
                log.log(Level.WARN, "Mailing Service response message: " + response.getBody().get("message").asText());
            } else {
                log.log(Level.WARN, "An error occurred while taking body from Mailing Service response");
            }
        } catch (RestClientException rCE) {
            log.log(Level.WARN, rCE);
        }
    }
}
