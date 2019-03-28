package com.educ_nc_spring_19.stud_spreading_service.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Entity
public class Group {
    @Id
    @GeneratedValue
    private UUID id;

    private String groupName;
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

    protected Group() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public UUID getMentorId() {
        return mentorId;
    }

    public void setMentorId(UUID mentorId) {
        this.mentorId = mentorId;
    }

    public UUID getBackupId() {
        return backupId;
    }

    public void setBackupId(UUID backupId) {
        this.backupId = backupId;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public OffsetDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(OffsetDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public UUID getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(UUID updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    public Collection<UUID> getStudents() {
        return students;
    }

    public void setStudents(Collection<UUID> students) {
        this.students = students;
    }
}
