package com.stofina.app.portfolioservice.mapper;


import com.stofina.app.portfolioservice.dto.WithdrawalRestrictionDto;
import com.stofina.app.portfolioservice.model.WithdrawalRestriction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WithdrawalRestrictionMapper {

    WithdrawalRestrictionDto toDto(WithdrawalRestriction restriction);
}
