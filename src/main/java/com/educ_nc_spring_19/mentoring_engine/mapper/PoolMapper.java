package com.educ_nc_spring_19.mentoring_engine.mapper;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.PoolDTO;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface PoolMapper {
    PoolDTO toPoolDTO(Pool pool);
    List<PoolDTO> toPoolsDTO(List<Pool> pools);
    Pool toPool(PoolDTO poolDTO);
}
