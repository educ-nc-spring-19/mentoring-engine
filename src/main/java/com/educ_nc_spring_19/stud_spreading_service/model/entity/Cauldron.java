package com.educ_nc_spring_19.stud_spreading_service.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Audit;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.Auditable;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.listener.AuditListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
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
    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "cauldron_mentor",
            joinColumns = @JoinColumn(name = "cauldron_id")
    )
    @Column(name = "mentor_id", nullable = false)
    private List<UUID> mentors;
}
