package com.educ_nc_spring_19.stud_spreading_service.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;
import java.util.UUID;

@Data

@Entity
public class Pool {
    @Id
    private UUID id;

    @ElementCollection
    @CollectionTable(
            name = "pool_student",
            joinColumns = @JoinColumn(name = "pool_id")
    )
    @Column(name = "student_id", nullable = false)
    private Collection<UUID> students;
}
