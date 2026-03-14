package com.example.discordBot.audio.handlers;

import com.example.discordBot.audio.ResolvedTrack;
import com.example.discordBot.audio.SourceType;

public class YouTubeQueryHandler implements TrackQueryHandler
{
    @Override
    public ResolvedTrack resolveTrack(String input, SourceType sourceType)
    {
        return new ResolvedTrack(input, sourceType);
    }
}