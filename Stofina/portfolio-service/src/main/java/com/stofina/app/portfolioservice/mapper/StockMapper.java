package com.stofina.app.portfolioservice.mapper;

import com.stofina.app.portfolioservice.dto.StockDto;
import com.stofina.app.portfolioservice.model.Stock;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    StockDto toStockDto(Stock stock);
    List<StockDto> toStockDtoList(List<Stock> stocks);


}
