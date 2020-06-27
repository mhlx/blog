package me.qyh.blog.mapper;

import me.qyh.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CategoryMapper {

    Optional<Category> selectById(int id);

    List<Category> selectAll();

    Optional<Category> selectByName(String name);

    void deleteById(int id);

    void insert(Category category);

    void update(Category category);

}
