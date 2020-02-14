package me.qyh.blog.web.backend.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.qyh.blog.entity.Moment;
import me.qyh.blog.exception.LogicException;
import me.qyh.blog.exception.ResourceNotFoundException;
import me.qyh.blog.service.MomentService;
import me.qyh.blog.vo.MomentArchiveQueryParam;

@Controller
@RequestMapping("console")
public class MomentBackgroudController {

	private final MomentService momentService;

	public MomentBackgroudController(MomentService momentService) {
		super();
		this.momentService = momentService;
	}

	@ResponseBody
	@PostMapping("moment/save")
	public int create(@Valid @RequestBody Moment moment) {
		return momentService.saveMoment(moment);
	}

	@ResponseBody
	@PostMapping("moments/{id}/update")
	public void updateMoment(@Valid @RequestBody Moment moment) {
		momentService.updateMoment(moment);
	}

	@ResponseBody
	@PostMapping("moments/{id}")
	public Moment get(@PathVariable("id") int id) {
		return momentService.getMoment(id).orElseThrow(() -> new LogicException("moment.notExists", "动态不存在"));
	}

	@GetMapping("moment/write")
	public String write() {
		return "console/moment/write";
	}

	@GetMapping("moments/{id}/edit")
	public String edit(@PathVariable("id") int id, Model model) {
		model.addAttribute("moment", momentService.getMomentForEdit(id)
				.orElseThrow(() -> new ResourceNotFoundException("moment.notExists", "动态不存在")));
		return "console/moment/edit";
	}

	@PostMapping("moments/{id}/delete")
	@ResponseBody
	public void delete(@PathVariable("id") int id) {
		momentService.deleteMoment(id);
	}

	@GetMapping("moments")
	public String index(@Valid MomentArchiveQueryParam param, Model model) {
		if (!param.hasPageSize()) {
			param.setPageSize(10);
		}
		param.setQueryPrivate(true);
		param.setQueryPasswordProtected(true);
		model.addAttribute("page", momentService.queryMomentArchive(param));
		model.addAttribute("statistic", momentService.getMomentStatistic());
		return "console/moment/index";
	}

}
