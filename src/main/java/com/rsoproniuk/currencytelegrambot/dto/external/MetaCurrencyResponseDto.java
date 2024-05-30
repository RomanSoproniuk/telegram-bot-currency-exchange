package com.rsoproniuk.currencytelegrambot.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class MetaCurrencyResponseDto {
    @JsonProperty("last_update_at")
    private LocalDateTime lastUpdateAt;
}
