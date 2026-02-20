package com.example.discordBot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface Command
{
    String getName();

    String getDescription();

    void executeSlash(SlashCommandInteractionEvent event);
}