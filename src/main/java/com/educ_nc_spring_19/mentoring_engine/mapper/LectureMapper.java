package com.educ_nc_spring_19.mentoring_engine.mapper;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.LectureDTO;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Lecture;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface LectureMapper {
    Lecture toLecture(LectureDTO lectureDTO);
    LectureDTO toLectureDTO(Lecture lecture);
    List<LectureDTO> toLecturesDTO(List<Lecture> lectures);
}
