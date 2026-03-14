package com.example.discordBot.commands;

import com.example.discordBot.LavaPlayer.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Play1Command implements Command
{
    @Override
    public String getName()
    {
        return "play1";
    }

    @Override
    public String getDescription()
    {
        return "Play music from SoundCloud";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();

        Guild guild = event.getGuild();
        if (guild == null)
        {
            event.getHook().sendMessage("❌ This command can only be used in a server.").queue();
            return;
        }

        String url = event.getOption("url") != null ? event.getOption("url").getAsString() : null;
        if (url == null || url.isBlank())
        {
            event.getHook().sendMessage("❌ Provide a SoundCloud link.").queue();
            return;
        }

        if (!url.contains("soundcloud.com"))
        {
            event.getHook().sendMessage("❌ This is not a valid SoundCloud link.").queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel())
        {
            event.getHook().sendMessage("❌ You must be in a voice channel.").queue();
            return;
        }

        AudioChannel memberChannel = member.getVoiceState().getChannel();
        if (memberChannel == null)
        {
            event.getHook().sendMessage("❌ Could not determine your voice channel.").queue();
            return;
        }

        guild.getAudioManager().openAudioConnection(memberChannel);

        PlayerManager.get().play(guild, url, event);
    }
}