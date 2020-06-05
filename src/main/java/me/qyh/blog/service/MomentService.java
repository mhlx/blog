package me.qyh.blog.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.BlogContext;
import me.qyh.blog.Markdown2Html;
import me.qyh.blog.entity.Comment;
import me.qyh.blog.entity.CommentModule;
import me.qyh.blog.entity.Moment;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.mapper.CommentMapper;
import me.qyh.blog.mapper.MomentMapper;
import me.qyh.blog.security.SecurityChecker;
import me.qyh.blog.utils.JsoupUtils;
import me.qyh.blog.vo.MomentArchive;
import me.qyh.blog.vo.MomentArchiveQueryParam;
import me.qyh.blog.vo.MomentQueryParam;
import me.qyh.blog.vo.MomentStatistic;
import me.qyh.blog.vo.PageResult;

@Component
public class MomentService implements CommentModuleHandler<Moment> {

	private final MomentMapper momentMapper;
	private final Markdown2Html markdown2Html;
	private final CommentMapper commentMapper;

	private static final String COMMENT_MODULE_NAME = "moment";

	public MomentService(MomentMapper momentMapper, Markdown2Html markdown2Html, CommentMapper commentMapper) {
		this.momentMapper = momentMapper;
		this.markdown2Html = markdown2Html;
		this.commentMapper = commentMapper;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public int saveMoment(Moment moment) {
		momentMapper.insert(moment);
		return moment.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateMoment(Moment moment) {
		momentMapper.selectById(moment.getId())
				.orElseThrow(() -> new ResourceNotFoundException("moment.notExists", "动态不存在"));
		moment.setModifyTime(LocalDateTime.now());
		momentMapper.update(moment);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteMoment(int id) {
		momentMapper.deleteById(id);
		commentMapper.deleteByModule(new CommentModule(COMMENT_MODULE_NAME, id));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void hit(int id) {
		if (BlogContext.isAuthenticated()) {
			return;
		}
		Optional<Moment> opMoment = momentMapper.selectById(id);
		if (opMoment.isEmpty()) {
			throw new ResourceNotFoundException("moment.notExists", "动态不存在");
		}
		Moment moment = opMoment.get();
		SecurityChecker.check(moment);
		momentMapper.increaseHits(id, 1);
	}

	@Transactional(readOnly = true)
	public Optional<Moment> prev(int id) {
		return prevOrNext(id, true);
	}

	@Transactional(readOnly = true)
	public Optional<Moment> next(int id) {
		return prevOrNext(id, false);
	}

	private Optional<Moment> prevOrNext(int id, boolean prev) {
		Optional<Moment> opCurrent = momentMapper.selectById(id);
		if (opCurrent.isPresent()) {
			SecurityChecker.check(opCurrent.get());
			boolean queryPrivate = BlogContext.isAuthenticated();
			Optional<Moment> opMoment = prev ? momentMapper.selectPrev(opCurrent.get(), queryPrivate)
					: momentMapper.selectNext(opCurrent.get(), queryPrivate);
			opMoment.ifPresent(this::processMoment);
			return opMoment;
		}
		return Optional.empty();
	}

	@Transactional(readOnly = true)
	public Optional<Moment> getMoment(int id) {
		Optional<Moment> opMoment = momentMapper.selectById(id);
		if (opMoment.isPresent()) {
			Moment moment = opMoment.get();
			SecurityChecker.check(moment);
			processMoments(List.of(moment));
			return Optional.of(moment);
		}
		return opMoment;
	}

	@Transactional(readOnly = true)
	public MomentStatistic getMomentStatistic() {
		return momentMapper.selectStatistic(BlogContext.isAuthenticated());
	}

	@Transactional(readOnly = true)
	public Optional<Moment> getMomentForEdit(int id) {
		return momentMapper.selectById(id);
	}

	@Transactional(readOnly = true)
	public PageResult<MomentArchive> queryMomentArchive(MomentArchiveQueryParam param) {
		if (!BlogContext.isAuthenticated()) {
			param.setQueryPasswordProtected(false);
			param.setQueryPrivate(false);
		}
		int count = momentMapper.selectDaysCount(param);
		List<LocalDate> days = momentMapper.selectDays(param);
		int size = days.size();
		if (size == 0) {
			return new PageResult<>(param, count, Collections.emptyList());
		}
		LocalDateTime begin, end;
		if (size == 1) {
			LocalDate localDate = days.get(0);
			begin = localDate.atStartOfDay();
			end = localDate.plusDays(1).atStartOfDay();
		} else {
			LocalDate max, min;
			if (param.isAsc()) {
				max = days.get(size - 1);
				min = days.get(0);
			} else {
				max = days.get(0);
				min = days.get(size - 1);
			}
			begin = min.atStartOfDay();
			end = max.plusDays(1).atStartOfDay();
		}

		MomentQueryParam np = new MomentQueryParam();
		np.setIgnorePaging(true);
		np.setBegin(begin);
		np.setEnd(end);
		np.setQuery(param.getQuery());
		np.setAsc(param.isAsc());
		np.setQueryPrivate(param.isQueryPrivate());
		np.setQueryPasswordProtected(param.isQueryPasswordProtected());

		List<Moment> moments = momentMapper.selectPage(np);

		processMoments(moments);

		Map<LocalDate, List<Moment>> map = moments.stream().collect(Collectors.groupingBy(moment -> {
			return moment.getTime().toLocalDate();
		}));

		List<MomentArchive> archives = days.stream().map(d -> new MomentArchive(d, map.get(d)))
				.collect(Collectors.toList());

		return new PageResult<>(param, count, archives);
	}

	@Override
	public Moment checkBeforeQuery(CommentModule module) {
		Moment moment = doCheck(module);
		Moment rt = new Moment();
		rt.setId(moment.getId());
		return rt;
	}

	@Override
	public void checkBeforeSave(Comment comment, CommentModule module) {
		Moment moment = doCheck(module);
		if (!BlogContext.isAuthenticated() && !moment.getAllowComment()) {
			throw new LogicException("moment.disableComment", "动态禁止评论");
		}
	}

	private Moment doCheck(CommentModule module) {
		Moment moment = momentMapper.selectById(module.getId())
				.orElseThrow(() -> new ResourceNotFoundException("moment.notExists", "动态不存在"));
		SecurityChecker.check(moment);
		return moment;
	}

	private void processMoment(Moment moment) {
		processMoments(List.of(moment));
	}

	private void processMoments(List<Moment> moments) {
		if (!BlogContext.isAuthenticated()) {
			moments.stream().filter(SecurityChecker::locked).forEach(Moment::clearProtected);
		}
		Map<Integer, String> contentMap = moments.stream().filter(m -> m.getContent() != null)
				.collect(Collectors.toMap(Moment::getId, Moment::getContent));
		if (contentMap.isEmpty()) {
			return;
		}
		Map<Integer, String> markdownMap = markdown2Html.toHtmls(contentMap);
		moments.forEach(m -> {
			m.setContent(markdownMap.get(m.getId()));
			JsoupUtils.getFirstImage(m.getContent()).ifPresent(m::setFirstImage);
		});
	}

	@Override
	public String getModuleName() {
		return COMMENT_MODULE_NAME;
	}
}
