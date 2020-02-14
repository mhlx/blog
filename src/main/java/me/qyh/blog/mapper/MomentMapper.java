package me.qyh.blog.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import me.qyh.blog.entity.Moment;
import me.qyh.blog.vo.MomentArchiveQueryParam;
import me.qyh.blog.vo.MomentQueryParam;
import me.qyh.blog.vo.MomentStatistic;

@Mapper
public interface MomentMapper {

	Optional<Moment> selectById(int id);

	/**
	 * 查询符合条件的动态
	 * 
	 * @param param
	 * @return
	 */
	List<Moment> selectPage(MomentQueryParam param);

	/**
	 * 查询符合条件的动态数目
	 * 
	 * @param param
	 * @return
	 */
	int selectCount(MomentQueryParam param);

	/**
	 * 根据ID删除
	 * 
	 * @param id
	 */
	void deleteById(int id);

	void insert(Moment moment);

	void update(Moment moment);

	/**
	 * 分页查询动态归档日期
	 * 
	 * @param param
	 * @return
	 */
	List<LocalDate> selectDays(MomentArchiveQueryParam param);

	/**
	 * 查询动态归档日期数
	 * 
	 * @param param
	 * @return
	 */
	int selectDaysCount(MomentArchiveQueryParam param);

	void increaseHits(@Param("id") int id, @Param("hits") int i);

	Optional<Moment> selectPrev(@Param("moment") Moment moment, @Param("queryPrivate") boolean queryPrivate);

	Optional<Moment> selectNext(@Param("moment") Moment moment, @Param("queryPrivate") boolean queryPrivate);

	MomentStatistic selectStatistic(boolean queryPrivate);
}
