package com.example.automaticvideodirector.database;


/**
*
* Meta data class
*/

public class MetaData {
	
	//VARIABLES
	private long _id;
	private String filename;
	private String timestamp;
	private int duration;
	private int width;
	private int height;
	private int shaking;
	private int tilt;
	private String status;
	private long serverId;
	
	
	//GETTER
	public long getId(){
		return this._id;
	}
	public String getVideoFile(){
		return this.filename;
	}
	public String getTimeStamp(){
		return this.timestamp;
	}
	public int getDuration(){
		return this.duration;
	}
	public int getWidth(){
		return this.width;
	}
	public int getHeight(){
		return this.height;
	}
	public int getShaking(){
		return this.shaking;
	}
	public int getTilt(){
		return this.tilt;
	}
	public String getStatus(){
		return this.status;
	}
	public long getServerId(){
		return this.serverId;
	}
	
	//SETTER
	public void setId(long id){
		this._id=id;
	}
	public void setVideoFile(String filename){
		this.filename=filename;
	}
	public void setTimeStamp(String timestamp){
		this.timestamp=timestamp;
	}
	public void setDuration(int duration){
		this.duration=duration;
	}
	public void setWidth(int width){
		this.width=width;
	}
	public void setHeight(int height){
		this.height=height;
	}
	public void setShaking(int shaking){
		this.shaking=shaking;
	}
	public void setTilt(int tilt){
		this.tilt=tilt;
	}
	public void setStatus(String status){
		this.status=status;
	}
	public void setServerId(long serverId){
		this.serverId = serverId;
	}
	

}
