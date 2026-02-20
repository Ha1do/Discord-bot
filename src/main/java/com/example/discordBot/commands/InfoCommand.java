package com.example.discordBot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class InfoCommand implements Command
{
    @Override
    public String getName()
    {
        return "info";
    }

    @Override
    public String getDescription()
    {
        return "Replies with bot information!";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bot information");
        embed.setDescription("This bot was created by Ha1do");
        embed.setColor(new Color(148,0,211));
        embed.addField("Author", "text", false);
        embed.addField("Language", "Java", false);
        embed.addField("Lib", "JDA (Java DS API)", false);

        MessageEmbed message = embed.build();
        event.replyEmbeds(message).queue();
    }
}

