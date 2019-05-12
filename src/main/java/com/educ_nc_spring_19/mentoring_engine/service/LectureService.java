package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Lecture;
import com.educ_nc_spring_19.mentoring_engine.service.repo.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class LectureService {
    private final LectureRepository lectureRepository;
    private final MasterDataClient masterDataClient;

    public void deleteAll() {
        lectureRepository.deleteAll();
        log.log(Level.INFO, "All Lectures deleted");
    }

    public List<Lecture> findAll() {
        List<Lecture> lectures = IterableUtils.toList(lectureRepository.findAll());

        log.log(Level.DEBUG,
                "Groups found by findAll(): " + lectures.stream().map(Lecture::getId).collect(Collectors.toList()));
        return lectures;
    }

    public List<Lecture> findAllById(Iterable<UUID> ids) {
        List<Lecture> lectures = IterableUtils.toList(lectureRepository.findAllById(ids));

        log.log(Level.DEBUG,
                "Groups found by findAllById(): " + lectures.stream().map(Lecture::getId).collect(Collectors.toList()));
        return lectures;
    }

    public Optional<Lecture> findById(UUID id) {
        Optional<Lecture> optionalLecture = lectureRepository.findById(id);
        optionalLecture.ifPresent(lecture ->
                log.log(Level.DEBUG, "Lecture(id=" + lecture.getId() + ") found by id"));
        return optionalLecture;
    }

    public Lecture save(Lecture lecture) {
        return lectureRepository.save(lecture);
    }

    public List<Lecture> saveAll(Iterable<Lecture> lectures) {
        return IterableUtils.toList(lectureRepository.saveAll(lectures));
    }

    public Lecture setFirstLectureDay(UUID id, OffsetDateTime date)
            throws IllegalArgumentException, NoSuchElementException {
        if (id == null) {
            String errorMessage = "Provided id is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (date == null) {
            String errorMessage = "Provided date is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (date.isBefore(OffsetDateTime.now())) {
            String errorMessage = "Provided date '" + date + "' is earlier than current date-time";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Optional<Lecture> optionalLecture = lectureRepository.findById(id);
        if (optionalLecture.isPresent()) {
            Lecture lecture = optionalLecture.get();

            // setting first lecture date
            lecture.setFirstLecture(date);
            // saving updated lecture
            lecture = lectureRepository.save(lecture);
            log.log(Level.INFO, "Lecture(id=" + lecture.getId() + ")'s first lecture date set to '"
                    + lecture.getFirstLecture() + "'");
            return lecture;
        } else {
            String errorMessage = "Lecture(id=" + id + ") doesn't exist";
            log.log(Level.WARN, errorMessage);
            throw new NoSuchElementException(errorMessage);
        }
    }
}
