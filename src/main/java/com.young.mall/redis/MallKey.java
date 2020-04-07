package com.young.mall.redis;

public class MallKey extends BasePrefix{

	private MallKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static MallKey isGoodsOver = new MallKey(0, "go");
	public static MallKey getMiaoshaPath = new MallKey(60, "mp");
	public static MallKey getMiaoshaVerifyCode = new MallKey(300, "vc");
}
