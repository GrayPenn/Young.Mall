package com.young.mall.redis;

public class OrderKey extends BasePrefix {

	public OrderKey(String prefix) {
		super(prefix);
	}
	public static OrderKey getMallOrderByUidGid = new OrderKey("moug");
}
