package com.educ_nc_spring_19.mentoring_engine.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Data

@Entity
public class Pool {
    @Id
    private UUID id;

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
