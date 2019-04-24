package com.educ_nc_spring_19.mentoring_engine.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Audit;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Auditable;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.listener.AuditListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data

@Entity
@EntityListeners(AuditListener.class)
public class Stage implements Auditable {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime deadline;

    @Embedded
    private Audit audit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "stage",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Group> groups;
}
