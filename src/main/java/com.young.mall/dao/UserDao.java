package com.young.mall.dao;

import com.young.mall.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

@Mapper
public interface UserDao {
	
	//@Select("select * from user where id = #{id}")
	public User getById(@Param("id") int id);

	@Insert("insert into user(id, name)values(#{id}, #{name})")
	public int insert(User user);
	
}
