package com.example.discordBot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class EchoCommand implements Command
{
    @Override
    public String getName()
    {
        return "echo";
    }

    @Override
    public String getDescription()
    {
        return "Replies with your message!";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        OptionMapping textOption = event.getOption("text");
        String textToEcho = "";
        if (textOption != null)
            textToEcho = textOption.getAsString();
        else
            textToEcho = "No text provided!";

        event.reply(textToEcho).setEphemeral(false).queue();
    }
}

