package com.example.discordBot.audio.player;

import com.example.discordBot.LavaPlayer.GuildMusicManager;
import com.example.discordBot.audio.ResolvedTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

abstract class AbstractLavaplayerSourcePlayer implements SourcePlayer
{
    @Override
    public void play(AudioPlayerManager audioPlayerManager,
                     GuildMusicManager guildMusicManager,
                     ResolvedTrack resolvedTrack,
                     SlashCommandInteractionEvent event)
    {
        String trackQuery = prepareTrackQuery(resolvedTrack);

        if (trackQuery == null || trackQuery.isBlank())
        {
            event.getHook().sendMessage("❌ Track query is empty.").queue();
            return;
        }

        audioPlayerManager.loadItemOrdered(guildMusicManager, trackQuery, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack audioTrack)
            {
                guildMusicManager.getScheduler().queue(audioTrack);
                event.getHook().sendMessage("🎵 Queued: **" + audioTrack.getInfo().title + "**").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist)
            {
                if (audioPlaylist.getTracks().isEmpty())
                {
                    event.getHook().sendMessage("❌ Playlist is empty.").queue();
                    return;
                }

                if (trackQuery.startsWith("ytsearch:"))
                {
                    AudioTrack firstTrack = audioPlaylist.getTracks().get(0);
                    guildMusicManager.getScheduler().queue(firstTrack);
                    event.getHook().sendMessage("🎵 Queued from search: **" + firstTrack.getInfo().title + "**").queue();
                    return;
                }

                audioPlaylist.getTracks().forEach(guildMusicManager.getScheduler()::queue);
                event.getHook().sendMessage(
                        "📋 Queued playlist: **" + audioPlaylist.getName() + "** with " + audioPlaylist.getTracks().size() + " tracks"
                ).queue();
            }

            @Override
            public void noMatches()
            {
                event.getHook().sendMessage("❌ No matches found for: " + trackQuery).queue();
            }

            @Override
            public void loadFailed(FriendlyException e)
            {
                event.getHook().sendMessage("❌ Failed to load track: " + e.getMessage()).queue();
            }
        });
    }

    protected String prepareTrackQuery(ResolvedTrack resolvedTrack)
    {
        return resolvedTrack.getTrackQuery();
    }
}

