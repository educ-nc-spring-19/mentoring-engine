package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.service.repo.PoolRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PoolService {
    private final PoolRepository poolRepository;

    public void deleteAll() {
        poolRepository.deleteAll();
    }

    public List<Pool> saveAll(Iterable<Pool> pools) {
        return IterableUtils.toList(poolRepository.saveAll(pools));
    }

    public Pool save(Pool pool) {
        return poolRepository.save(pool);
    }

    public Optional<Pool> findByDirectionId(UUID directionId) {
        return poolRepository.findByDirectionId(directionId);
    }

    public Optional<Pool> findByDirectionIdAndStudentsIs(UUID directionId, UUID studentId) {
        return poolRepository.findByDirectionIdAndStudentsIs(directionId, studentId);
    }
}
