package com.example.discordBot.LavaPlayer;

import com.example.discordBot.audio.ResolvedTrack;
import com.example.discordBot.audio.SourceType;
import com.example.discordBot.audio.player.SearchSourcePlayer;
import com.example.discordBot.audio.player.SoundCloudSourcePlayer;
import com.example.discordBot.audio.player.SourcePlayer;
import com.example.discordBot.audio.player.YouTubeSourcePlayer;
import com.example.discordBot.config.Config;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.Android;
import dev.lavalink.youtube.clients.AndroidMusic;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.Ios;
import dev.lavalink.youtube.clients.MWeb;
import dev.lavalink.youtube.clients.Music;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.EnumMap;
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
    private final Map<SourceType, SourcePlayer> sourcePlayers = new EnumMap<>(SourceType.class);
    private final YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(
            new Music(),
            new AndroidMusic(),
            new MWeb(),
            new Ios(),
            new Android(),
            new AndroidVr()
    );

    private PlayerManager()
    {
        String youtubeRefreshToken = Config.getOptionalValue("YOUTUBE_REFRESH_TOKEN", "youtubeRefreshToken");
        if (youtubeRefreshToken != null)
        {
            youtubeAudioSourceManager.useOauth2(youtubeRefreshToken, true);
        }

        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        registerSourcePlayers();
    }

    private void registerSourcePlayers()
    {
        SourcePlayer youtubePlayer = new YouTubeSourcePlayer();
        sourcePlayers.put(SourceType.YOUTUBE, youtubePlayer);
        sourcePlayers.put(SourceType.YOUTUBE_MUSIC, youtubePlayer);
        sourcePlayers.put(SourceType.SOUNDCLOUD, new SoundCloudSourcePlayer());
        sourcePlayers.put(SourceType.SEARCH, new SearchSourcePlayer());
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
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), ignoredGuildId -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager);
            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());
            return musicManager;
        });
    }

    public void play(Guild guild, ResolvedTrack resolvedTrack, SlashCommandInteractionEvent event)
    {
        if (guild == null)
        {
            event.getHook().sendMessage("❌ Guild is null.").queue();
            return;
        }

        if (resolvedTrack == null || resolvedTrack.getTrackQuery() == null || resolvedTrack.getTrackQuery().isBlank())
        {
            event.getHook().sendMessage("❌ Track query is empty.").queue();
            return;
        }

        SourcePlayer sourcePlayer = sourcePlayers.get(resolvedTrack.getSourceType());
        if (sourcePlayer == null)
        {
            event.getHook().sendMessage("❌ This source is not implemented yet: " + resolvedTrack.getSourceType()).queue();
            return;
        }

        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        sourcePlayer.play(audioPlayerManager, guildMusicManager, resolvedTrack, event);
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