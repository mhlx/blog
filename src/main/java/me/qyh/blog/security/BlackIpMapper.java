package me.qyh.blog.security;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlackIpMapper {

	void insert(String ip);

	void delete(String ip);

	List<String> selectAll();
}
