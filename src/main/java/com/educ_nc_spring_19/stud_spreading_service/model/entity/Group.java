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
@Table(name = "spr_group")
public class Group implements Auditable {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private UUID mentorId;
    private UUID backupId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(name = "stage_id", insertable = false, updatable = false)
    private UUID stageId;

    @Embedded
    private Audit audit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "group_student",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @Column(name = "student_id", nullable = false)
    private List<UUID> students;
}
