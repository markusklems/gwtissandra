package edu.kit.aifb.cass.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.kit.aifb.cass.client.TweetService;
import edu.kit.aifb.cass.shared.CassandraParam;
import edu.kit.aifb.cass.shared.model.Followee;
import edu.kit.aifb.cass.shared.model.Follower;
import edu.kit.aifb.cass.shared.model.Tweet;
import edu.kit.aifb.cass.shared.model.User;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class TweetServiceImpl extends RemoteServiceServlet implements
		TweetService {

	public void saveTweet(Tweet tweet, List<Follower> followers)
			throws IllegalArgumentException {
		// Assign a timestamp.
		Long timestamp = System.currentTimeMillis();
		tweet.setTweettimestamp(timestamp);
		// Assign a random ID to the tweet.
		String tweetID = UUID.randomUUID().toString() + timestamp;
		tweet.setID(tweetID);
		// Insert a new row.
		StringSerializer serializer = StringSerializer.get();
		Mutator<String> mutator = HFactory.createMutator(Hector.getKeyspace(),
				serializer);
		// TWEET COLUMN FAMILY
		// Insert: Row Key, ColumnFamily, Column(name,value)
		// Insert tweet body and username with the same row key tweetID
		mutator.addInsertion(tweetID, CassandraParam.CF_TWEET.getValue(),
				HFactory.createStringColumn("body", tweet.getBody()))
				.addInsertion(
						tweetID,
						CassandraParam.CF_TWEET.getValue(),
						HFactory.createStringColumn("username",
								tweet.getUsername()));
		// USERLINE COLUMN FAMILY
		// Insert params per line: Row Key, ColumnFamily, Column(name,value)
		// Insert timestamp->tweetKey with row key username
		mutator.addInsertion(tweet.getUsername(), CassandraParam.CF_USERLINE
				.getValue(), HFactory.createColumn(timestamp, tweetID,
				LongSerializer.get(), serializer));
		// FOLLOWEES COLUMN FAMILY
		insertFollowers(timestamp, tweetID, mutator, followers);
		// PUBLIC USERLINE
		mutator.addInsertion(CassandraParam.PUBLIC_USERLINE_KEY.getValue(), CassandraParam.CF_USERLINE
				.getValue(), HFactory.createColumn(timestamp, tweetID,
				LongSerializer.get(), serializer));

		// Execute INSERT operations
		MutationResult mr = mutator.execute();
		System.out.println(mr.toString());

		System.out.println("Tweet " + tweet.getID() + " with body \""
				+ tweet.getBody() + "\"" + " at time "
				+ tweet.getTweettimestamp() + " has been saved.");
	}

	/**
	 * Helper method to insert a tweet into each follower's personal timeline of
	 * tweets which they are following.
	 * 
	 * @param mutator
	 * @param followers
	 */
	private void insertFollowers(Long timestamp, String tweetID,
			Mutator<String> mutator, List<Follower> followers) {
		// Insert params per line: Row Key, ColumnFamily, Column(key,value)
		// Insert timestamp->tweetID with row key followername
		for (Follower f : followers) {
			mutator.addInsertion(
					f.getFollower(),
					CassandraParam.CF_TIMELINE.getValue(),
					HFactory.createColumn(timestamp, tweetID,
							LongSerializer.get(), StringSerializer.get()));
		}
	}

	/**
	 * Retrieve the user's own tweets.
	 * 
	 */
	public Tweet[] getUserline(User user, Long timestamp, int count) {
		return getLine(CassandraParam.CF_USERLINE.getValue(), user.getName(), timestamp,
				count);
	}

	/**
	 * Retrieve the tweets that user with username follows.
	 */
	public Tweet[] getTimeline(User user, Long timestamp, int count) {
		return getLine(CassandraParam.CF_TIMELINE.getValue(), user.getName(), timestamp,
				count);
	}

	/**
	 * 
	 * Get a userline or timeline for a given username.
	 * 
	 * @param username
	 * @param start
	 * @param limit
	 * @return List of Tweets. The last tweet should be used as nextTweet for
	 *         pagination.
	 */
	private Tweet[] getLine(final String columnFamilyType, final String username,
			final Long timestamp, final int count) {
		// Perform some sanity checks.
		// if (!(columnFamilyType.equals(CassandraParam.CF_TIMELINE.toString())
		// || columnFamilyType
		// .equals(CassandraParam.CF_USERLINE.toString()))) {
		// return null;
		// }
		// Retrieve the tweet IDs.
		
		// Create a list of size count + 1 (max number of Tweets retrieved with a query)
		final Tweet[] toReturn = new Tweet[count+1];

		// Prepare a range query
		StringSerializer se = StringSerializer.get();
		RangeSlicesQuery<String, Long, String> q = HFactory
				.createRangeSlicesQuery(Hector.getKeyspace(), se,
						LongSerializer.get(), se);
		q.setColumnFamily(columnFamilyType);
		// Range query from 0 to the most recent timestamp.
		q.setRange(0L,timestamp, false, count);
		// Restrict query on rows with key=username.
		q.setKeys(username, username);
		// Execute the query.
		final QueryResult<OrderedRows<String, Long, String>> result = q
				.execute();
		System.out.println(result.toString());
		final Row<String, Long, String> row = result.get().getByKey(username);
		// If there are any results... retrieve the actual tweets in descending time order.
		if (row != null) {
			final ColumnSlice<Long, String> slice = row.getColumnSlice();
			final HashMap<Long,String> unorderedTweetIDs = new HashMap<Long,String>();
			System.out.println("Row: "+row.toString());
			System.out.println("Column slice: "+slice.toString());
			for (final HColumn<Long, String> c : slice.getColumns()) {
				System.out.println("Tweet: " + c.getValue());
				unorderedTweetIDs.put(c.getName(),c.getValue());
			}

			// Prepare a multiget query to retrieve the actual tweets.
			MultigetSliceQuery<String, String, String> mq = HFactory
					.createMultigetSliceQuery(Hector.getKeyspace(), se, se, se);
			mq.setColumnFamily(CassandraParam.CF_TWEET.getValue());
			mq.setKeys(unorderedTweetIDs.values().toArray(new String[]{}));
			mq.setRange("", "", false, count);
			final QueryResult<Rows<String, String, String>> mqResult = mq
					.execute();
			// Sort the tweet IDs.
			List<String> orderedTweetIDs = sortTweets(unorderedTweetIDs);
			for (final Row<String, String, String> mqRow : mqResult.get()) {
				final ColumnSlice<String, String> mqSlice = mqRow
						.getColumnSlice();
				String tweetID = mqRow.getKey();
				int index = orderedTweetIDs.indexOf(tweetID);
				toReturn[index] = new Tweet(tweetID, mqSlice.getColumnByName("username")
						.getValue(), mqSlice.getColumnByName("body").getValue());
			}
			System.out.println(mqResult.toString());

		}

		return toReturn;
	}

	/**
	 * Helper method that sorts the input hashmap by keys (i.e. by timestamps).
	 * 
	 * @param unorderedTweetIDs HashMap with unordered Tweet IDs.
	 * @return List with ordered Tweet IDs.
	 */
	private List<String> sortTweets(
			HashMap<Long, String> unorderedTweetIDs) {
		List<String> toReturn = new ArrayList<String>();
		int max = unorderedTweetIDs.values().size();
		TreeSet<Long> sortedSet = new TreeSet<Long>(new ReverseComparator<Long>());
		sortedSet.addAll(unorderedTweetIDs.keySet());
		Long[] sortedKeys = sortedSet.toArray(new Long[]{});
		for(int i=0; i<max; i++) {
			toReturn.add(i, unorderedTweetIDs.get(sortedKeys[i]));
		}
		return toReturn;
	}
	
	class ReverseComparator<Long> implements Comparator<Long> {

		@Override
		public int compare(Long l1, Long l2) {
			String s1 = l1.toString();
			String s2 = l2.toString();
			return s2.compareTo(s1);
		}

		
	}

}
