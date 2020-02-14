package me.qyh.blog.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.vo.CommentQueryParam;

@Mapper
public interface CommentMapper {

	Optional<Comment> selectById(int id);

	boolean isIpCommentsAnyChecked(String ip);

	Optional<Comment> selectLastByModuleAndIp(@Param("module") CommentModule module, @Param("ip") String ip);

	void insert(Comment comment);

	void update(Comment comment);

	void deleteByModule(CommentModule module);

	int selectCount(CommentQueryParam param);

	List<Comment> selectPage(CommentQueryParam param);

	List<Comment> selectChildren(@Param("parent") int id, @Param("checking") Boolean checking);

	void deleteById(int id);

	void deleteChildren(int id);

	Integer selectRank(CommentQueryParam param);

}
