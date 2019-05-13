package com.educ_nc_spring_19.mentoring_engine.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Audit;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Auditable;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.DayOfWeekTime;
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
public class Lecture implements Auditable {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID directionId;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime firstLecture;

    @Embedded
    private Audit audit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Embedded
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "lecture_day",
            joinColumns = @JoinColumn(name = "dir_lec_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"dir_lec_id", "day"})
    )
    private Set<DayOfWeekTime> lectureDays;
}
