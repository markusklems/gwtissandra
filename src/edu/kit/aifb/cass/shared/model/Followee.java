package edu.kit.aifb.cass.shared.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Followee implements Serializable {

	private String username;
	private String followeeUsername;

	public Followee() {

	}

	public Followee(String username, String followee) {
		this.username = username;
		this.followeeUsername = followee;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFollowee() {
		return followeeUsername;
	}

	public void setFollowee(String followee) {
		this.followeeUsername = followee;
	}

	/**
	 * Followees are the same if the have the same username.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Followee) {
			Followee f = (Followee)o;
			return f.getUsername().equals(username);
		} else {
			return super.equals(o);
		}
	}

}
