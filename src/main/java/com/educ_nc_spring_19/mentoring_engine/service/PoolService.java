package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.service.repo.PoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class PoolService {
    private final PoolRepository poolRepository;

    public List<Pool> findAll() {
        List<Pool> pools = IterableUtils.toList(poolRepository.findAll());

        log.log(Level.DEBUG,
                "Pools found by findAll(): " + pools.stream().map(Pool::getId).collect(Collectors.toList()));
        return pools;
    }

    public List<Pool> findAllById(Iterable<UUID> ids) {
        List<Pool> pools = IterableUtils.toList(poolRepository.findAllById(ids));

        log.log(Level.DEBUG,
                "Pools found by findAllById(): " + pools.stream().map(Pool::getId).collect(Collectors.toList()));
        return pools;
    }

    public Optional<Pool> findById(UUID id) {
        Optional<Pool> optionalPool = poolRepository.findById(id);
        optionalPool.ifPresent(pool ->
                log.log(Level.DEBUG, "Pool(id=" + pool.getId() + ") found by id"));
        return optionalPool;
    }

    public void deleteAll() {
        poolRepository.deleteAll();
        log.log(Level.INFO, "All Pools deleted");
    }

    public List<Pool> saveAll(Iterable<Pool> pools) {
        return IterableUtils.toList(poolRepository.saveAll(pools));
    }

    public Pool save(Pool pool) {
        return poolRepository.save(pool);
    }

    public Optional<Pool> findByDirectionId(UUID directionId) {
        Optional<Pool> optionalPool = poolRepository.findByDirectionId(directionId);
        optionalPool.ifPresent(pool ->
                log.log(Level.DEBUG,"Pool(id=" + pool.getId() + ") found by Direction(id=" + directionId + ")"));
        return optionalPool;
    }

    public Optional<Pool> findByDirectionIdAndStudentsIs(UUID directionId, UUID studentId) {
        Optional<Pool> optionalPool = poolRepository.findByDirectionIdAndStudentsIs(directionId, studentId);
        optionalPool.ifPresent(pool ->
                log.log(Level.DEBUG,"Pool(id=" + pool.getId() + ") found by Direction(id=" + directionId
                        + ") and Student(id=" + studentId + ")"));
        return optionalPool;
    }
}
