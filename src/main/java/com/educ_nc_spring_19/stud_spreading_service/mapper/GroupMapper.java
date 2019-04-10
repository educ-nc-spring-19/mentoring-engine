package com.educ_nc_spring_19.stud_spreading_service.mapper;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.GroupDTO;
import com.educ_nc_spring_19.stud_spreading_service.model.entity.Group;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface GroupMapper {
    GroupDTO toGroupDTO(Group group);
    List<GroupDTO> toGroupsDTO(List<Group> groups);
    Group toGroup(GroupDTO groupDTO);
}
