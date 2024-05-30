package com.rsoproniuk.currencytelegrambot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
public class CurrencySaverService {
    private String baseCurrency;
    private List<String> currencies = new ArrayList<>();
}
