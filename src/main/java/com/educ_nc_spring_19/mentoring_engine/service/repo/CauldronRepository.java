package com.educ_nc_spring_19.mentoring_engine.service.repo;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface CauldronRepository extends CrudRepository<Cauldron, UUID> {
    Optional<Cauldron> findByStudentsIs(UUID studentId);
    Optional<Cauldron> findByMentorsIsAndStudentsIs(UUID mentorId, UUID studentId);
}
