package com.rsoproniuk.currencytelegrambot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:string.properties")
})
public class TelegramBotConfig {
    private final List<BotCommand> botCommandList = new ArrayList<>();
    @Value("${bot.start.command}")
    private String startCommand;
    @Value("${bot.help.command}")
    private String helpCommand;
    @Value("${bot.start.command.description}")
    private String startCommandDescription;
    @Value("${bot.help.command.description}")
    private String helpCommandDescription;
    @Value("${bot.token}")
    private String botToken;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(botToken);
    }

    @Bean
    public SetMyCommands setMyCommands() {
        botCommandList.add(new BotCommand(startCommand, startCommandDescription));
        botCommandList.add(new BotCommand(helpCommand, helpCommandDescription));
        return new SetMyCommands(botCommandList);
    }
}
