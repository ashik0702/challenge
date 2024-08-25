package com.dws.challenge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    @NotNull
    private Long accountFromId;
    @NotNull
    private Long accountToId;
    @NotNull
    private BigDecimal amount;


    @Override
    public String toString() {
        return "TransferRequest{" +
                "accountFromId=" + accountFromId +
                ", accountToId=" + accountToId +
                ", amount=" + amount +
                '}';
    }

}
