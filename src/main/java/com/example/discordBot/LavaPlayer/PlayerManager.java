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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager
{
    private static final Logger logger = LoggerFactory.getLogger(PlayerManager.class);

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