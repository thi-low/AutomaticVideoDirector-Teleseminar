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

import com.google.gson.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler
{
    final Connection connection;
    
    public static Connection getConnection() throws Exception
    {
        String driver = "org.gjt.mm.mysql.Driver";
        String url = "jdbc:mysql://localhost/videodirectordb";
        String username = "uname";
        String password = "passw";
        
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, username, password);
        return conn;
    }
    
    public DatabaseHandler() throws Exception
    {
        connection = getConnection();
    }
    
    private String createInsertQuery(String tableName, JsonObject data)
    {
        int numItems = data.entrySet().size();
        
        ArrayList<String> keys = new ArrayList<>();
        data.entrySet().stream().forEach((entry) ->
        {
            keys.add(entry.getKey());
        });
        
        String query = "INSERT INTO " + tableName + " (";

        for(int i = 0; i < numItems; i++)
        {
            query += keys.get(i);
            if(i != numItems - 1)
            {
                query += ", ";
            }
        }
        query += ") VALUES (";

        for(int i = 0; i < numItems; i++)
        {
            try
            {
                query += data.get(keys.get(i));
                if (i != numItems - 1) {
                    query += ", ";
                }
            }
            catch(JsonParseException e)
            {
                e.printStackTrace();
            }
        }
        query += ")";
        System.out.println("query: " + query);
        return query;
    }
    
    private int executeInsertQuery(String query)
    {
        Statement stm = null;
        try
        {
            stm = connection.createStatement();
            // resultSet gets the result of the SQL query
            stm.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next())
            {
                return rs.getInt(1);
            }
            return -1;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stm != null) stm.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return -1;
    }
    private int executeUpdateQuery(String query)
    {
        Statement stm = null;
        try
        {
            stm = connection.createStatement();
            // resultSet gets the result of the SQL query
            stm.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            
            ResultSet rs = stm.getGeneratedKeys();
            if (rs.next())
            {
                return rs.getInt(1);
            }
            return -1;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stm != null) stm.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return -1;
    }
    
    private ResultSet executeSelectQuery(Statement stm, String query)
            throws SQLException
    {
        return stm.executeQuery(query);
    }
    
    private JsonObject getSelectQueryAsJson(String query)
    {
        Statement stm = null;
        JsonObject result = null;
        try
        {
            stm = connection.createStatement();
            ResultSet set = executeSelectQuery(stm, query);
            
            /*if (set == null)
            {
                return "{\"error\": \"" + table + " empty result set\"}";
            }*/
            result = getJsonFromResultSet(set);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stm != null) stm.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    private ArrayList<Integer> getSelectQueryAsList(String query)
    {
        Statement stm = null;
        ArrayList<Integer> result = null;
        try
        {
            stm = connection.createStatement();
            ResultSet set = executeSelectQuery(stm, query);
            
            result = getIdFromResultSet(set);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (stm != null) stm.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        return result;
    }

    private JsonObject getJsonFromResultSet(ResultSet resultSet)
    {
        JsonObject json = new JsonObject();
        try
        {
            //find the column name
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numColumns = metaData.getColumnCount();
            String[] columnNames = new String[numColumns];
            
            for(int i = 0; i < numColumns; i++)
                columnNames[i] = metaData.getColumnName(i+1);
            
            //turn answer from query into a string
            while (resultSet.next())
            {
                JsonObject current = new JsonObject();
                
                for(int i = 0; i < numColumns; i++)
                {
                    String columnType = metaData.getColumnTypeName(i+1);
                    //System.out.println(columnNames[i] + " " + columnType);
                    // add the column value to json as new property
                    switch (columnType)
                    {
                    case "VARCHAR":
                        current.addProperty(
                                columnNames[i], resultSet.getString(i+1));
                        break;
                    case "INT":
                        current.addProperty(
                                columnNames[i], resultSet.getInt(i+1));
                        break;
                    case "TIME":
                        current.addProperty(
                                columnNames[i], resultSet.getTime(i+1).toString());
                        break;
                    case "DATE":
                        current.addProperty(
                                columnNames[i], resultSet.getDate(i+1).toString());
                        break;
                    case "BIGINT":
                        current.addProperty(
                                columnNames[i], resultSet.getLong(i+1));
                        break;
                    default:
                        System.out.println("UNUSED COLUMN: " + columnNames[i] + " " + columnType);
                    }
                }
                json.add(resultSet.getString(1), current);
            }
        }
        catch (Exception e)
        {
            System.out.println("Error occured in getJsonFromResultSet: " + e);
            e.printStackTrace();
        }
        System.out.println(json.toString());
        return json;
    }
    private ArrayList<Integer> getIdFromResultSet(ResultSet resultSet)
    {
        ArrayList<Integer> res = new ArrayList<>();
        try
        {
            //find the column name
            ResultSetMetaData metaData = resultSet.getMetaData();
            
            int numColumns = metaData.getColumnCount();
            if (numColumns == 0) return null;
            
            // get all ids from result set
            while (resultSet.next())
            {
                res.add( resultSet.getInt(1) );
            }
        }
        catch (Exception e)
        {
            System.out.println("Error occured in getIdFromResultSet: " + e);
            e.printStackTrace();
        }
        return res;
    }
    
    public JsonObject getEvents()
    {
        String query = "SELECT id, name FROM event";
        return getSelectQueryAsJson(query);
    }
    public JsonObject getEvent(int event)
    {
        String query = "SELECT id, name FROM event WHERE id=" + event;
        return getSelectQueryAsJson(query);
    }
    public long getEventTimestamp(int event_id)
    {
        // get events belonging to a video
        String query = "SELECT id, unix_timestamp(ts) as ts FROM event "
                + "WHERE id=" + event_id;
        JsonObject obj = getSelectQueryAsJson(query);
        return obj.getAsJsonObject(Integer.toString(event_id))
        		.get("ts").getAsLong();
    }
    public void setEventTimestamp(int event_id, long ts)
    {
        // set the current timestamp for an event
        Date ts_date = new Date(ts * 1000);
        String ts_str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts_date);
        
        System.out.println("New event(" + event_id + ") timestamp: " + ts_str);
        
        // ESCAPES!!
        String query = "UPDATE event SET ts=\"" + ts_str + "\" WHERE id=" + event_id;
        executeUpdateQuery(query);
    }
    public JsonObject getEventVideos(int event)
    {
        // get event
        JsonObject obj = getEvent(event);
        // get videos for event
        String query = "SELECT video_id FROM event_videos WHERE event_id=" + event;
        JsonObject child = getSelectQueryAsJson(query);
        // add videos to event
        obj.add("videos", child);
        return obj;
    }
    public ArrayList<Integer> getEventFromVideo(int video_id)
    {
        // get events belonging to a video
        String query = "SELECT event_id FROM event_videos WHERE video_id=" + video_id;
        return getSelectQueryAsList(query);
    }
    public ArrayList<Integer> getEventTopRatedVideo(int event_id)
    {
        // get events belonging to a video
        String query = "SELECT v.id, v.rating, e.event_id FROM video AS v, event_videos AS e "
                     + "WHERE v.id = e.video_id AND e.event_id = " + event_id + " ORDER BY v.rating";
        return getSelectQueryAsList(query);
    }
    
    public int addEvent(JsonObject data)
    {
        String query = createInsertQuery("event", data);
        System.out.println(query);
        return executeInsertQuery(query);
    }
    
    public JsonObject getVideo(int video_id)
    {
        String query = "SELECT * FROM video WHERE id=" + video_id;
        return getSelectQueryAsJson(query);
    }
    
    /**
     * @param data Video metadata as JSON object
     * @return Returns ID of saved video
    **/
    public int saveVideo(JsonObject data)
    {
        String query = createInsertQuery("video", data);
        System.out.println(query);
        return executeInsertQuery(query);
    }
    
    public long addEventVideo(long event_id, long video_id)
    {
        JsonObject json = new JsonObject();
        json.addProperty("event_id", event_id);
        json.addProperty("video_id", video_id);
        
        String query = createInsertQuery("event_videos", json);
        return executeInsertQuery(query);
    }

    public void setVideoStatus(int video_id, int status)
    {
        String query = "UPDATE video SET status=" + status + " WHERE id=" + video_id;
        executeUpdateQuery(query);
    }
    public int getVideoStatus(int video_id)
    {
        String query = "SELECT status FROM video WHERE id=" + video_id;
        ArrayList<Integer> list = getSelectQueryAsList(query);
        
        if (!list.isEmpty()) return list.get(0);
        return -1;
    }
    
    public ArrayList<Integer> getEventVideosAfterTimestamp(
            int event_id, int status)
    {
        long ts = getEventTimestamp(event_id);
        
        // find videos for event that starts after the delayed time
        String query = 
            "SELECT v.id, v.rating FROM video AS v, event_videos AS e WHERE "
          + "v.id = e.video_id AND v.status = " + status + " AND ("
          + "unix_timestamp(v.finish_time) - (v.duration / 1000) > " + ts + ") "
          + "AND e.event_id=" + event_id + " ORDER BY v.rating LIMIT 1;";
        
        return getSelectQueryAsList(query);
    }
    
}
