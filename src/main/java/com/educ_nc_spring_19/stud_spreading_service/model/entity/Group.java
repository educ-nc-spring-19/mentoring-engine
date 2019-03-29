package com.educ_nc_spring_19.stud_spreading_service.model.entity;


import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Data

@Entity
public class Group {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private UUID mentorId;
    private UUID backupId;

    @ManyToOne
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdDate;
    private UUID createdByUserId;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime updatedDate;
    private UUID updatedByUserId;

    @ElementCollection
    @CollectionTable(
            name = "group_student",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @Column(name = "student_id", nullable = false)
    private Collection<UUID> students;
}
