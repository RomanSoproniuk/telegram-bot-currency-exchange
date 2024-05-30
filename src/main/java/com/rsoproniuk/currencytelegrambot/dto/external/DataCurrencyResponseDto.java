package com.rsoproniuk.currencytelegrambot.dto.external;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class DataCurrencyResponseDto {
    private String code;
    private BigDecimal value;
}
