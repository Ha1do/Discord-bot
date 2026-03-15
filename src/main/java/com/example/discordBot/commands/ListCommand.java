package com.example.discordBot.commands;

import com.example.discordBot.LavaPlayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class ListCommand implements Command
{
    private static final int MAX_VISIBLE_ITEMS = 10;

    @Override
    public String getName()
    {
        return "list";
    }

    @Override
    public String getDescription()
    {
        return "Shows current track and queue";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        Guild guild = event.getGuild();
        if (guild == null)
        {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        PlayerManager.QueueView queueView = PlayerManager.get().getQueueView(guild, MAX_VISIBLE_ITEMS);
        AudioTrack currentTrack = queueView.currentTrack();
        List<AudioTrack> upcoming = queueView.upcomingTracks();

        if (currentTrack == null && queueView.totalQueueSize() == 0)
        {
            event.reply("Queue is empty.").setEphemeral(true).queue();
            return;
        }

        StringBuilder message = new StringBuilder();
        if (currentTrack != null)
        {
            message.append("▶️ Now playing: **")
                    .append(currentTrack.getInfo().title)
                    .append("**\n");
        }
        else
        {
            message.append("▶️ Nothing is playing right now.\n");
        }

        if (queueView.totalQueueSize() > 0)
        {
            message.append("\n📋 Queue (")
                    .append(queueView.totalQueueSize())
                    .append("):\n");

            for (int i = 0; i < upcoming.size(); i++)
            {
                message.append(i + 1)
                        .append(". ")
                        .append(upcoming.get(i).getInfo().title)
                        .append("\n");
            }

            int hiddenCount = queueView.totalQueueSize() - upcoming.size();
            if (hiddenCount > 0)
            {
                message.append("... and ").append(hiddenCount).append(" more track(s)");
            }
        }

        event.reply(message.toString()).queue();
    }
}

