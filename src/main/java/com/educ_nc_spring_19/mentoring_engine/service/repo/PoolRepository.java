package com.educ_nc_spring_19.mentoring_engine.service.repo;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface PoolRepository extends CrudRepository<Pool, UUID> {
    Optional<Pool> findByDirectionId(UUID directionId);
    Optional<Pool> findByDirectionIdAndStudentsIs(UUID directionId, UUID studentId);
}
