package com.educ_nc_spring_19.mentoring_engine.mapper;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.TeamDTO;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mapper
public interface TeamMapper {
    @Mappings({
            @Mapping(target = "students", source = "studentDTOS")
    })
    TeamDTO toTeamDTO(Group group, List<StudentDTO> studentDTOS);

   default List<TeamDTO> toTeamsDTO(Map<Group, List<StudentDTO>>  groupsStudents) {
       if (groupsStudents == null) {
           return null;
       }

       List<TeamDTO> teamDTOList = new LinkedList<>();

       for (Map.Entry<Group, List<StudentDTO>> groupStudentsEntry : groupsStudents.entrySet()) {
           Group group = groupStudentsEntry.getKey();
           List<StudentDTO> studentDTOS = groupStudentsEntry.getValue();

           // evade of empty node addition
           if (group == null && studentDTOS == null) {
               continue;
           }

           teamDTOList.add(toTeamDTO(group, studentDTOS));
       }

       return teamDTOList;
   }

    @Mappings({
            @Mapping(target = "students",
                    expression = "java(teamDTO.getStudents().stream().map(StudentDTO::getId).collect(java.util.stream.Collectors.toList()))")
    })
    Group toGroup(TeamDTO teamDTO);
}
