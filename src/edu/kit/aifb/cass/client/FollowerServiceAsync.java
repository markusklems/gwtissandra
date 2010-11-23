package edu.kit.aifb.cass.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.kit.aifb.cass.shared.model.Followee;
import edu.kit.aifb.cass.shared.model.Follower;
import edu.kit.aifb.cass.shared.model.Tweet;

/**
 * The async counterpart of <code>FollowerService</code>.
 */
public interface FollowerServiceAsync {
	void addFollowee(String username, String follower, AsyncCallback<Void> callback)
			throws IllegalArgumentException;

	void getFollowersOf(String username, AsyncCallback<List<Follower>> callback);

	void getFolloweesOf(String username, AsyncCallback<List<Followee>> callback);

}
