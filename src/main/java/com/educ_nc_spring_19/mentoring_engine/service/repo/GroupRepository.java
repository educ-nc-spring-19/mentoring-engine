package com.educ_nc_spring_19.mentoring_engine.service.repo;

import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends CrudRepository<Group, UUID> {
    Iterable<Group> findAllByStageId(UUID stageId);
    Optional<Group> findByMentorId(UUID mentorId);
    
    @Query(value = "select _group.id, _group.name, _group.mentor_id, _group.backup_id, _group.stage_id, _group.created_date, _group.created_by_user_id, _group.updated_date, _group.updated_by_user_id " +
            "from mentoring_engine.spr_group _group " +
            "left join mentoring_engine.group_student _group_student on _group_student.group_id = _group.id " +
            "where _group_student.student_id = :studentId",
            nativeQuery = true)
    Optional<Group> findByStudentsIs(@Param("studentId") UUID studentId);
}
