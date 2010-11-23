package edu.kit.aifb.cass.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.kit.aifb.cass.shared.model.Follower;
import edu.kit.aifb.cass.shared.model.Tweet;
import edu.kit.aifb.cass.shared.model.User;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("tweet")
public interface TweetService extends RemoteService {
	void saveTweet(Tweet tweet, List<Follower> followers) throws IllegalArgumentException;
	Tweet[] getUserline(final User user, Long start, int count) throws IllegalArgumentException;
	Tweet[] getTimeline(final User user, Long start, int count) throws IllegalArgumentException;
}
