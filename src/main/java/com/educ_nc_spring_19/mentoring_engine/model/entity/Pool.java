package com.educ_nc_spring_19.mentoring_engine.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Audit;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Auditable;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.listener.AuditListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data

@Entity
@EntityListeners(AuditListener.class)
public class Pool implements Auditable {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID directionId;

    @Embedded
    private Audit audit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "pool_student",
            joinColumns = @JoinColumn(name = "pool_id")
    )
    @Column(name = "student_id", nullable = false)
    private List<UUID> students;
}
