package com.jwt.components;

public class Location {
	private String location;
	private String user;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	@Override
	public String toString() {
		return "Track [location=" + location + ", user=" + user + "]";
	}
}
