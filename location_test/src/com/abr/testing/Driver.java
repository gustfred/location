package com.abr.testing;

public class Driver {
	
	private String name = "";
	private long bestTime = 0;
	private int stintLaps=0,laps=0;
	
	public String getName(){
		return name;
		
	}
	
	public void setName(String name){
		this.name = name;
		
	}
	
	public long getTime(){
		return bestTime;
	}
	
	public void setTime(long time){
		this.bestTime = time;
		
	}
	
	public int getStintLaps(){
		return stintLaps;
	}
	
	public void setStintLaps(int stint){
		this.stintLaps = stint;
		
	}
	
	public int getLaps(){
		return laps;
	}
	
	public void setLaps(int laps){
		this.laps = laps;
		
	}

}
