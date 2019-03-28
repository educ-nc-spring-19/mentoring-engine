package com.educ_nc_spring_19.stud_spreading_service.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Pool {
    @Id
    private UUID studentId;

    protected Pool() {
    }

    public Pool(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }
}
