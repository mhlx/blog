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

	List<Moment> selectPage(MomentQueryParam param);

	int selectCount(MomentQueryParam param);

	void deleteById(int id);

	void insert(Moment moment);

	void update(Moment moment);

	List<LocalDate> selectDays(MomentArchiveQueryParam param);

	int selectDaysCount(MomentArchiveQueryParam param);

	void increaseHits(@Param("id") int id, @Param("hits") int i);

	Optional<Moment> selectPrev(@Param("moment") Moment moment, @Param("queryPrivate") boolean queryPrivate);

	Optional<Moment> selectNext(@Param("moment") Moment moment, @Param("queryPrivate") boolean queryPrivate);

	MomentStatistic selectStatistic(boolean queryPrivate);
}
