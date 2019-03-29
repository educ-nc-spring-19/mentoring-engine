package com.educ_nc_spring_19.stud_spreading_service.service.repo;

import com.educ_nc_spring_19.stud_spreading_service.model.entity.Stage;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface StageRepository extends CrudRepository<Stage, UUID> {
}
