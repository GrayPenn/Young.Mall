package com.young.mall.redis;

public class MallUserKey extends BasePrefix{

	public static final int TOKEN_EXPIRE = 3600*24 * 2;
	private String prefix ;
	private MallUserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
		this.prefix = prefix;
	}
	public static MallUserKey token = new MallUserKey(TOKEN_EXPIRE, "tk");
	
	public static MallUserKey getById = new MallUserKey(0, "id");
	
	public MallUserKey withExpire(int seconds) {
		return new MallUserKey(seconds, prefix);
	}
}
