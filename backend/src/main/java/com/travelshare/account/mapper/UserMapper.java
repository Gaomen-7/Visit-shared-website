package com.travelshare.account.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travelshare.account.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
