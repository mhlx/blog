package me.qyh.blog.core.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import me.qyh.blog.core.entity.Tag;
import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.Validators;

/**
 * 用来将mysql取出的tag 字符串转化为Set&lt;Tag&gt;
 * 
 * @author Administrator
 *
 */
public class TagsTypeHandler extends BaseTypeHandler<Set<Tag>> {
	@Override
	public Set<Tag> getNullableResult(ResultSet rs, String str) throws SQLException {
		return toTags(rs.getString(str));
	}

	@Override
	public Set<Tag> getNullableResult(ResultSet rs, int pos) throws SQLException {
		return toTags(rs.getString(pos));
	}

	@Override
	public void setNonNullParameter(PreparedStatement arg0, int arg1, Set<Tag> arg2, JdbcType arg3)
			throws SQLException {
		throw new SystemException("不支持这个方法");
	}

	private Set<Tag> toTags(String str) {
		if (Validators.isEmptyOrNull(str, true)) {
			return new HashSet<>();
		}
		return Arrays.stream(str.split(",")).sorted().map(Tag::new)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Set<Tag> getNullableResult(CallableStatement arg0, int arg1) throws SQLException {
		throw new SystemException("不支持这个方法");
	}

}
