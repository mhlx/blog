package me.qyh.blog.security;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BlackIpMapper {

    void insert(String ip);

    void delete(String ip);

    List<String> selectAll();
}
