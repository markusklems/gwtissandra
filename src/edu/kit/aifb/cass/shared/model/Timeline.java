package edu.kit.aifb.cass.shared.model;

import java.util.List;

/**
 * 
 * The per-user Timeline represents a batch of tweets which can be used for
 * pagination.
 * 
 * @author user markus klems
 * 
 */
public class Timeline {
	// The timeline key is the username.
	private String username;
	private List<Tweet> tweets;
	private Long nextTweet;
	
	public Timeline(){
		
	}

	public Timeline(String username, List<Tweet> view, Long nextview) {
		this.username = username;
		this.tweets = view;
		this.nextTweet = nextview;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * The <code>getTweets()</code> method returns a limited number of tweets.
	 * The <code>getNextTweet()</code> method returns the oldest tweet timestamp
	 * in the batch.
	 * 
	 * @return Batch of tweets.
	 */
	public List<Tweet> getTweets() {
		return tweets;
	}

	/**
	 * The <code>getTweets()</code> method returns a limited number of tweets.
	 * The <code>getNextTweet()</code> method returns the oldest tweet timestamp
	 * in the batch.
	 * 
	 * @return Timestamp of next tweet to fetch with the next data store request.
	 */
	public Long getNextTweet() {
		return nextTweet;
	}

}