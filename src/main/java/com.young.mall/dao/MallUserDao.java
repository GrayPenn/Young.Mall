package com.young.mall.dao;

import com.young.mall.entity.MallUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MallUserDao {

	@Select("select * from mall_user where id = #{id}")
	public MallUser getById(@Param("id") long id);

	@Update("update mall_user set password = #{password} where id = #{id}")
	public void update(MallUser toBeUpdate);
}
