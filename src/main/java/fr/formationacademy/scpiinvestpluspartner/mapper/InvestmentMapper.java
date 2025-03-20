package fr.formationacademy.scpiinvestpluspartner.mapper;

import fr.formationacademy.scpiinvestpluspartner.dto.ScpiRequestDto;
import fr.formationacademy.scpiinvestpluspartner.entity.Investment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvestmentMapper {
    Investment toEntity(ScpiRequestDto scpiRequestDto);
    ScpiRequestDto toDto(Investment investment);
}

