package com.educ_nc_spring_19.stud_spreading_service.mapper;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.TeamDTO;
import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface TeamMapper {
    @Mappings({
            @Mapping(target = "students", source = "studentDTOS")
    })
    TeamDTO toTeamDTO(Group group, List<StudentDTO> studentDTOS);

   ArrayList<TeamDTO> toTeamsDTO(ArrayList<Group> groups, ArrayList<List<StudentDTO>> studentDTOS);

    @Mappings({
            @Mapping(target = "students",
                    expression = "java(teamDTO.getStudents().stream().map(StudentDTO::getId).collect(java.util.stream.Collectors.toList()))")
    })
    Group toGroup(TeamDTO teamDTO);
}
