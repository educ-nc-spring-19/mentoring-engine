package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.DayOfWeekTime;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Lecture;
import com.educ_nc_spring_19.mentoring_engine.service.repo.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class LectureService {
    private final LectureRepository lectureRepository;

    public Lecture addLectureDayTime(UUID lectureId, DayOfWeek day, OffsetTime time)
            throws IllegalArgumentException, NoSuchElementException {
        if (lectureId == null) {
            String errorMessage = "Provided id is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (day == null) {
            String errorMessage = "Provided day is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (time == null) {
            String errorMessage = "Provided time is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Optional<Lecture> optionalLecture = lectureRepository.findById(lectureId);
        if (optionalLecture.isPresent()) {
            Lecture lecture = optionalLecture.get();
            Set<DayOfWeekTime> lectureDays = lecture.getLectureDays();
            if (lectureDays.stream().map(DayOfWeekTime::getDay).anyMatch(day::equals)) {
                String errorMessage = "Provided day '" + day.name() + "' is currently in Lecture(id" + lectureId + ")'s days";
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            lectureDays.add(new DayOfWeekTime(day, time));
            // saving updated lecture
            lecture = lectureRepository.save(lecture);
            log.log(Level.INFO, "DayOfWeekTime(day=" + day.name()
                    + ", time=" + time + ") added to Lecture(id=" + lecture.getId() + ")");
            return lecture;
        } else {
            String errorMessage = "Lecture(id=" + lectureId + ") doesn't exist";
            log.log(Level.WARN, errorMessage);
            throw new NoSuchElementException(errorMessage);
        }
    }

    public void deleteAll() {
        lectureRepository.deleteAll();
        log.log(Level.INFO, "All Lectures deleted");
    }

    public Lecture deleteLectureDay(UUID lectureId, DayOfWeek day)
            throws IllegalArgumentException, NoSuchElementException {
        if (lectureId == null) {
            String errorMessage = "Provided id is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (day == null) {
            String errorMessage = "Provided day is null";
            log.log(Level.WARN, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Optional<Lecture> optionalLecture = lectureRepository.findById(lectureId);
        if (optionalLecture.isPresent()) {
            Lecture lecture = optionalLecture.get();
            Set<DayOfWeekTime> lectureDays = lecture.getLectureDays();
            if (CollectionUtils.isEmpty(lectureDays) ||  lectureDays.stream().map(DayOfWeekTime::getDay).noneMatch(day::equals)) {
                String errorMessage = "Provided day '" + day.name() + "' is absent in Lecture(id" + lectureId + ")'s days";
                log.log(Level.WARN, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            lectureDays.removeIf(lDay -> lDay.getDay().equals(day));

            // saving updated lecture
            lecture = lectureRepository.save(lecture);
            log.log(Level.INFO, "DayOfWeek(day=" + day.name()
                    + ") removed from Lecture(id=" + lecture.getId() + ")");
            return lecture;
        } else {
            String errorMessage = "Lecture(id=" + lectureId + ") doesn't exist";
            log.log(Level.WARN, errorMessage);
            throw new NoSuchElementException(errorMessage);
        }
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

    public Optional<Lecture> findByDirectionId(UUID directionId) {
        Optional<Lecture> optionalLecture = lectureRepository.findByDirectionId(directionId);
        optionalLecture.ifPresent(lecture ->
                log.log(Level.DEBUG, "Lecture(id=" + lecture.getId() + ") found by Direction(id=" + directionId + ")"));
        return optionalLecture;
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
