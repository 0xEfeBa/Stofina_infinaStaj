package com.stofina.app.portfolioservice.mapper;

import com.stofina.app.portfolioservice.dto.BalanceReservationDto;
import com.stofina.app.portfolioservice.model.BalanceReservation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BalanceReservationMapper {

    BalanceReservationDto toDto(BalanceReservation reservation);
}
