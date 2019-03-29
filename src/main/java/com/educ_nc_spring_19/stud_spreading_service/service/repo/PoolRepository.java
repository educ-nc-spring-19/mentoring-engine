package com.educ_nc_spring_19.stud_spreading_service.service.repo;

import com.educ_nc_spring_19.stud_spreading_service.model.entity.Pool;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PoolRepository extends CrudRepository<Pool, UUID> {
}
