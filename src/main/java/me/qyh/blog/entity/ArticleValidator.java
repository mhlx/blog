package me.qyh.blog.entity;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.StringUtils;

public class ArticleValidator implements Validator {

	private static final int MAX_SUMMARY_LENGTH = 2000;
	private static final int MAX_TITLE_LENGTH = 200;
	private static final int MAX_ALIAS_LENGTH = 200;
	private static final int MAX_CONTENT_LENGTH = 200000;
	private static final int MAX_LEVEL = 100;
	private static final int MIN_LEVEL = 1;
	private static final int MAX_PASSWORD_LENGTH = 25;
	private static final int MAX_TAGS_SIZE = 5;
	private static final int MAX_CATEGORIES_SIZE = 5;
	private static final String ALIAS_PATTERN = "^[0-9a-zA-Z_-]+$";
	private static final int MAX_FEATURE_IMAGE_LENGTH = 200;
	private static final String TAG_NAME_PATTERN = "^[A-Za-z0-9_-\\u4E00-\\u9FA5 ]+$";
	private static final int MAX_TAG_NAME_LENGTH = 20;

	@Override
	public boolean supports(Class<?> clazz) {
		return Article.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Article article = (Article) target;
		String title = article.getTitle();
		if (StringUtils.isNullOrBlank(title)) {
			errors.rejectValue("title", "NotBlank", "文章标题不能为空");
		} else {
			if (title.length() > MAX_TITLE_LENGTH) {
				errors.rejectValue("title", "Size", new Object[] { MAX_TITLE_LENGTH },
						"文章标题不能超过" + MAX_TITLE_LENGTH + "个字符");
			}
		}
		String content = article.getContent();
		if (StringUtils.isNullOrBlank(content)) {
			errors.rejectValue("content", "NotBlank", "文章内容不能为空");
		} else {
			if (content.length() > MAX_CONTENT_LENGTH) {
				errors.rejectValue("content", "Size", new Object[] { MAX_CONTENT_LENGTH },
						"文章内容不能超过" + MAX_CONTENT_LENGTH + "个字符");
			}
		}

		String alias = article.getAlias();
		if (alias != null) {
			if (alias.length() > MAX_ALIAS_LENGTH) {
				errors.rejectValue("alias", "Size", new Object[] { MAX_ALIAS_LENGTH },
						"文章别名不能超过" + MAX_ALIAS_LENGTH + "个字符");
			} else {
				if (!validateAlias(alias)) {
					errors.rejectValue("alias", "Pattern", "文章别名不能为纯数字且只能为大小写英文字母、数字以及-_且只能以字母或者数字开头或结尾");
				}
			}
		}

		if (article.getAllowComment() == null) {
			errors.rejectValue("allowComment", "NotNull", "文章是否允许评论不能为空");
		}

		if (article.getIsPrivate() == null) {
			errors.rejectValue("isPrivate", "NotNull", "文章是否私人访问不能为空");
		}

		Integer level = article.getLevel();
		if (level != null) {
			if (level > MAX_LEVEL || level < MIN_LEVEL) {
				errors.rejectValue("level", "Range", new Object[] { MIN_LEVEL, MAX_LEVEL },
						"文章置顶级别不能小于" + MIN_LEVEL + "并且不能大于" + MAX_LEVEL);
			}
		} else {
			article.setLevel(0);
		}

		String password = article.getPassword();
		if (password != null) {
			if (password.isBlank()) {
				errors.rejectValue("password", "NotBlank", "文章密码不能为空");
			} else {
				if (password.length() > MAX_PASSWORD_LENGTH) {
					errors.rejectValue("password", "Size", new Object[] { MAX_PASSWORD_LENGTH },
							"文章密码长度不能超过" + MAX_PASSWORD_LENGTH + "个字符");
				}
			}
		}

		String summary = article.getSummary();
		if (summary != null && summary.length() > MAX_SUMMARY_LENGTH) {
			errors.rejectValue("summary", "Size", new Object[] { MAX_SUMMARY_LENGTH },
					"文章摘要长度不能超过" + MAX_SUMMARY_LENGTH + "个字符");
		}

		ArticleStatus status = article.getStatus();
		if (status == null) {
			errors.rejectValue("status", "NotBlank", "文章状态不能为空");
		} else {
			if (ArticleStatus.SCHEDULED.equals(status)) {
				LocalDateTime pubDate = article.getPubDate();
				if (pubDate == null) {
					errors.rejectValue("pubDate", "NotBlank", "选择计划发表时，发表日期不能为空");
				} else {
					if (pubDate.isBefore(LocalDateTime.now())) {
						errors.rejectValue("pubDate", "Future", "计划发表日期不能小于当前日期");
					}
				}
			} else {
				article.setPubDate(null);
			}
		}

		Set<Tag> tags = article.getTags();
		if (!CollectionUtils.isEmpty(tags)) {
			if (tags.size() > MAX_TAGS_SIZE) {
				errors.rejectValue("tags", "Size", new Object[] { MAX_TAGS_SIZE }, "文章标签不能超过" + MAX_TAGS_SIZE + "个");
			} else {
				int i = 0;
				for (Tag tag : tags) {
					if (tag == null || StringUtils.isNullOrBlank(tag.getName())) {
						errors.rejectValue("tags[" + i + "]", "NotBlank", "不支持空标签");
						break;
					}
					if (tag.getName().length() > MAX_TAG_NAME_LENGTH) {
						errors.rejectValue("tags[" + i + "]", "Size", new Object[] { MAX_TAG_NAME_LENGTH },
								"单个标签不能超过" + MAX_TAG_NAME_LENGTH + "个字符");
						break;
					}
					if (!tag.getName().matches(TAG_NAME_PATTERN)) {
						errors.rejectValue("tags[" + i + "]", "Invalid", "标签名只能是中英文字符、数字以及_-和空格");
						break;
					}
					i++;
				}
			}
		}

		Set<Category> categories = article.getCategories();
		if (CollectionUtils.isEmpty(categories)) {

			if (!ArticleStatus.DRAFT.equals(article.getStatus())) {
				errors.rejectValue("categories", "NotEmpty", "必须为文章指定一个分类");
			}

		} else {
			if (categories.size() > MAX_CATEGORIES_SIZE) {
				errors.rejectValue("categories", "Size", new Object[] { MAX_CATEGORIES_SIZE },
						"文章分类不能超过" + MAX_CATEGORIES_SIZE + "个");
			} else {
				int i = 0;
				for (Category category : categories) {
					if (category == null || category.getId() == null) {
						errors.rejectValue("categories[" + i + "]", "NotBlank", "不支持空分类");
						break;
					}
					i++;
				}
			}
		}

		if (article.getFeatureImage() != null) {
			String featureImage = article.getFeatureImage();
			if (!org.springframework.util.StringUtils.startsWithIgnoreCase(featureImage, "https://")
					&& !org.springframework.util.StringUtils.startsWithIgnoreCase(featureImage, "http://")) {
				featureImage = "/" + FileUtils.cleanPath(featureImage);
			}
			if (featureImage.length() > MAX_FEATURE_IMAGE_LENGTH) {
				errors.rejectValue("featureImage", "Size", new Object[] { MAX_FEATURE_IMAGE_LENGTH },
						"文章特征图像地址不能超过" + MAX_FEATURE_IMAGE_LENGTH + "个字符");
			}
			article.setFeatureImage(featureImage);
		}
	}

	private boolean validateAlias(String alias) {
		if (alias.isEmpty()) {
			return false;
		}
		try {
			Integer.parseInt(alias);
			return false;
		} catch (NumberFormatException e) {
		}
		if (!alias.matches(ALIAS_PATTERN)) {
			return false;
		}
		char first = alias.charAt(0);
		if (!Character.isLetterOrDigit(first)) {
			return false;
		}
		char last = alias.charAt(alias.length() - 1);
		if (!Character.isLetterOrDigit(last)) {
			return false;
		}

		return true;
	}
}
