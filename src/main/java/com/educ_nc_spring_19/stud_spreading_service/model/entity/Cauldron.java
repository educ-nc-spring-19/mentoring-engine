package com.educ_nc_spring_19.stud_spreading_service.model.entity;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.CreatedUpdatedDateByUser;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data

@Entity
public class Cauldron {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String description;

    @Embedded
    private CreatedUpdatedDateByUser createdUpdatedDateByUser;

    @ElementCollection
    @CollectionTable(
            name = "cauldron_mentor",
            joinColumns = @JoinColumn(name = "cauldron_id")
    )
    @Column(name = "mentor_id", nullable = false)
    private List<UUID> mentors;
}
