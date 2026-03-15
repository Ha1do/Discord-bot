package com.example.discordBot.LavaPlayer;

import com.example.discordBot.audio.ResolvedTrack;
import com.example.discordBot.audio.SourceType;
import com.example.discordBot.audio.player.SearchSourcePlayer;
import com.example.discordBot.audio.player.SoundCloudSourcePlayer;
import com.example.discordBot.audio.player.SourcePlayer;
import com.example.discordBot.audio.player.YouTubeSourcePlayer;
import com.example.discordBot.config.Config;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.TvHtml5Simply;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.WebEmbedded;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager
{
    private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);

    public enum SkipResult
    {
        NOTHING_PLAYING,
        SKIPPED_TO_NEXT,
        STOPPED,
        CLEARED_ALL
    }

    public enum PauseResult
    {
        NOTHING_PLAYING,
        ALREADY_PAUSED,
        PAUSED
    }

    public enum ResumeResult
    {
        NOTHING_PLAYING,
        NOT_PAUSED,
        RESUMED
    }

    public record QueueView(AudioTrack currentTrack, List<AudioTrack> upcomingTracks, int totalQueueSize)
    {
    }

    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final Map<SourceType, SourcePlayer> sourcePlayers = new EnumMap<>(SourceType.class);
    private final YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(
            new Music(),       // YouTube Music content
            new Web(),         // standard YouTube, most reliable
            new WebEmbedded(), // embedded player fallback
            new TvHtml5Simply(), // TV client, fewer bot restrictions
            new AndroidVr()    // last resort
    );

    private PlayerManager()
    {
        String youtubeRefreshToken = normalizeRefreshToken(
                Config.getYoutubeRefreshToken()
        );

        if (youtubeRefreshToken != null)
        {
            try
            {
                // false = initialize token flow immediately, so config issues are visible at startup.
                youtubeAudioSourceManager.useOauth2(youtubeRefreshToken, false);
                logger.info("YouTube OAuth2 is enabled via refresh token.");
            }
            catch (Exception e)
            {
                logger.warn("Failed to initialize YouTube OAuth2 refresh token, continuing without OAuth2.", e);
            }
        }
        else
        {
            logger.info("YouTube OAuth2 refresh token is not configured; some videos may require login.");
        }

        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        registerSourcePlayers();
    }

    private String normalizeRefreshToken(String token)
    {
        if (token == null)
        {
            return null;
        }

        String normalized = token.trim();
        if (normalized.isEmpty())
        {
            return null;
        }

        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'")))
        {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        // Access token passed by mistake: lavalink expects refresh token here.
        if (normalized.regionMatches(true, 0, "Bearer ", 0, 7))
        {
            logger.warn("YOUTUBE_REFRESH_TOKEN looks like an access token (Bearer ...). Provide a refresh token instead.");
            return null;
        }

        return normalized.isEmpty() ? null : normalized;
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

    public SkipResult skip(Guild guild, boolean skipAll)
    {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        TrackScheduler scheduler = guildMusicManager.getScheduler();

        if (skipAll)
        {
            return scheduler.skipAllTracks() ? SkipResult.CLEARED_ALL : SkipResult.NOTHING_PLAYING;
        }

        if (scheduler.getPlayer().getPlayingTrack() == null)
        {
            return SkipResult.NOTHING_PLAYING;
        }

        AudioTrack nextTrack = scheduler.skipCurrentTrack();
        return nextTrack == null ? SkipResult.STOPPED : SkipResult.SKIPPED_TO_NEXT;
    }

    public PauseResult pause(Guild guild)
    {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        TrackScheduler scheduler = guildMusicManager.getScheduler();

        if (scheduler.getPlayer().getPlayingTrack() == null)
        {
            return PauseResult.NOTHING_PLAYING;
        }

        if (scheduler.getPlayer().isPaused())
        {
            return PauseResult.ALREADY_PAUSED;
        }

        scheduler.getPlayer().setPaused(true);
        return PauseResult.PAUSED;
    }

    public ResumeResult resume(Guild guild)
    {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        TrackScheduler scheduler = guildMusicManager.getScheduler();

        if (scheduler.getPlayer().getPlayingTrack() == null)
        {
            return ResumeResult.NOTHING_PLAYING;
        }

        if (!scheduler.getPlayer().isPaused())
        {
            return ResumeResult.NOT_PAUSED;
        }

        scheduler.getPlayer().setPaused(false);
        return ResumeResult.RESUMED;
    }

    public TrackScheduler.LoopMode setLoopMode(Guild guild, TrackScheduler.LoopMode mode)
    {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        guildMusicManager.getScheduler().setLoopMode(mode);
        return guildMusicManager.getScheduler().getLoopMode();
    }

    public TrackScheduler.LoopMode getLoopMode(Guild guild)
    {
        return getGuildMusicManager(guild).getScheduler().getLoopMode();
    }

    public QueueView getQueueView(Guild guild, int maxItems)
    {
        TrackScheduler scheduler = getGuildMusicManager(guild).getScheduler();
        return new QueueView(
                scheduler.getPlayer().getPlayingTrack(),
                scheduler.getQueueSnapshot(maxItems),
                scheduler.getQueueSize()
        );
    }
}