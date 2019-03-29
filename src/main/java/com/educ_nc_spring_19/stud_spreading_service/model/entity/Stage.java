package com.educ_nc_spring_19.stud_spreading_service.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data

@Entity
public class Stage {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime deadline;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdDate;
    private UUID createdByUserId;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime updatedDate;
    private UUID updatedByUserId;

    @OneToMany(mappedBy = "stage")
    private List<Group> groups;
}
