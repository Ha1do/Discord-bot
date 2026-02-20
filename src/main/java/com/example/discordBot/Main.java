package com.example.discordBot;

import com.example.discordBot.config.Config;
import com.example.discordBot.listeners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static JDA jda;

    public static void main(String[] args) {
        logger.info("Starting Discord Bot...");
        String token = Config.getBotToken();

        if (token == null || token.isEmpty())
        {
            logger.error("Bot token not found in configuration file!");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token).
                    enableIntents(EnumSet.allOf(GatewayIntent.class)).
                    addEventListeners(newCommandListener()).
                    build();

            jda.awaitReady();
            registerSlashCommands();
            logger.info("Discord Bot started successfully!");
        } catch (Exception e){
            logger.error("Failed to start Discord Bot: ", e);
        }
    }

    private static CommandListener newCommandListener()
    {
        return new CommandListener();
    }

    private static void registerSlashCommands()
    {
        if (jda == null)
        {
            logger.error("JDA instance is null! Can`t register slash commands!");
            return;
        }

        logger.info("Registering slash commands...");
        jda.updateCommands().addCommands(
                Commands.slash("ping", "Replies with Pong!"),
                Commands.slash("echo", "Replies with your message!").
                        addOption(OptionType.STRING, "text", "Message to echo", true),
                Commands.slash("info", "Replies with bot information!")
        ).queue(success -> logger.info("Slash commands registered successfully!"), failure -> logger.error("Failed to register slash commands: ", failure));
    }
}
