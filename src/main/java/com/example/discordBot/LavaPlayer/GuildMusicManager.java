package com.example.discordBot.LavaPlayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager
{
    private TrackScheduler scheduler;
    public AudioForwarder audioForwarder;

    public GuildMusicManager(AudioPlayerManager manager)
    {
        AudioPlayer player = manager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        this.audioForwarder = new AudioForwarder(player);
    }

    public TrackScheduler getScheduler()
    {
        return scheduler;
    }

    public AudioForwarder getAudioForwarder()
    {
        return audioForwarder;
    }
}
