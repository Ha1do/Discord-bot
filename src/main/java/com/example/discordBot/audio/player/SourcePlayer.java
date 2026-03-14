package com.example.discordBot.audio.player;

import com.example.discordBot.LavaPlayer.GuildMusicManager;
import com.example.discordBot.audio.ResolvedTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SourcePlayer
{
    void play(AudioPlayerManager audioPlayerManager,
              GuildMusicManager guildMusicManager,
              ResolvedTrack resolvedTrack,
              SlashCommandInteractionEvent event);
}

