package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.mentoring_engine.service.repo.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
public class LectureService {
    private final LectureRepository lectureRepository;


}
