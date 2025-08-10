package com.stofina.app.portfolioservice.request.reservation;


import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseReservationRequest {

    @NotNull
    private Long reservationId;

    private String reason;
}
