package com.educ_nc_spring_19.mentoring_engine.service.repo;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Stage;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface StageRepository extends CrudRepository<Stage, UUID> {
}
