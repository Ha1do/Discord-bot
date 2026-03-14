package com.example.discordBot.audio;

import com.example.discordBot.audio.handlers.DefaultSearchQueryHandler;
import com.example.discordBot.audio.handlers.SoundCloudQueryHandler;
import com.example.discordBot.audio.handlers.TrackQueryHandler;
import com.example.discordBot.audio.handlers.YouTubeQueryHandler;

public class TrackQueryRouter
{
    public static String resolve(String input, SourceType sourceType)
    {
        TrackQueryHandler handler = switch (sourceType)
        {
            case YOUTUBE, YOUTUBE_MUSIC -> new YouTubeQueryHandler();
            case SOUNDCLOUD -> new SoundCloudQueryHandler();
            case SEARCH -> new DefaultSearchQueryHandler();
            default -> null;
        };

        if (handler == null)
        {
            return null;
        }

        return handler.resolveTrackQuery(input);
    }
}