package edu.kit.aifb.cass.shared.model;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Follower implements Serializable {
	
	private String username;
	private String followerUsername;
	
	public Follower() {

	}
	
	public Follower(String username, String follower) {
		this.username = username;
		this.followerUsername = follower;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFollower() {
		return followerUsername;
	}

	public void setFollower(String follower) {
		this.followerUsername = follower;
	}
	
	/**
	 * Followers are the same if the have the same username.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Follower) {
			Follower f = (Follower)o;
			return f.getUsername().equals(username);
		} else {
			return super.equals(o);
		}
	}
	
}
