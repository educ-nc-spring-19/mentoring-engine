package com.educ_nc_spring_19.stud_spreading_service.repository;

import com.educ_nc_spring_19.stud_spreading_service.entity.Cauldron;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CauldronRepository extends CrudRepository<Cauldron, UUID> {
}
