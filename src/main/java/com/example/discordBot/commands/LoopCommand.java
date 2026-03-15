package com.example.discordBot.commands;

import com.example.discordBot.LavaPlayer.PlayerManager;
import com.example.discordBot.LavaPlayer.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LoopCommand implements Command
{
    @Override
    public String getName()
    {
        return "loop";
    }

    @Override
    public String getDescription()
    {
        return "Sets loop mode: off, track or queue";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null)
        {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel())
        {
            event.reply("You need to be in a voice channel to use this command!").setEphemeral(true).queue();
            return;
        }

        GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        if (selfVoiceState == null || !selfVoiceState.inAudioChannel())
        {
            event.reply("I am not in a voice channel right now.").setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
        {
            event.reply("You need to be in the same voice channel as me to use this command!").setEphemeral(true).queue();
            return;
        }

        OptionMapping modeOption = event.getOption("mode");
        if (modeOption == null)
        {
            event.reply("Please provide loop mode: off, track or queue.").setEphemeral(true).queue();
            return;
        }

        TrackScheduler.LoopMode mode = parseMode(modeOption.getAsString());
        if (mode == null)
        {
            event.reply("Unknown mode. Use one of: off, track, queue.").setEphemeral(true).queue();
            return;
        }

        TrackScheduler.LoopMode activeMode = PlayerManager.get().setLoopMode(guild, mode);
        event.reply("Loop mode set to: **" + activeMode.name().toLowerCase() + "**").queue();
    }

    private TrackScheduler.LoopMode parseMode(String value)
    {
        if (value == null)
        {
            return null;
        }

        return switch (value.toLowerCase())
        {
            case "off" -> TrackScheduler.LoopMode.OFF;
            case "track" -> TrackScheduler.LoopMode.TRACK;
            case "queue" -> TrackScheduler.LoopMode.QUEUE;
            default -> null;
        };
    }
}

