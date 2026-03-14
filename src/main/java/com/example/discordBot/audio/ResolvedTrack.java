package com.example.discordBot.audio;

public class ResolvedTrack
{
    private final String url;
    private final SourceType sourceType;

    public ResolvedTrack(String url, SourceType sourceType)
    {
        this.url = url;
        this.sourceType = sourceType;
    }

    public String getUrl()
    {
        return url;
    }

    public SourceType getSourceType()
    {
        return sourceType;
    }
}