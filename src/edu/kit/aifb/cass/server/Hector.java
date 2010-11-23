package edu.kit.aifb.cass.server;

import edu.kit.aifb.cass.shared.CassandraParam;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

public class Hector {
	
	private static Cluster cluster = null;
	private static Keyspace keyspace = null;
	
	/**
	 * Create a Hector client with default Cassandra settings as specified in CasandraParam.
	 */
	static {
		cluster = HFactory.getOrCreateCluster(CassandraParam.CLUSTER_NAME.getValue(), CassandraParam.HOST_POOL.getValue());
		keyspace = HFactory.createKeyspace(CassandraParam.KEYSPACE.getValue(), cluster);
	}
	
	/**
	 * 
	 * @param clusterName The name of the cluster.
	 * @param config CassandraHostConfigurator
	 * @param keyspaceName The name of the Cassandra Keyspace.
	 */
	public static void set(String clusterName, CassandraHostConfigurator config, String keyspaceName) {
		cluster = HFactory.getOrCreateCluster(clusterName, config);
		keyspace = HFactory.createKeyspace(keyspaceName, cluster);
	}
	
	/**
	 *  Set the default host pool "localhost:9160"
	 *  
	 * @param clusterName The name of the cluster.
	 * @param keyspaceName The name of the Cassandra Keyspace.
	 */
	public static void set(String clusterName, String keyspaceName) {
		cluster = HFactory.getOrCreateCluster(clusterName, "localhost:9160");
		keyspace = HFactory.createKeyspace(keyspaceName, cluster);		
	}

	public static Cluster getCluster() {
		return cluster;
	}

	public static Keyspace getKeyspace() {
		return keyspace;
	}

}
