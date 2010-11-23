package edu.kit.aifb.cass.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class User implements Serializable {

	private String name;

    private List<Follower> followers = new ArrayList<Follower>();
    private List<Followee> followees = new ArrayList<Followee>();

    public User(final String name) {
        this.name = name;
    }
    
    public User() {
    	
    }

	public String getName() {
        return name;
    }

	public List<Follower> getFollowers() {
		return followers;
	}

	public void setFollowers(List<Follower> followers) {
		this.followers = followers;
	}
	
	public List<Followee> getFollowees() {
		return followees;
	}

	public void setFollowees(List<Followee> followees) {
		this.followees = followees;
	}
	
}

