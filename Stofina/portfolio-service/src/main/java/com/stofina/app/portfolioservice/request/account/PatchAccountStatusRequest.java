package com.stofina.app.portfolioservice.request.account;

import com.stofina.app.portfolioservice.enums.AccountStatus;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchAccountStatusRequest {

    @NotNull(message = "New account status is required")
    private AccountStatus newStatus;
}
