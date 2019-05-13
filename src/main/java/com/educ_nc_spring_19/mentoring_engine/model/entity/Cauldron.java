package com.educ_nc_spring_19.mentoring_engine.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Audit;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Auditable;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.listener.AuditListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Data

@Entity
@EntityListeners(AuditListener.class)
public class Cauldron implements Auditable {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Embedded
    private Audit audit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "cauldron_mentor",
            joinColumns = @JoinColumn(name = "cauldron_id")
    )
    @Column(name = "mentor_id", unique = true, nullable = false)
    private Set<UUID> mentors;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "cauldron_student",
            joinColumns = @JoinColumn(name = "cauldron_id")
    )
    @Column(name = "student_id", unique = true, nullable = false)
    private Set<UUID> students;
}
