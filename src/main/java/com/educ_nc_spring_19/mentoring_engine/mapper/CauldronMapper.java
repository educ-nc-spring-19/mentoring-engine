package com.educ_nc_spring_19.mentoring_engine.mapper;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.CauldronDTO;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CauldronMapper {
    Cauldron toCauldron(CauldronDTO cauldronDTO);
    CauldronDTO toCauldronDTO(Cauldron cauldron);
    List<CauldronDTO> toCauldronsDTO(List<Cauldron> cauldrons);
}
