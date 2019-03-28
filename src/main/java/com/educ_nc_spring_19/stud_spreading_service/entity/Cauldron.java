package com.educ_nc_spring_19.stud_spreading_service.entity;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Entity
public class Cauldron {
    @Id
    @GeneratedValue
    private UUID id;

    private String cauldronName;
    private String cauldronDescription;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdDate;
    private UUID createdByUserId;

    @Column(columnDefinition = "timestamp with time zone")
    private OffsetDateTime updatedDate;
    private UUID updatedByUserId;

    @ElementCollection
    @CollectionTable(
            name = "cauldron_mentor",
            joinColumns = @JoinColumn(name = "cauldron_id")
    )
    @Column(name = "mentor_id", nullable = false)
    private Collection<UUID> mentors;

    protected Cauldron() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCauldronName() {
        return cauldronName;
    }

    public void setCauldronName(String cauldronName) {
        this.cauldronName = cauldronName;
    }

    public String getCauldronDescription() {
        return cauldronDescription;
    }

    public void setCauldronDescription(String cauldronDescription) {
        this.cauldronDescription = cauldronDescription;
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

    public Collection<UUID> getMentors() {
        return mentors;
    }

    public void setMentors(Collection<UUID> mentors) {
        this.mentors = mentors;
    }
}
