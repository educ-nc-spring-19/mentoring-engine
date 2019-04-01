package com.educ_nc_spring_19.stud_spreading_service.model.entity;


import com.educ_nc_spring_19.educ_nc_spring_19_common.common.CreatedUpdatedDateByUser;
import lombok.Data;

import javax.persistence.*;
import java.util.Collection;
import java.util.UUID;

@Data

@Entity
@Table(name = "spr_group")
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

    @Embedded
    private CreatedUpdatedDateByUser createdUpdatedDateByUser;

    @ElementCollection
    @CollectionTable(
            name = "group_student",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @Column(name = "student_id", nullable = false)
    private Collection<UUID> students;
}
