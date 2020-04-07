package com.young.mall.vo;

import com.young.mall.entity.MallUser;

public class GoodsDetailVo {
	private int mallStatus = 0;
	private int remainSeconds = 0;
	private GoodsVo goods ;
	private MallUser user;
	public int getMallStatus() {
		return mallStatus;
	}
	public void setMallStatus(int mallStatus) {
		this.mallStatus = mallStatus;
	}
	public int getRemainSeconds() {
		return remainSeconds;
	}
	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}
	public GoodsVo getGoods() {
		return goods;
	}
	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}
	public MallUser getUser() {
		return user;
	}
	public void setUser(MallUser user) {
		this.user = user;
	}
}
