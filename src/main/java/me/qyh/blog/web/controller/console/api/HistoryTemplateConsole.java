package me.qyh.blog.web.controller.console.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import me.qyh.blog.core.exception.LogicException;
import me.qyh.blog.core.message.Message;
import me.qyh.blog.template.entity.HistoryTemplate;
import me.qyh.blog.template.service.TemplateService;
import me.qyh.blog.web.controller.console.BaseMgrController;

@Controller
@RequestMapping("api/console/template")
public class HistoryTemplateConsole extends BaseMgrController {

	private static final int MAX_REMARK_LENGTH = 500;

	@Autowired
	private TemplateService templateService;

	@GetMapping("history/{id}")
	public ResponseEntity<HistoryTemplate> getHistoryTemplate(@PathVariable("id") Integer id) {
		return ResponseEntity.of(templateService.getHistoryTemplate(id));
	}

	@DeleteMapping("history/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Integer id) throws LogicException {
		templateService.deleteHistoryTemplate(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("history/{id}")
	public ResponseEntity<Void> update(@PathVariable("id") Integer id, @RequestParam("remark") String remark)
			throws LogicException {
		Optional<Message> optionalError = validRemark(remark);
		if (optionalError.isPresent()) {
			throw new LogicException(optionalError.get());
		}
		templateService.updateHistoryTemplate(id, remark);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 校验模板备注，
	 * 
	 * @param remark
	 * @return 错误信息
	 */
	static Optional<Message> validRemark(String remark) {
		Message message = null;
		if (remark == null) {
			message = new Message("historyTemplate.remark.blank", "备注不能为空");
		} else if (remark.length() > MAX_REMARK_LENGTH) {
			message = new Message("historyTemplate.remark.toolong", "备注不能超过" + MAX_REMARK_LENGTH + "个字符",
					MAX_REMARK_LENGTH);
		}
		return Optional.ofNullable(message);
	}

}
