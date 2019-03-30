package com.educ_nc_spring_19.stud_spreading_service.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.CreatedUpdatedDateByUser;
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

    @Embedded
    private CreatedUpdatedDateByUser createdUpdatedDateByUser;

    @OneToMany(mappedBy = "stage")
    private List<Group> groups;
}
