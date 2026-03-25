package com.example.discordBot.audio.player;

import com.example.discordBot.audio.FallbackQueryBuilder;
import com.example.discordBot.audio.ResolvedTrack;

public class ExternalFallbackSourcePlayer extends AbstractLavaplayerSourcePlayer
{
    @Override
    protected String prepareTrackQuery(ResolvedTrack resolvedTrack)
    {
        String query = FallbackQueryBuilder.buildForSoundCloudSearch(
                resolvedTrack.getTrackQuery(),
                resolvedTrack.getSourceType()
        );

        if (query == null || query.isBlank())
        {
            return null;
        }

        String trimmedQuery = query.trim();
        if (trimmedQuery.startsWith("scsearch:"))
        {
            return trimmedQuery;
        }

        return "scsearch:" + trimmedQuery;
    }
}

