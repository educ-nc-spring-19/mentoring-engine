package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Stage;
import com.educ_nc_spring_19.mentoring_engine.service.repo.StageRepository;
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

    public Optional<Stage> findByOrder(Long order) {
        return stageRepository.findByOrder(order);
    }
}
