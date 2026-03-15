package com.example.discordBot.LavaPlayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter
{
    public enum LoopMode
    {
        OFF,
        TRACK,
        QUEUE
    }

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
    private LoopMode loopMode = LoopMode.OFF;

    public TrackScheduler(AudioPlayer player)
    {
        this.player = player;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        if (!endReason.mayStartNext)
        {
            return;
        }

        if (track != null)
        {
            if (loopMode == LoopMode.TRACK)
            {
                player.startTrack(track.makeClone(), false);
                return;
            }

            if (loopMode == LoopMode.QUEUE)
            {
                queue.offer(track.makeClone());
            }
        }

        player.startTrack(queue.poll(), false);
    }

    public void queue(AudioTrack track)
    {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public AudioTrack skipCurrentTrack()
    {
        AudioTrack nextTrack = queue.poll();
        player.startTrack(nextTrack, false);
        return nextTrack;
    }

    public boolean skipAllTracks()
    {
        if (player.getPlayingTrack() == null && queue.isEmpty())
        {
            return false;
        }

        queue.clear();
        player.stopTrack();
        return true;
    }

    public LoopMode getLoopMode()
    {
        return loopMode;
    }

    public void setLoopMode(LoopMode loopMode)
    {
        this.loopMode = loopMode == null ? LoopMode.OFF : loopMode;
    }

    public List<AudioTrack> getQueueSnapshot(int limit)
    {
        int safeLimit = Math.max(limit, 0);
        return queue.stream().limit(safeLimit).toList();
    }

    public int getQueueSize()
    {
        return queue.size();
    }

    public AudioPlayer getPlayer()
    {
        return player;
    }

    public BlockingQueue<AudioTrack> getQueue()
    {
        return queue;
    }
}