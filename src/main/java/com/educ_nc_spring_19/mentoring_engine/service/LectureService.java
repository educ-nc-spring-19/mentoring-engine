package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Lecture;
import com.educ_nc_spring_19.mentoring_engine.service.repo.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class LectureService {
    private final LectureRepository lectureRepository;

    public void deleteAll() {
        lectureRepository.deleteAll();
        log.log(Level.INFO, "All Lectures deleted");
    }

    public Lecture save(Lecture lecture) {
        return lectureRepository.save(lecture);
    }

    public List<Lecture> saveAll(Iterable<Lecture> lectures) {
        return IterableUtils.toList(lectureRepository.saveAll(lectures));
    }
}
