package com.example.discordBot.LavaPlayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager
{
    public enum SkipResult
    {
        NOTHING_PLAYING,
        SKIPPED_TO_NEXT,
        STOPPED
    }

    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    private PlayerManager()
    {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager get()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild)
    {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager);
            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());
            return musicManager;
        });
    }

    public void play(Guild guild, String trackQuery, SlashCommandInteractionEvent event)
    {
        if (guild == null)
        {
            event.getHook().sendMessage("❌ Guild is null.").queue();
            return;
        }

        if (trackQuery == null || trackQuery.isBlank())
        {
            event.getHook().sendMessage("❌ Track query is empty.").queue();
            return;
        }

        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);

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

    public SkipResult skip(Guild guild)
    {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);

        if (guildMusicManager.getScheduler().getPlayer().getPlayingTrack() == null)
        {
            return SkipResult.NOTHING_PLAYING;
        }

        AudioTrack nextTrack = guildMusicManager.getScheduler().skipCurrentTrack();
        return nextTrack == null ? SkipResult.STOPPED : SkipResult.SKIPPED_TO_NEXT;
    }
}