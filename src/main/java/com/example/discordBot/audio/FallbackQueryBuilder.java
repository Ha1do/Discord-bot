package com.example.discordBot.audio;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FallbackQueryBuilder
{
    private static final Set<String> NOISE_SEGMENTS = Set.of(
            "track",
            "album",
            "playlist",
            "artist",
            "music",
            "audio",
            "song",
            "songs",
            "ru",
            "us"
    );

    private FallbackQueryBuilder()
    {
    }

    public static String buildForSoundCloudSearch(String input, SourceType sourceType)
    {
        if (input == null)
        {
            return null;
        }

        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty())
        {
            return null;
        }

        if (!trimmedInput.startsWith("http"))
        {
            return trimmedInput;
        }

        try
        {
            URI uri = URI.create(trimmedInput);
            String host = uri.getHost();
            String path = uri.getPath();
            String hostHint = host == null ? sourceType.name().toLowerCase(Locale.ROOT) : host.replace("www.", "");

            List<String> terms = new ArrayList<>();
            if (path != null)
            {
                for (String rawSegment : path.split("/"))
                {
                    if (rawSegment == null || rawSegment.isBlank())
                    {
                        continue;
                    }

                    String decoded = URLDecoder.decode(rawSegment, StandardCharsets.UTF_8)
                            .replace('-', ' ')
                            .replace('_', ' ')
                            .trim();

                    if (decoded.isEmpty())
                    {
                        continue;
                    }

                    String normalized = decoded.toLowerCase(Locale.ROOT);
                    if (normalized.matches("^[0-9]+$") || normalized.matches("^[a-z0-9]{18,}$"))
                    {
                        continue;
                    }

                    if (NOISE_SEGMENTS.contains(normalized))
                    {
                        continue;
                    }

                    terms.add(decoded);
                    if (terms.size() >= 4)
                    {
                        break;
                    }
                }
            }

            if (terms.isEmpty())
            {
                return hostHint;
            }

            return String.join(" ", terms);
        }
        catch (Exception e)
        {
            return trimmedInput;
        }
    }
}

