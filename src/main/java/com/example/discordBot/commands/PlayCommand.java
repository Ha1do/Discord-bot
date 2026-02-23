package com.example.discordBot.commands;

import com.example.discordBot.LavaPlayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlayCommand implements Command
{
    @Override
    public String getName()
    {
        return "play";
    }

    @Override
    public String getDescription()
    {
        return "Bot connects to your voice channel and plays music";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        OptionMapping URL = event.getOption("URL");

        Member member = event.getMember();
        GuildVoiceState membervoiceState = member.getVoiceState();
        if (!membervoiceState.inAudioChannel())
        {
            event.reply("You need to be in a voice channel to use this command!").queue();
            return;
        }
        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel())
        {
            event.getGuild().getAudioManager().openAudioConnection(membervoiceState.getChannel());
        }
        else
        {
            if (selfVoiceState.getChannel() != membervoiceState.getChannel())
            {
                event.reply("You need to be in the same voice channel as me to use this command!").queue();
                return;
            }
        }

        PlayerManager playerManager = PlayerManager.get();
        event.reply("Playing " + URL.getAsString()).queue();
        playerManager.play(event.getGuild(), event.getOption("URL").getAsString());
    }

}
