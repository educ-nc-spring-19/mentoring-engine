package com.educ_nc_spring_19.mentoring_engine.model.entity;


import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Audit;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Auditable;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.DayOfWeekTime;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.StudentStatusBind;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.listener.AuditListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "spr_group")
public class Group implements Auditable {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private UUID mentorId;
    private UUID backupId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "stage_id", insertable = false, updatable = false)
    private UUID stageId;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime firstMeetingDate;

    @Embedded
    private Audit audit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Embedded
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "group_student",
            joinColumns = @JoinColumn(name = "group_id")
    )
    private Set<StudentStatusBind> students;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Embedded
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "group_meeting",
            joinColumns = @JoinColumn(name = "group_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "day"})
    )
    private Set<DayOfWeekTime> meetings;
}
