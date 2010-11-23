package edu.kit.aifb.cass.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.kit.aifb.cass.shared.model.Follower;
import edu.kit.aifb.cass.shared.model.Tweet;
import edu.kit.aifb.cass.shared.model.User;

/**
 * The async counterpart of <code>FollowerService</code>.
 */
public interface TweetServiceAsync {
	void saveTweet(Tweet tweet, List<Follower> followers, AsyncCallback<Void> callback)
			throws IllegalArgumentException;

	void getUserline(User user, Long start, int count, AsyncCallback<Tweet[]> callback)
			throws IllegalArgumentException;
	
	void getTimeline(User user, Long start, int count, AsyncCallback<Tweet[]> callback)
	throws IllegalArgumentException;
}
