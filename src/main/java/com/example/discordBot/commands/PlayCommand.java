package com.example.discordBot.commands;

import com.example.discordBot.LavaPlayer.PlayerManager;
import com.example.discordBot.audio.SourceResolver;
import com.example.discordBot.audio.SourceType;
import com.example.discordBot.audio.TrackQueryRouter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlayCommand implements Command
{
    @Override
    public String getName()
    {
        return "play";
    }

    @Override
    public String getDescription()
    {
        return "Bot connects to your voice channel and plays music";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event)
    {
        Guild guild = event.getGuild();
        if (guild == null)
        {
            event.reply("❌ This command can only be used in a server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        OptionMapping urlOption = event.getOption("url");
        if (urlOption == null)
        {
            event.reply("❌ URL option is missing!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String input = urlOption.getAsString();
        if (input == null || input.isBlank())
        {
            event.reply("❌ Please provide a link or search query.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        SourceType sourceType = SourceResolver.resolve(input);
        String trackQuery = TrackQueryRouter.resolve(input, sourceType);

        if (trackQuery == null)
        {
            event.reply("❌ Unsupported link. Supported sources: YouTube, SoundCloud, Spotify, Apple Music, YouTube Music, or plain search text.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Member member = event.getMember();
        if (member == null)
        {
            event.reply("❌ Could not determine the user.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || !memberVoiceState.inAudioChannel())
        {
            event.reply("❌ You need to be in a voice channel to use this command!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel())
        {
            guild.getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        }
        else if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
        {
            event.reply("❌ You need to be in the same voice channel as me to use this command!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply().queue();

        PlayerManager playerManager = PlayerManager.get();
        playerManager.play(guild, trackQuery, event);
    }
}