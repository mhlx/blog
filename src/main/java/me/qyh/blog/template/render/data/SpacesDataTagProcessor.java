package me.qyh.blog.template.render.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.service.SpaceService;
import me.qyh.blog.core.vo.SpaceQueryParam;

/**
 * 查询所有的空间
 * 
 * @author mhlx
 *
 */
public class SpacesDataTagProcessor extends DataTagProcessor<List<Space>> {

	@Autowired
	private SpaceService spaceService;

	public SpacesDataTagProcessor(String name, String dataName) {
		super(name, dataName);
	}

	@Override
	protected List<Space> query(Attributes attributes) throws LogicException {
		return spaceService.querySpace(new SpaceQueryParam());
	}

	@Override
	public List<String> getAttributes() {
		return List.of();
	}

}
