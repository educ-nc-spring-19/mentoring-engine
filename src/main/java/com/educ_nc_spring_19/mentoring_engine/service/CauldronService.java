package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import com.educ_nc_spring_19.mentoring_engine.service.repo.CauldronRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CauldronService {
    private final CauldronRepository cauldronRepository;

    public void deleteAll() {
        cauldronRepository.deleteAll();
    }

    public List<Cauldron> saveAll(Iterable<Cauldron> cauldrons) {
        return IterableUtils.toList(cauldronRepository.saveAll(cauldrons));
    }
}
