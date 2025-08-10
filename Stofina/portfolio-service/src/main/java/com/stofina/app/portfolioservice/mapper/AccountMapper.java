package com.stofina.app.portfolioservice.mapper;

import com.stofina.app.portfolioservice.dto.AccountDto;
import com.stofina.app.portfolioservice.model.Account;
import com.stofina.app.portfolioservice.request.account.CreateAccountRequest;
import com.stofina.app.portfolioservice.request.account.PatchAccountStatusRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { StockMapper.class })
public interface AccountMapper {

    AccountDto toAccountDto(Account account);

    Account toEntity(CreateAccountRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchStatus(@MappingTarget Account account, PatchAccountStatusRequest request);
}
