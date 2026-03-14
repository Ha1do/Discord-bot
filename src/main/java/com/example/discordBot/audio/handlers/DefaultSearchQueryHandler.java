package com.example.discordBot.audio.handlers;

public class DefaultSearchQueryHandler implements TrackQueryHandler
{
    @Override
    public String resolveTrackQuery(String input)
    {
        return "ytsearch:" + input;
    }
}