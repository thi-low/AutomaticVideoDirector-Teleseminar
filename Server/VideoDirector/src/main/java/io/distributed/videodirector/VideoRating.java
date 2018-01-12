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

import com.google.gson.JsonObject;

/**
 *
 * @author gonzo
 */
public class VideoRating
{
    // http://en.wikipedia.org/wiki/Sigmoid_function
    // http://upload.wikimedia.org/wikipedia/commons/6/6f/Gjl-t%28x%29.svg
    static double transformed_sigmoid(double x, double duration)
    {
        // video duration scaling (for short videos)
        x *= x / duration / 15.0;
        x -= 0.3 * duration;
        
        // some arbitrary sigmoid function
        double f = x / (1 + Math.abs(x));
        f = Math.pow(f, 3.0);
        
        // transform from [-1, 1] to [0, 1]
        return (f + 1.0) * 0.5;
    }
    
    /**
     * @param shake
     * @param tilt
     * @return Rank values in range [0, 100]
    **/
    static int rank(double shake, double tilt)
    {
        final double kernel[] =
        {
            shake, 
            tilt
        };
        final double weights[] = 
        {
            0.60, // shake
            0.40  // tilt
        };
        
        double dot = 0.0f;
        for (int i = 0; i < 2; i++)
        {
            dot += (kernel[i] * kernel[i]) * weights[i];
        }
        double root = Math.sqrt(dot);
        
        return (int) (root * 100.0);
    }
    
    /**
     * @param obj Json string of video to be rated
     * @return Returns the rating for a video in range [0, 100]
    **/
    static int rate(JsonObject obj)
    {
        double dur = obj.get("duration").getAsInt() / 1000.0;
        if (dur < 0.1) dur = 0.1; // clamp to some positive
        
        int tilt  = obj.get("tilt").getAsInt();
        int shake = obj.get("shaking").getAsInt();
        
        // transformation function: f(duration, shake/tilt)
        double nshake = transformed_sigmoid(shake, dur);
        double ntilt  = transformed_sigmoid(tilt,  dur);
        
        // apply final ranking kernel for weighting shake vs tilt
        return rank(nshake, ntilt);
    }
}
