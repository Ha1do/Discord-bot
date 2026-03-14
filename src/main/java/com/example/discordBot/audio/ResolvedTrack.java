package com.example.discordBot.audio;

public class ResolvedTrack
{
    private final String trackQuery;
    private final SourceType sourceType;

    public ResolvedTrack(String trackQuery, SourceType sourceType)
    {
        this.trackQuery = trackQuery;
        this.sourceType = sourceType;
    }

    public String getTrackQuery()
    {
        return trackQuery;
    }

    // Backward-compatible alias for existing call sites.
    public String getUrl()
    {
        return trackQuery;
    }

    public SourceType getSourceType()
    {
        return sourceType;
    }
}