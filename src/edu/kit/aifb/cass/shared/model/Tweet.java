package edu.kit.aifb.cass.shared.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Tweet implements Serializable
{
	private String id;
    private String username;
    private String body;
    private long tweettimestamp;

    public Tweet() {
    	
    }
    
    /**
     * The two parameters 'key' and 'tweettimestamp' are generated on the server side.
     * 
     * @param username Username of user whon tweeted.
     * @param body Text body of the tweet.
     */
    public Tweet(final String username, final String body) {
        this.username = username;
        this.body = body;
    }
    
	public Tweet(final String id, final String username, final String body) {
    	this.id = id;
        this.username = username;
        this.body = body;
    }
    
	public Tweet(final String id, final String username, final String body, final long tweettimestamp) {
    	this.id = id;
        this.username = username;
        this.body = body;
        this.tweettimestamp = tweettimestamp;
    }

	public String getID() {
        return id;
    }

    public void setID(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public String getBody() {
        return body;
    }
	
    public long getTweettimestamp() {
		return tweettimestamp;
	}
    
    public void setTweettimestamp(long tweettimestamp) {
		this.tweettimestamp = tweettimestamp;
	}
}