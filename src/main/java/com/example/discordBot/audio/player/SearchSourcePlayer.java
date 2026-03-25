package com.example.discordBot.audio.player;

import com.example.discordBot.audio.ResolvedTrack;

public class SearchSourcePlayer extends AbstractLavaplayerSourcePlayer
{
	@Override
	protected String prepareTrackQuery(ResolvedTrack resolvedTrack)
	{
		String input = resolvedTrack.getTrackQuery();
		if (input == null)
		{
			return null;
		}

		String trimmedInput = input.trim();
		if (trimmedInput.startsWith("scsearch:"))
		{
			return trimmedInput;
		}

		return "scsearch:" + trimmedInput;
	}
}

