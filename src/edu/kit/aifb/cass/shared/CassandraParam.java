package edu.kit.aifb.cass.shared;

public enum CassandraParam {

	CLUSTER_NAME("TwissandraCluster"), HOST_POOL("localhost:9160"), KEYSPACE(
			"Twissandra"), CF_User("User"), CF_FOLLOWEES("Followees"), CF_FOLLOWERS(
			"Followers"), CF_TWEET("Tweets"), CF_TIMELINE("Timeline"), CF_USERLINE(
			"Userline"), PUBLIC_USERLINE_KEY("Public");

	private final String value;

	CassandraParam(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
