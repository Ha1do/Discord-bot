package com.example.discordBot.audio.handlers;

import com.example.discordBot.audio.ResolvedTrack;
import com.example.discordBot.audio.SourceType;

public interface TrackQueryHandler
{
    ResolvedTrack resolveTrack(String input, SourceType sourceType);
}