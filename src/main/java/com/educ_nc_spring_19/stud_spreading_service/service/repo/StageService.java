package com.educ_nc_spring_19.stud_spreading_service.service.repo;

import com.educ_nc_spring_19.stud_spreading_service.model.entity.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StageService {
    private final StageRepository stageRepository;

    public Optional<Stage> findById(UUID id) {
        return stageRepository.findById(id);
    }
}
