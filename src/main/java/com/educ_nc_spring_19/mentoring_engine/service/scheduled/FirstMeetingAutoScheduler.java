package com.educ_nc_spring_19.mentoring_engine.service.scheduled;

import com.educ_nc_spring_19.mentoring_engine.enums.StageType;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Stage;
import com.educ_nc_spring_19.mentoring_engine.service.GroupService;
import com.educ_nc_spring_19.mentoring_engine.service.InviteService;
import com.educ_nc_spring_19.mentoring_engine.service.StageService;
import com.educ_nc_spring_19.mentoring_engine.util.InviteLinkPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Component
public class FirstMeetingAutoScheduler {

    private final GroupService groupService;
    private final InviteService inviteService;
    private final StageService stageService;

    @Scheduled(cron = "0 0/5 * 1/1 * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setFirstMeetingStageAndSendInvites() {
        log.log(Level.INFO, "Start of setFirstMeetingStageAndSendInvites() method");
        Optional<Stage> optionalStage = stageService.findByType(StageType.FIRST_MEETING);
        if (!optionalStage.isPresent()) {
            log.log(Level.WARN, "Can't find Stage(type=" + StageType.FIRST_MEETING.name()
                    + "). setFirstMeetingStageAndSendInvites() method stopped");
            return;
        }

        Stage stage = optionalStage.get();
        OffsetDateTime stageDeadline = stage.getDeadline();
        if (stageDeadline == null) {
            log.log(Level.WARN, "Deadline of Stage(id=" + stage.getId()
                    + ") is null. setFirstMeetingStageAndSendInvites() method stopped");
            return;
        }

        OffsetDateTime currentTime = OffsetDateTime.now();
        if (currentTime.compareTo(stageDeadline) < 0) {
            log.log(Level.INFO, "Current time (" + currentTime.toString()
                    + ") less than deadline of Stage(id=" + stage.getId() + ", deadline=" + stageDeadline.toString()
                    + "). setFirstMeetingStageAndSendInvites() method stopped");
            return;
        }

        List<Group> groups = Collections.emptyList();
        try {
            log.log(Level.INFO, "Trying to set FirstMeeting stage to groups (BULK)");
            groups = groupService.setFirstMeetingStageBulk();
            // logging of changes
            groups.forEach(group -> log.log(Level.INFO, "Stage changed to Stage(id=" + group.getStageId()
                    + ") for Group(id=" + group.getId() + ")")
            );
        } catch (IllegalArgumentException iAE) {
            log.log(Level.WARN, iAE);
        }

        if (CollectionUtils.isNotEmpty(groups)) {
            log.log(Level.INFO, "Trying to send invites (BULK)");
            List<Map<UUID, InviteLinkPair>> invites = new ArrayList<>();
            groups.forEach(group -> {
                try {
                    invites.add(inviteService.getGroupInviteLinks(group));
                } catch (IllegalArgumentException iAE) {
                    log.log(Level.WARN, iAE);
                }

            });

            // saving groups after students status change
            groups = groupService.saveAll(groups);
            log.log(Level.INFO, "Groups saved: " + groups.stream().map(Group::getId).collect(Collectors.toList()));

            // collecting student's ID
            Set<UUID> studentIds = new HashSet<>();
            invites.forEach(invite -> studentIds.addAll(invite.keySet()));

            studentIds.forEach(studentId -> log.log(Level.INFO, "Invite sent for Student(id=" + studentId + ")"));
        }

        log.log(Level.INFO, "End of setFirstMeetingStageAndSendInvites() method");
    }
}
