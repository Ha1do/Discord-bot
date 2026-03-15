package com.example.discordBot;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import com.example.discordBot.config.Config;
import com.example.discordBot.listeners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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

        if (token == null || token.isEmpty()) {
            logger.error("Bot token not found in configuration file!");
            return;
        }

        try {

            AudioModuleConfig audioConfig = new AudioModuleConfig()
                    .withDaveSessionFactory(new JDaveSessionFactory());

            jda = JDABuilder.createDefault(token)
                    .enableIntents(EnumSet.of(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS
                    ))
                    .setAudioModuleConfig(audioConfig)
                    .addEventListeners(newCommandListener())
                    .build();

            jda.awaitReady();
            registerSlashCommands();

            logger.info("Discord Bot started successfully!");

        } catch (Exception e) {
            logger.error("Failed to start Discord Bot: ", e);
        }
    }

    private static CommandListener newCommandListener() {
        return new CommandListener();
    }

    private static void registerSlashCommands() {

        if (jda == null) {
            logger.error("JDA instance is null! Can't register slash commands!");
            return;
        }

        logger.info("Registering slash commands...");

        jda.updateCommands().addCommands(
                Commands.slash("ping", "Replies with Pong!"),
                Commands.slash("echo", "Replies with your message!")
                        .addOption(OptionType.STRING, "text", "Message to echo", true),
                Commands.slash("info", "Replies with bot information!"),
                Commands.slash("play", "Plays a song from YouTube!")
                        .addOption(OptionType.STRING, "url", "YouTube URL of the song to play", true),
                Commands.slash("skip", "Skips current track or clears queue")
                        .addOption(OptionType.BOOLEAN, "all", "Set true to stop and clear all queued tracks", false),
                Commands.slash("pause", "Pauses current playback"),
                Commands.slash("resume", "Resumes paused playback"),
                Commands.slash("loop", "Sets loop mode")
                        .addOptions(
                                new OptionData(OptionType.STRING, "mode", "off, track, or queue", true)
                                        .addChoice("off", "off")
                                        .addChoice("track", "track")
                                        .addChoice("queue", "queue")
                        ),
                Commands.slash("list", "Shows current track and queue")

        ).queue(
                success -> logger.info("Slash commands registered successfully!"),
                failure -> logger.error("Failed to register slash commands: ", failure)
        );
    }
}