/*
 * The MIT License
 *
 * Copyright 2014 gonzo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.distributed.videodirector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author gonzo
 */
public class Client
{
    private final ArrayList<Video> videos;
    private final int session_id;
    
    public Client(int id)
    {
        this.session_id = id;
        this.videos = new ArrayList<>();
    }
    
    public int getSessionId()
    {
        return session_id;
    }
    
    public Video getVideo(int vid)
    {
        for (Video v : videos)
        {
            if (v.getId() == vid) return v;
        }
        return null;
    }
    public void addVideo(int event_id, int vid, String timestamp)
        throws ParseException
    {
        if (hasVideo(vid)) return;
        // parse date to unix timestamp
        Date ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
        // add new video for this client (with timestamp in seconds)
        videos.add(new Video(event_id, vid, ts.getTime() / 1000));
    }
    
    public boolean hasVideos()
    {
        return !videos.isEmpty();
    }
    public boolean hasVideo(int vid)
    {
        return videos.stream().anyMatch((v) -> 
                (v.getId() == vid));
    }
    public ArrayList<Video> getVideos()
    {
        return videos;
    }
}
