package com.example.discordBot.commands;

import com.example.discordBot.LavaPlayer.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ResumeCommand implements Command
{
    @Override
    public String getName()
    {
        return "resume";
    }

    @Override
    public String getDescription()
    {
        return "Resumes paused playback";
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

        PlayerManager.ResumeResult result = PlayerManager.get().resume(guild);

        switch (result)
        {
            case NOTHING_PLAYING -> event.reply("There is no track playing right now.").setEphemeral(true).queue();
            case NOT_PAUSED -> event.reply("Playback is not paused.").setEphemeral(true).queue();
            case RESUMED -> event.reply("Playback resumed.").queue();
        }
    }
}

