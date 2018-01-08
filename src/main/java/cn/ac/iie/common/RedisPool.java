package cn.ac.iie.common;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.concurrent.atomic.AtomicLong;

/**redis连接池**/
public class RedisPool {
	private MMConf conf;
	private JedisSentinelPool jsp = null;
	private JedisPool jp = null;
	private JedisCluster jc = null;
	private String masterName = null;
	private AtomicLong alloced = new AtomicLong(0);
	private AtomicLong balanceTarget = new AtomicLong(0);
	private String pid;
	
	public RedisPool(MMConf conf, String masterName) {
		this.conf = conf;
		this.masterName = masterName;
	}
	
	public void quit() {
		if (jsp != null)
			jsp.destroy();
	}
	
	public static Jedis getRawInstance(String host, int port) {
		return new Jedis(host, port);
	}
	
	public Jedis getResource() throws JedisException {
		switch (conf.getRedisMode()) {
		case STANDALONE:
			if (jp != null)
				return jp.getResource();
			else {
				JedisPoolConfig c = new JedisPoolConfig();
				jp = new JedisPool(c, conf.getHap().getHost(), conf.getHap().getPort(),
						conf.getRedisTimeout());
				System.out.println("New standalone pool @ " + conf.getHap().getHost() + 
						":" + conf.getHap().getPort());
				return jp.getResource();
			}
		case SENTINEL: {
			if (jsp != null)
				return jsp.getResource();
			else {
				JedisPoolConfig c = new JedisPoolConfig();
				if (conf.getAuthToken() != null)
					jsp = new JedisSentinelPool(masterName, conf.getSentinels(), c,
							conf.getRedisTimeout(), conf.getAuthToken());
				else
					jsp = new JedisSentinelPool(masterName, conf.getSentinels(), c,
							conf.getRedisTimeout());
				System.out.println("New sentinel pool @ " + masterName);
				return jsp.getResource();
			}
		}
		case CLUSTER: {
		}
		}
		return null;
	}
	
	public Jedis putInstance(Jedis j) {
		try {
			if (j == null)
				return null;
			switch (conf.getRedisMode()) {
			case STANDALONE:
				if (jp != null)
					jp.returnResourceObject(j);
				break;
			case SENTINEL:
				if (jsp != null)
					jsp.returnResourceObject(j);
				break;
			}
		} catch (Exception e) {
			jsp.destroy();
			jsp = null;
		}
		return null;
	}
	
	public void incrAlloced() {
		alloced.incrementAndGet();
	}

	public AtomicLong getAlloced() {
		return alloced;
	}

	public void setAlloced(AtomicLong alloced) {
		this.alloced = alloced;
	}

	public AtomicLong getBalanceTarget() {
		return balanceTarget;
	}

	public void setBalanceTarget(AtomicLong balanceTarget) {
		this.balanceTarget = balanceTarget;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}
}
