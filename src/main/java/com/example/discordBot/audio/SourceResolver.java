package com.example.discordBot.audio;

import java.net.URI;

public class SourceResolver
{
    public static SourceType resolve(String input)
    {
        if (input == null || input.isBlank())
        {
            return SourceType.UNKNOWN;
        }

        if (!input.startsWith("http"))
        {
            return SourceType.SEARCH;
        }

        try
        {
            String host = URI.create(input).getHost();

            if (host == null)
            {
                return SourceType.UNKNOWN;
            }

            host = host.toLowerCase();

            if (host.contains("youtube.com") || host.contains("youtu.be"))
            {
                return SourceType.YOUTUBE;
            }

            if (host.contains("soundcloud.com"))
            {
                return SourceType.SOUNDCLOUD;
            }

            if (host.contains("spotify.com"))
            {
                return SourceType.SPOTIFY;
            }

            if (host.contains("music.apple.com"))
            {
                return SourceType.APPLE_MUSIC;
            }

            if (host.contains("music.youtube.com"))
            {
                return SourceType.YOUTUBE_MUSIC;
            }

            return SourceType.UNKNOWN;
        }
        catch (Exception e)
        {
            return SourceType.UNKNOWN;
        }
    }
}