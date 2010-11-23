package edu.kit.aifb.cass.server;

import java.util.ArrayList;
import java.util.List;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.kit.aifb.cass.client.FollowerService;
import edu.kit.aifb.cass.shared.CassandraParam;
import edu.kit.aifb.cass.shared.model.Followee;
import edu.kit.aifb.cass.shared.model.Follower;

@SuppressWarnings("serial")
public class FollowerServiceImpl extends RemoteServiceServlet implements
		FollowerService {

	/**
	 * 
	 * @param follower
	 *            ... wants to follow followee.
	 * @param followee
	 *            ... is going to be followed by user.
	 * @throws IllegalArgumentException
	 */
	@Override
	public void addFollowee(String follower, String followee)
			throws IllegalArgumentException {
		// Insert a new row.
		StringSerializer serializer = StringSerializer.get();
		Mutator<String> mutator = HFactory.createMutator(Hector.getKeyspace(),
				serializer);
		// FOLLOWER COLUMN FAMILY
		// Insert params per line: Row Key, ColumnFamily, Column(key,value)
		// followee -> follower
		mutator.addInsertion(followee, CassandraParam.CF_FOLLOWERS.getValue(),
				HFactory.createStringColumn("follower", follower));
		// FOLLOWEE COLUMN FAMILY
		// follower -> followee
		mutator.addInsertion(follower, CassandraParam.CF_FOLLOWEES.getValue(),
				HFactory.createStringColumn("followee", followee));
		// Execute INSERT operations
		MutationResult mr = mutator.execute();
		System.out.println(mr.toString());
		System.out.println(followee + " is now following " + follower);
	}

	/**
	 * Get a (paginated) list of followers.
	 */
	@Override
	public List<Follower> getFollowersOf(String followee)
			throws IllegalArgumentException {
		final List<Follower> toReturn = new ArrayList<Follower>();

		// Perform a range query
		StringSerializer se = StringSerializer.get();
		RangeSlicesQuery<String, String, String> q = HFactory
				.createRangeSlicesQuery(Hector.getKeyspace(), se, se, se);
		q.setColumnFamily(CassandraParam.CF_FOLLOWERS.getValue());
		q.setRange("", "", false, 100);
		q.setKeys(followee, followee);
		// q.setRowCount(?);
		QueryResult<OrderedRows<String, String, String>> result = q.execute();
		System.out.println("Followers of "+followee);
		for (Row<String, String, String> row : result.get().getList()) {
			ColumnSlice<String, String> slice = row.getColumnSlice();
			HColumn<String, String> followerUsername = slice
					.getColumnByName("follower");
			if (followerUsername != null) {
				toReturn.add(new Follower(followee,
						followerUsername.getValue()));
				System.out.println(followerUsername.getValue());
			}
		}
		System.out.println("Result: "+result.toString());

		return toReturn;
	}

	/**
	 * Get a (paginated) list of followees.
	 */
	@Override
	public List<Followee> getFolloweesOf(String follower)
			throws IllegalArgumentException {
		final List<Followee> toReturn = new ArrayList<Followee>();

		// Perform a range query
		StringSerializer se = StringSerializer.get();
		RangeSlicesQuery<String, String, String> q = HFactory
				.createRangeSlicesQuery(Hector.getKeyspace(), se, se, se);
		q.setColumnFamily(CassandraParam.CF_FOLLOWEES.getValue());
		q.setRange("", "", false, 100);
		q.setKeys(follower, follower);
		QueryResult<OrderedRows<String, String, String>> result = q.execute();
		System.out.println(follower+ " is following these people:");
		for (Row<String, String, String> row : result.get().getList()) {
			ColumnSlice<String, String> slice = row.getColumnSlice();
			HColumn<String, String> followeeUsername = slice
					.getColumnByName("followee");
			if (followeeUsername != null) {
				toReturn.add(new Followee(follower,
						followeeUsername.getValue()));
				System.out.println(followeeUsername.getValue());
			}
		}
		System.out.println(result.toString());

		return toReturn;
	}

}
