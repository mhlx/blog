package me.qyh.blog.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import me.qyh.blog.entity.Tag;

@Mapper
public interface TagMapper {

	Optional<Tag> selectById(int id);

	List<Tag> selectAll();

	Optional<Tag> selectByName(String name);

	void deleteById(int id);

	void insert(Tag tag);

	void update(Tag tag);

}
