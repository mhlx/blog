package me.qyh.blog.core.entity;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import me.qyh.blog.core.message.Message;
import me.qyh.blog.core.util.Validators;

/**
 * 
 * @author Administrator
 *
 */
public class Article extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Space space;// 空间
	private String title;// 标题
	private String content;// 博客原始内容
	private Set<Tag> tags = new LinkedHashSet<>();// 博客标签
	private Timestamp pubDate;// 撰写日期
	private Timestamp lastModifyDate;// 最后修改日期
	private Boolean isPrivate;// 是否是私人博客
	private int hits;// 点击数量
	private int comments;// 评论数量
	private ArticleFrom from;// 博客来源
	private ArticleStatus status;// 博客状态
	private Editor editor;// 编辑器
	private String summary;// 博客摘要
	private Integer level; // 博客级别，级别越高显示越靠前
	private String alias;// 别名，可通过别名访问文章
	private Boolean allowComment;
	private String lockId;

	/**
	 * <p>
	 * 文章特征图像
	 * </p>
	 * 
	 * @since 5.5.3
	 */
	private String featureImage;

	/**
	 * 文章来源
	 * 
	 * @author Administrator
	 *
	 */
	public enum ArticleFrom {
		// 原创
		ORIGINAL(new Message("article.from.original", "原创")),
		// 转载
		COPIED(new Message("article.from.copied", "转载"));

		private Message message;

		ArticleFrom(Message message) {
			this.message = message;
		}

		ArticleFrom() {

		}

		public Message getMessage() {
			return message;
		}
	}

	/**
	 * 文章状态
	 * 
	 * @author Administrator
	 *
	 */
	public enum ArticleStatus {
		// 正常
		PUBLISHED(new Message("article.status.published", "已发布")),
		// 计划博客
		SCHEDULED(new Message("article.status.schedule", "计划中")),
		// 草稿
		DRAFT(new Message("article.status.draft", "草稿")),
		// 回收站
		DELETED(new Message("article.status.deleted", "已删除"));

		private Message message;

		ArticleStatus(Message message) {
			this.message = message;
		}

		ArticleStatus() {

		}

		public Message getMessage() {
			return message;
		}
	}

	/**
	 * default
	 */
	public Article() {
		super();
	}

	/**
	 * 
	 * @param id
	 *            文章id
	 */
	public Article(Integer id) {
		super(id);
	}

	/**
	 * clone
	 * 
	 * @param source
	 *            源文章
	 */
	public Article(Article source) {
		this.alias = source.alias;
		this.comments = source.comments;
		this.content = source.content;
		this.editor = source.editor;
		this.from = source.from;
		this.hits = source.hits;
		this.isPrivate = source.isPrivate;
		this.lastModifyDate = source.lastModifyDate;
		this.level = source.level;
		this.pubDate = source.pubDate;
		this.space = source.space;
		this.status = source.status;
		this.summary = source.summary;
		this.tags = source.tags;
		this.title = source.title;
		this.id = source.id;
		this.allowComment = source.allowComment;
		this.featureImage = source.featureImage;
		this.lockId = source.lockId;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Timestamp getPubDate() {
		return pubDate;
	}

	public void setPubDate(Timestamp pubDate) {
		this.pubDate = pubDate;
	}

	public Timestamp getLastModifyDate() {
		return lastModifyDate;
	}

	public void setLastModifyDate(Timestamp lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
	}

	public Boolean isPrivate() {
		if (isPrivate == null) {
			return false;
		}
		if (!isPrivate && (space != null && space.getIsPrivate() != null)) {
			return space.getIsPrivate();
		}
		return isPrivate;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public ArticleFrom getFrom() {
		return from;
	}

	public void setFrom(ArticleFrom from) {
		this.from = from;
	}

	public ArticleStatus getStatus() {
		return status;
	}

	public void setStatus(ArticleStatus status) {
		this.status = status;
	}

	public Editor getEditor() {
		return editor;
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public boolean isSchedule() {
		return ArticleStatus.SCHEDULED.equals(status);
	}

	public boolean isDeleted() {
		return ArticleStatus.DELETED.equals(status);
	}

	public boolean isPublished() {
		return ArticleStatus.PUBLISHED.equals(status);
	}

	public boolean isDraft() {
		return ArticleStatus.DRAFT.equals(status);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTagStr() {
		return tags.stream().map(Tag::getName).collect(Collectors.joining(","));
	}

	public boolean hasLock() {
		if (lockId != null) {
			return true;
		}
		return space != null && space.hasLock();
	}

	/**
	 * 是否包含某个标签
	 * 
	 * @param tag
	 *            标签
	 * @return true包含，false不包含
	 */
	public boolean hasTag(String tag) {
		return this.tags.stream().anyMatch(_tag -> Jsoup.clean(_tag.getName(), Whitelist.none()).equalsIgnoreCase(tag));
	}

	public Optional<Tag> getTag(String name) {
		return this.tags.stream().filter(tag -> name.equalsIgnoreCase(Jsoup.clean(tag.getName(), Whitelist.none())))
				.findAny();
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Boolean getAllowComment() {
		return allowComment;
	}

	public void setAllowComment(Boolean allowComment) {
		this.allowComment = allowComment;
	}

	public String getFeatureImage() {
		return featureImage;
	}

	public void setFeatureImage(String featureImage) {
		this.featureImage = featureImage;
	}

	public String getLockId() {
		return lockId;
	}

	public void setLockId(String lockId) {
		this.lockId = lockId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (Validators.baseEquals(this, obj)) {
			Article rhs = (Article) obj;
			return Objects.equals(this.id, rhs.id);
		}
		return false;
	}
}
