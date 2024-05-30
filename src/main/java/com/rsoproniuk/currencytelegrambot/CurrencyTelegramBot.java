package com.rsoproniuk.currencytelegrambot;

import com.rsoproniuk.currencytelegrambot.dto.external.CurrencyResponseDto;
import com.rsoproniuk.currencytelegrambot.dto.external.DataCurrencyResponseDto;
import com.rsoproniuk.currencytelegrambot.service.HandleMassageService;
import com.rsoproniuk.currencytelegrambot.service.CallbackQueryService;
import com.rsoproniuk.currencytelegrambot.service.CurrencyApiService;
import com.rsoproniuk.currencytelegrambot.service.CurrencySaverService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class CurrencyTelegramBot implements SpringLongPollingBot,
        LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final CurrencySaverService currencySaverService;
    private final CurrencyApiService currencyApiService;
    private final SetMyCommands setMyCommands;
    private final HandleMassageService commandHandleService;
    private final CallbackQueryService callbackQueryService;
    @Value("${bot.token}")
    private String botToken;

    public CurrencyTelegramBot(TelegramClient telegramClient,
                               CurrencySaverService currencySaverService,
                               CurrencyApiService currencyApiService,
                               HandleMassageService commandHandleService,
                               SetMyCommands setMyCommands,
                               CallbackQueryService callbackQueryService)
            throws TelegramApiException {
        this.callbackQueryService = callbackQueryService;
        this.commandHandleService = commandHandleService;
        this.currencyApiService = currencyApiService;
        this.currencySaverService = currencySaverService;
        this.telegramClient = telegramClient;
        this.setMyCommands = setMyCommands;
        telegramClient.execute(setMyCommands);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String textFromUser = update.getMessage().getText().trim();
            SendMessage sendMessage = commandHandleService.handleTextFromUser(chatId, textFromUser);
            telegramClient.execute(sendMessage);
        } else if (update.hasCallbackQuery()) {
            List<String> currenciesList = Arrays.stream(Currencies.values())
                    .map(Enum::toString)
                    .toList();
            String callbackData = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String currencyCode = callbackData.substring(0, 3);
            String codeButton = callbackData.substring(3);
            if (callbackData.startsWith("BACK")) {
                EditMessageText editMessageText = handleShowResultButton(messageId, chatId);
                telegramClient.execute(editMessageText);
            } else if (callbackData.equals(callbackData + "SOME")) {
                handleBackButton(messageId, chatId);
            } else if (currenciesList.contains(currencyCode) && codeButton.equals("base")) {
                EditMessageText editMessageText = handleSelectBaseCurrencies(messageId, chatId);
                currencySaverService.setBaseCurrency(currencyCode);
                telegramClient.execute(editMessageText);
            } else if (currenciesList.contains(currencyCode) && codeButton.equals("additional")) {
                EditMessageText editMessageText = handleSelectAdditionalCurrencies(messageId, chatId);
                currencySaverService.getCurrencies().add(currencyCode);
                telegramClient.execute(editMessageText);
            } else if (callbackData.equals("CONTINUE_BUTTON")) {
                EditMessageText continueButtonAnswer = handleContinueButton(messageId, chatId);
                telegramClient.execute(continueButtonAnswer);
            }
        }
    }

    private EditMessageText handleSelectAdditionalCurrencies(Integer messageId, Long chatId) {
        return handleCommandsButton(messageId, chatId, "additional", "Do you want more currencies?");
    }

    private EditMessageText handleContinueButton(Integer messageId, Long chatId) {
        return handleCommandsButton(messageId, chatId, "base", "Select the base currency");
    }

    private EditMessageText handleSelectBaseCurrencies(Integer messageId,
                                                       Long chatId) {
        return handleCommandsButton(messageId, chatId, "additional", "Select the currencies for which you want to see the exchange rate");
    }

    private static EditMessageText handleCommandsButton(Integer messageId, Long chatId, String additionalCode, String textToUser) {
        List<String> availableCurrencies = Arrays.stream(Currencies.values())
                .map(String::valueOf)
                .toList();
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < availableCurrencies.size(); i += 3) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            for (int j = i; j < Math.min(i + 3, availableCurrencies.size()); j++) {
                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(availableCurrencies.get(j))
                        .callbackData(availableCurrencies.get(j) + additionalCode)
                        .build();
                row.add(button);
            }
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);
        return EditMessageText.builder()
                .messageId(messageId)
                .text(textToUser)
                .chatId(chatId)
                .replyMarkup(markup)
                .build();
    }

    private void handleBackButton(Integer messageId, Long chatId) {
        //TODO handle back button - if user want come back to main menu
    }


    private EditMessageText handleShowResultButton(Integer messageId, Long chatId) throws IOException, InterruptedException {
        StringBuilder answerToUser = new StringBuilder();
        answerToUser.append("Result: \n");
        String baseCurrency = currencySaverService.getBaseCurrency();
        List<String> currenciesSelectedByUserList = currencySaverService.getCurrencies();
        CurrencyResponseDto currenciesRateResponseDto
                = currencyApiService.getCurrencyRate(baseCurrency, currenciesSelectedByUserList);
        Map<String, DataCurrencyResponseDto> codeValueCurrenciesMap = currenciesRateResponseDto.getData();
        List<DataCurrencyResponseDto> dataCurrenciesList = codeValueCurrenciesMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .toList();
        for (DataCurrencyResponseDto dataCurrencyResponseDto : dataCurrenciesList) {
            String currency = dataCurrencyResponseDto.getCode();
            BigDecimal value = dataCurrencyResponseDto.getValue();
            answerToUser.append(baseCurrency + " : " + currency + " - 1 : " + value + "\n");
        }
        return EditMessageText.builder()
                .text(answerToUser.toString())
                .messageId(messageId)
                .chatId(chatId)
                .build();
    }
}