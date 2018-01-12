package io.distributed.videodirector;

import com.google.gson.*;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import spark.Request;
import spark.Response;
import static spark.Spark.*;

/**
 * @author gonzo
 * 
 * 
**/
public class Main
{
    static Director server = new Director();
    static int id_counter  = 0;
    
    private static int copyInputStream(InputStream  in, 
                                       OutputStream out)
    throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;
        int total = 0;
        
        while ((len = in.read(buffer)) >= 0)
        {
            out.write(buffer, 0, len);
            total += len;
            System.out.print(".");
        }
        return total;
    }
    
    private static int getSessionID(Request req)
    {
        Integer id = req.session().attribute("id");
        if (id == null)
        {
            req.session().attribute("id", ++id_counter);
            System.out.println("New client id issued: " + id_counter);
            return id_counter;
        }
        System.out.println("ClientID: " + id);
        return id;
    }
    
    public static void main(String[] args)
    {
        setPort(1234);
        
        /// GET /events
        /// Lists all events (including videos) in JSON ///
        get("/events",
        (Request request, Response response) ->
        {
            response.status(200);
            response.type("application/json");
            return server.getEvents().toString();
        });
        
        /// POST /event/new
        /// Create new event from JSON ///
        /// Attributes: name, ...
        post("/event/new",
        (Request request, Response response) ->
        {
            // parse JSON data
            JsonElement req = new JsonParser().parse(request.body());
            JsonObject  obj = req.getAsJsonObject();
            
            System.out.println("Creating event: " + obj.toString());
            int id = server.addEvent(obj);
            System.out.println("New event has id: " + id);
            
            // add id to response
            obj.addProperty("id", id);
            // respond with json string
            response.status(200);
            response.type("application/json");
            return obj.toString();
        });
        
        /// GET /event/event_id
        /// Returns event videos for @event_id as JSON ///
        get("/event/:event_id",
        (request, response) ->
        {
            // parse request
            String sid = request.params("event_id");
            int id = Integer.parseInt(sid);
            
            System.out.println("Retrieving event: " + id);
            JsonObject event = server.eventById(id);
            System.out.println("Response: " + event.toString());
            if (event.toString().length() < 3)
            {
                response.status(404);
                return "No such event";
            }
            
            response.status(200);
            response.type("application/json");
            return server.getEventAndVideos(id).toString();
        });
        
        /// POST /event/event_id (JSON)
        /// Upload JSON metadata about a video for Event @id ///
        post("/event/:event_id",
        (Request request, Response response) ->
        {
            // parse request
            int id = Integer.parseInt(request.params("event_id"));
            System.out.println(request.body());
            
            JsonObject event = server.eventById(id);
            System.out.println("eventById: " + event.toString());
            if (event.toString().length() < 3)
            {
                response.status(404);
                return "No such event";
            }
            
            // parse JSON data
            // TODO: move me to Event
            JsonElement req = new JsonParser().parse(request.body());
            JsonObject  obj = req.getAsJsonObject();
            
            // here we must rate the video before putting it in database
            // the rating is done through static Rating class
            int rank = VideoRating.rate(obj);
            obj.addProperty("rating", rank);
            
            // finally,
            // add the status flag to video, flagged as "metadata only"
            obj.addProperty("status", 0);
            
            // add video to database (and get id)
            int video_id = server.addEventVideo(id, obj);
            
            // get client from session
            int client_id = getSessionID(request);
            Client c = server.getClient(client_id);
            
            try
            {
                // add video to clients list of candidates for upload
                c.addVideo(id, video_id, obj.get("finish_time").getAsString());
            } catch (ParseException ex)
            {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                // finish_time is the only throwable error:
                return "Failed to parse video finish time";
            }
            
            // respond with request for successful call
            response.type("application/json");
            
            JsonObject respobj = new JsonObject();
            respobj.addProperty("id", video_id);
            respobj.add("name", obj.get("name"));
            return respobj.toString();
        }, 
        new JsonTransformer());
        
        /// PUT /video/video_id
        /// Upload @video_id candidate
        put("/video/:video_id",
        (Request request, Response response) ->
        {
            int video_id = Integer.parseInt(request.params("video_id"));
            System.out.println("Receiving video: " + video_id);
            System.out.println("-->");
            
            /**
             * Check if video exists for client, or if we 
             * have already received this video
            **/
            int client_id = getSessionID(request);
            Client c = server.getClient(client_id);
            
            Video video = c.getVideo(video_id);
            if (video == null)
            {
                response.status(404); // no such resource
                return "Video " + video_id + " has not been registered yet";
            }
            if (video.isReceived())
            {
                response.status(409); // resource conflict
                return "Video " + video_id + " has already been received";
            }
            
            /**
             * Receive video from client
             * 
            **/
            File file = new File("upload/video" + video_id + ".mp4");
            System.out.println("Checking paths: " + file.getAbsolutePath());
            
            if (file.exists())
            {
                response.status(403); // forbidden
                return "File already exists\n";
            }
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs())            
            {
                response.status(500); // internal error
                return "File path failure: " + file.getAbsolutePath();
            }
            System.out.println("Downloading to: " + file.getAbsoluteFile());
            
            try (FileOutputStream fw = new FileOutputStream(file.getAbsoluteFile()))
            {
                InputStream content = request.raw().getInputStream();
                int len = copyInputStream(content, fw);
                
                System.out.println("Received " + len + " bytes from file: " + file.getAbsoluteFile());
                
                /**
                 * Register that we received video from client
                **/
                System.out.println("Received video " + video_id);
                video.received();
                // update video status on database
                server.updateVideoStatus(video_id, Video.RECEIVED);
                // update timestamp on event registered to video_id
                server.updateEventTimestamp(video.getEventId(), video.getFinishTimestamp());
                
                response.status(201); // resource created
                return "Upload successful\n";
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            
            response.status(500); // internal error
            return "Some Ting Wong (IOException)\n";
        });
        
        /// GET /video/video_id
        /// Retrieve video (@video_id)
        get("/video/:video_id",
        (Request request, Response response) ->
        {
            // parse request
            String svid = request.params("video_id");
            int video_id = Integer.parseInt(svid);
            
            JsonObject video = server.videoById(video_id);
            if (video.has("id") == false)
            {
                response.status(404);
                return "No such video: " + video_id + "\n";
            }
            /**
             * Transfer video to client
             * 
            **/
            try (OutputStream output = response.raw().getOutputStream())
            {
                File file = new File("upload/video" + video_id);
                if (!file.exists())
                {
                    response.status(404);
                    return "No such video" + video_id + "\n";
                }
                
                try (FileInputStream fi = new FileInputStream(file.getAbsoluteFile()))
                {
                    int len = copyInputStream(fi, output);
                    response.status(200);
                    return "Sent " + len + " bytes\n";
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    return "File could not be sent";
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return "Could not create output stream\n";
            }
        });
        
        /// GET /selected
        /// Retrieve list of selected (video) candidates
        get("/selected", 
        (Request request, Response response) ->
        {
            int client_id = getSessionID(request);
            Client c = server.getClient(client_id);
            
            if (false) //c.hasVideos() == false)
            {
                // response.status(404); // no uploaded videos
                return "[]";
            }
            
            ArrayList<Video> candidates = 
                    server.calculateCandidates(c);
            
        	// should be returned as a JSON string. 
            response.type("application/json");
            return new Gson().toJson(candidates);
        });
        
    }
}
