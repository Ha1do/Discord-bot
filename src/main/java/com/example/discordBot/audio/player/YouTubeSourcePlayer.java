package com.example.discordBot.audio.player;

import com.example.discordBot.audio.ResolvedTrack;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class YouTubeSourcePlayer extends AbstractLavaplayerSourcePlayer
{
    @Override
    protected String prepareTrackQuery(ResolvedTrack resolvedTrack)
    {
        return normalizeYoutubeMixUrl(resolvedTrack.getTrackQuery());
    }

    private String normalizeYoutubeMixUrl(String url)
    {
        if (url == null)
        {
            return null;
        }

        String trimmedUrl = url.trim();
        if (!(trimmedUrl.contains("youtube.com") || trimmedUrl.contains("youtu.be")))
        {
            return trimmedUrl;
        }

        int queryStart = trimmedUrl.indexOf('?');
        if (queryStart < 0 || queryStart == trimmedUrl.length() - 1)
        {
            return trimmedUrl;
        }

        String base = trimmedUrl.substring(0, queryStart);
        String query = trimmedUrl.substring(queryStart + 1);

        String mixListValue = null;
        List<String> keptParams = new ArrayList<>();

        for (String param : query.split("&"))
        {
            if (param.isBlank())
            {
                continue;
            }

            int separator = param.indexOf('=');
            String key = separator >= 0 ? param.substring(0, separator) : param;
            String value = separator >= 0 ? param.substring(separator + 1) : "";

            if ("list".equals(key))
            {
                String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
                if (decodedValue.startsWith("RD"))
                {
                    mixListValue = decodedValue;
                    continue;
                }
            }

            keptParams.add(param);
        }

        if (mixListValue == null)
        {
            return trimmedUrl;
        }

        if (keptParams.isEmpty())
        {
            return base;
        }

        return base + "?" + String.join("&", keptParams);
    }
}

