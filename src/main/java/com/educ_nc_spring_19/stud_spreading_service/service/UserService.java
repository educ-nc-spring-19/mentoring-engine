package com.educ_nc_spring_19.stud_spreading_service.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    public UUID getCurrentUserId() {
        return UUID.fromString("2134692f-a215-41c7-aff9-5755c83dbd5a");
    }
}
