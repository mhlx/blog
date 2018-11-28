package me.qyh.blog.template.validator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.core.util.Validators;
import me.qyh.blog.template.entity.Fragment;
import me.qyh.blog.template.entity.Page;
import me.qyh.blog.template.vo.ExportPage;
import me.qyh.blog.template.vo.ExportPages;

@Component
public class ExportPagesValidator implements Validator {

	@Autowired
	private FragmentValidator fragmentValidator;

	@Override
	public boolean supports(Class<?> clazz) {
		return ExportPages.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ExportPages pages = (ExportPages) target;
		List<ExportPage> pageList = pages.getPages();

		if (CollectionUtils.isEmpty(pageList)) {
			errors.reject("template.import.empty", "要导入的模板不能为空");
			return;
		}

		for (ExportPage ep : pageList) {
			Page page = ep.getPage();
			if (page == null) {
				errors.reject("template.import.page.null", "要导入的页面不能为空");
				return;
			}
			validatePage(page, errors);
			if (errors.hasErrors()) {
				return;
			}
			List<Fragment> fragments = ep.getFragments();
			if (fragments == null) {
				ep.setFragments(new ArrayList<>());
			} else {
				for (Fragment fragment : fragments) {
					fragmentValidator.validate(fragment, errors);
					if (errors.hasErrors()) {
						return;
					}
				}
			}
		}

	}

	private void validatePage(Page page, Errors errors) {
		String name = page.getName();
		if (Validators.isEmptyOrNull(name, true)) {
			errors.reject("page.name.blank", "页面名称不能为空");
			return;
		}
		if (name.length() > PageValidator.PAGE_NAME_MAX_LENGTH) {
			errors.reject("page.name.toolong", new Object[] { PageValidator.PAGE_NAME_MAX_LENGTH },
					"页面名称不能超过" + PageValidator.PAGE_NAME_MAX_LENGTH + "个字符");
			return;
		}
		String pageTpl = page.getTpl();
		if (Validators.isEmptyOrNull(pageTpl, true)) {
			errors.reject("page.tpl.null", "页面模板不能为空");
			return;
		}
		if (pageTpl.length() > PageValidator.PAGE_TPL_MAX_LENGTH) {
			errors.reject("page.tpl.toolong", new Object[] { PageValidator.PAGE_TPL_MAX_LENGTH },
					"页面模板不能超过" + PageValidator.PAGE_TPL_MAX_LENGTH + "个字符");
			return;
		}
		String alias = PageValidator.validateAlias(page.getAlias(), errors);
		if (errors.hasErrors()) {
			return;
		}
		page.setAlias(alias);
	}

}
