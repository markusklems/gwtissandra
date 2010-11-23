package edu.kit.aifb.cass.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.kit.aifb.cass.shared.model.Followee;
import edu.kit.aifb.cass.shared.model.Follower;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("follower")
public interface FollowerService extends RemoteService {
	void addFollowee(String username, String follower) throws IllegalArgumentException;
	List<Follower> getFollowersOf(String username) throws IllegalArgumentException;
	List<Followee> getFolloweesOf(String username)
			throws IllegalArgumentException;
}
