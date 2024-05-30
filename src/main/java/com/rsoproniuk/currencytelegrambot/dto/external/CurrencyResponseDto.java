package com.rsoproniuk.currencytelegrambot.dto.external;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class CurrencyResponseDto {
    private MetaCurrencyResponseDto meta;
    private Map<String, DataCurrencyResponseDto> data;
}
