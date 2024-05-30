package com.rsoproniuk.currencytelegrambot.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:string.properties")
})
@RequiredArgsConstructor
public class HandleMassageService {
    private static final String BASE_COMMAND_CODE = "base";
    @Value("${bot.start.command}")
    private String startCommand;
    @Value("${bot.help.command}")
    private String helpCommand;
    @Value("${bot.not.supported.command.description}")
    private String notSupportedCommand;
    @Value("${base.currency.select.text}")
    private String selectBaseCurrencyText;
    private final CurrencySaverService currencySaverService;


    public SendMessage handleTextFromUser(Long chatId, String textFromUser) {
        if (textFromUser.equals(startCommand)) {
            return handleStartCommand(chatId);
        } else if (textFromUser.equals(helpCommand)) {
            return handleHelpCommand(chatId);
        } else {
            return handleNoSupportCommand(chatId);
        }
    }

    private SendMessage handleNoSupportCommand(Long chatId) {
        return SendMessage.builder()
                .text(notSupportedCommand)
                .chatId(chatId)
                .build();
    }

    private SendMessage handleHelpCommand(Long chatId) {
        return null;
        //TODO handle help command
    }

    private SendMessage handleStartCommand(Long chatId) {
        String textToUser = """
                If you want to start working with the bot and find out currency rates, 
                please click CONTINUE, if you want to return to the main menu of the bot, 
                click BACK.
                """;
        InlineKeyboardButton continueButton = InlineKeyboardButton.builder()
                .callbackData("BACK_BUTTON")
                .text("BACK")
                .build();
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .callbackData("CONTINUE_BUTTON")
                .text("CONTINUE")
                .build();
        InlineKeyboardRow rowWithButtons = new InlineKeyboardRow();
        rowWithButtons.add(continueButton);
        rowWithButtons.add(backButton);
        List<InlineKeyboardRow> buttonsContinueAndBack = new ArrayList<>();
        buttonsContinueAndBack.add(rowWithButtons);
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(buttonsContinueAndBack)
                .build();
        return SendMessage.builder()
                .chatId(chatId)
                .text(textToUser)
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

//    private SendMessage handleStartCommand(Long chatId) {
//        List<String> availableCurrencies = Arrays.stream(Currencies.values())
//                .map(String::valueOf)
//                .toList();
//        List<InlineKeyboardRow> rows = new ArrayList<>();
//        for (int i = 0; i < availableCurrencies.size(); i += 3) {
//            InlineKeyboardRow row = new InlineKeyboardRow();
//            for (int j = i; j < Math.min(i + 3, availableCurrencies.size()); j++) {
//                InlineKeyboardButton button = InlineKeyboardButton.builder()
//                        .text(availableCurrencies.get(j))
//                        .callbackData(availableCurrencies.get(j) + BASE_COMMAND_CODE)
//                        .build();
//                row.add(button);
//            }
//            rows.add(row);
//        }
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);
//        return SendMessage.builder()
//                .text("Select the base currency")
//                .chatId(chatId)
//                .replyMarkup(markup)
//                .build();
//    }
}
