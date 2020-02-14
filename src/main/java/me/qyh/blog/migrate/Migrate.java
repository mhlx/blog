package me.qyh.blog.migrate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Article.ArticleStatus;
import me.qyh.blog.entity.ArticleCategory;
import me.qyh.blog.entity.ArticleTag;
import me.qyh.blog.entity.Category;
import me.qyh.blog.entity.Moment;
import me.qyh.blog.entity.Tag;
import me.qyh.blog.mapper.ArticleCategoryMapper;
import me.qyh.blog.mapper.ArticleMapper;
import me.qyh.blog.mapper.ArticleTagMapper;
import me.qyh.blog.mapper.CategoryMapper;
import me.qyh.blog.mapper.MomentMapper;
import me.qyh.blog.mapper.TagMapper;

//7.0->8.0
@Service
public class Migrate {

	@Autowired
	private ArticleMapper articleMapper;
	@Autowired
	private CategoryMapper categoryMapper;
	@Autowired
	private TagMapper tagMapper;
	@Autowired
	private ArticleCategoryMapper articleCategoryMapper;
	@Autowired
	private ArticleTagMapper articleTagMapper;
	@Autowired
	private MomentMapper momentMapper;
	@Autowired
	private DataSource dataSource;

	private Map<Integer, Integer> tagMapping = new HashMap<>();
	private Map<Integer, Integer> categoryMapping = new HashMap<>();
	private Map<Integer, Integer> articleMapping = new HashMap<>();
	private Map<Integer, Integer> momentMapping = new HashMap<>();

	@Transactional(propagation = Propagation.REQUIRED)
	public void migrate(Connection fromOldDataBase, String password) throws SQLException {
		clear();
		migrateCategory(fromOldDataBase);
		migrateTag(fromOldDataBase);
		migrateArticle(fromOldDataBase, password);
		migrateMoment(fromOldDataBase, password);
		migrateComment(fromOldDataBase);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void migrateComment(Connection fromOldDataBase, String module, Integer moduleId, String newModule,
			String newModuleId, boolean delete) throws SQLException {
		doMigrateComment(fromOldDataBase, module, moduleId, newModule, newModuleId, delete);
	}

	private static final String[] clearSqls = new String[] { "delete from blog_category",
			"delete from blog_article_category", "delete from blog_tag", "delete from blog_article_tag",
			"delete from blog_article", "delete from blog_moment", "delete from blog_comment",
			"delete from blog_template" };

	private void clear() throws SQLException {
		try (Connection conn = dataSource.getConnection()) {
			for (String clearSql : clearSqls) {
				try (PreparedStatement ps = conn.prepareStatement(clearSql)) {
					ps.execute();
				}
			}
		}
	}

	private void doMigrateComment(Connection fromOldDataBase, String module, Integer moduleId, String newModule,
			String newModuleId, boolean delete) throws SQLException {
		String insertCommentSql = "insert into blog_comment(id,parent,parentPath,content,module_name,module_id,createTime,checking,nickname,email,ip,admin,gravatar,website) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try (Connection conn2 = dataSource.getConnection()) {
			conn2.setAutoCommit(false);
			if (delete) {
				try (PreparedStatement ps2 = conn2
						.prepareStatement("delete from blog_comment where module_name = ? and module_id = ?")) {
					ps2.setString(1, newModule);
					ps2.setString(2, newModuleId);
					ps2.execute();
				}
			}
			try (PreparedStatement ps = fromOldDataBase
					.prepareStatement("select * from blog_comment where module_type = ? and module_id = ?")) {
				ps.setString(1, module);
				ps.setInt(2, moduleId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						Integer id = rs.getInt("id");
						Integer parentId = rs.getInt("parent_id");
						String parentPath = rs.getString("parent_path");
						String nickname = rs.getString("comment_nickname");
						String email = rs.getString("comment_email");
						String ip = rs.getString("comment_ip");
						boolean admin = rs.getBoolean("comment_admin");
						String content = rs.getString("content");
						Timestamp date = rs.getTimestamp("comment_date");
						int status = rs.getInt("comment_status");
						String website = rs.getString("comment_website");
						String gravatar = rs.getString("COMMENT_GRAVATAR");

						try (PreparedStatement ps2 = conn2.prepareStatement(insertCommentSql)) {
							ps2.setInt(1, id);
							ps2.setInt(2, parentId);
							ps2.setString(3, parentPath);
							ps2.setString(4, content);
							ps2.setString(5, newModule);
							ps2.setNString(6, newModuleId);
							ps2.setTimestamp(7, date);
							ps2.setBoolean(8, status == 0 ? false : true);
							ps2.setString(9, nickname);
							ps2.setString(10, email);
							ps2.setString(11, ip);
							ps2.setBoolean(12, admin);
							ps2.setString(13, gravatar);
							ps2.setString(14, website);
							ps2.execute();
						}
					}
				}
			}
			conn2.commit();
		}
	}

	private void migrateComment(Connection fromOldDataBase) throws SQLException {
		String insertCommentSql = "insert into blog_comment(id,parent,parentPath,content,module_name,module_id,createTime,checking,nickname,email,ip,admin,gravatar,website) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try (Connection conn2 = dataSource.getConnection()) {
			conn2.setAutoCommit(false);
			try (PreparedStatement ps = fromOldDataBase.prepareStatement("select * from blog_comment")) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						Integer id = rs.getInt("id");
						Integer parentId = rs.getInt("parent_id");
						String parentPath = rs.getString("parent_path");
						String nickname = rs.getString("comment_nickname");
						String email = rs.getString("comment_email");
						String ip = rs.getString("comment_ip");
						boolean admin = rs.getBoolean("comment_admin");
						String content = rs.getString("content");
						String moduleId = rs.getString("module_id");
						String moduleType = rs.getString("module_type");
						Timestamp date = rs.getTimestamp("comment_date");
						int status = rs.getInt("comment_status");
						String website = rs.getString("comment_website");
						String gravatar = rs.getString("COMMENT_GRAVATAR");

						String moduleName;
						String _moduleId;
						// userpage
						// article
						// news
						if ("news".equals(moduleType)) {
							moduleName = "moment";
							_moduleId = momentMapping.get(Integer.parseInt(moduleId)).toString();
						} else if ("article".equals(moduleType)) {
							moduleName = "article";
							_moduleId = articleMapping.get(Integer.parseInt(moduleId)).toString();
						} else {
							continue;// handler userpage later;
						}
						try (PreparedStatement ps2 = conn2.prepareStatement(insertCommentSql)) {
							ps2.setInt(1, id);
							ps2.setInt(2, parentId);
							ps2.setString(3, parentPath);
							ps2.setString(4, content);
							ps2.setString(5, moduleName);
							ps2.setNString(6, _moduleId);
							ps2.setTimestamp(7, date);
							ps2.setBoolean(8, status == 0 ? false : true);
							ps2.setString(9, nickname);
							ps2.setString(10, email);
							ps2.setString(11, ip);
							ps2.setBoolean(12, admin);
							ps2.setString(13, gravatar);
							ps2.setString(14, website);
							ps2.execute();
						}
					}
				}
			}
			conn2.commit();
		}
	}

	private void migrateMoment(Connection fromOldDataBase, String password) throws SQLException {
		try (PreparedStatement ps = fromOldDataBase.prepareStatement("select * from blog_news")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {

					Moment moment = new Moment();
					moment.setAllowComment(rs.getBoolean("news_allowComment"));
					moment.setContent(rs.getString("news_content"));
					moment.setTime(rs.getTimestamp("news_write").toLocalDateTime());
					Timestamp update = rs.getTimestamp("news_update");
					if (update != null) {
						moment.setModifyTime(update.toLocalDateTime());
					}

					String lock = rs.getString("news_lock");
					if (lock != null) {
						moment.setPassword(password);
					}
					moment.setIsPrivate(rs.getBoolean("news_private"));
					momentMapper.insert(moment);
					momentMapping.put(rs.getInt("id"), moment.getId());
					momentMapper.increaseHits(moment.getId(), rs.getInt("news_hits"));
				}
			}
		}
	}

	private void migrateArticle(Connection fromOldDataBase, String password) throws SQLException {
		try (PreparedStatement ps = fromOldDataBase.prepareStatement("select * from blog_article")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Article article = new Article();
					article.setAlias(rs.getString("art_alias"));
					article.setAllowComment(rs.getBoolean("allowComment"));
					Integer level = rs.getObject("art_level", Integer.class);
					article.setLevel(level == null ? 0 : level);
					article.setIsPrivate(rs.getBoolean("isPrivate"));
					Timestamp pub = rs.getTimestamp("pubDate");
					if (pub != null)
						article.setPubDate(pub.toLocalDateTime());
					Timestamp modify = rs.getTimestamp("lastModifyDate");
					if (modify != null)
						article.setLastModifyDate(modify.toLocalDateTime());
					int statusOridinal = rs.getInt("art_status");
					ArticleStatus status;
					switch (statusOridinal) {
					case 0:
						status = ArticleStatus.PUBLISHED;
						break;
					case 1:
						status = ArticleStatus.SCHEDULED;
						break;
					default:
						status = ArticleStatus.DRAFT;
						break;
					}
					article.setStatus(status);
					article.setContent(rs.getString("content"));
					article.setSummary(rs.getString("summary"));
					article.setTitle(rs.getString("title"));

					String lock = rs.getString("art_lock");
					if (lock != null) {
						article.setPassword(password);
					}

					articleMapper.insert(article);
					articleMapping.put(rs.getInt("id"), article.getId());
					articleMapper.increaseHits(article.getId(), rs.getInt("hits"));

					int spaceId = rs.getInt("space_id");
					int categoryId = categoryMapping.get(spaceId);

					ArticleCategory ac = new ArticleCategory(article.getId(), categoryId);
					articleCategoryMapper.insert(ac);

					try (PreparedStatement ps2 = fromOldDataBase
							.prepareStatement("select * from blog_article_tag where article_id = ?")) {
						ps2.setInt(1, rs.getInt("id"));

						try (ResultSet rs2 = ps2.executeQuery()) {
							while (rs2.next()) {
								int tagId = rs2.getInt("tag_id");
								articleTagMapper.insert(new ArticleTag(article.getId(), tagMapping.get(tagId)));
							}
						}
					}

				}
			}
		}
	}

	private void migrateCategory(Connection fromOldDataBase) throws SQLException {
		try (PreparedStatement ps = fromOldDataBase.prepareStatement("select * from blog_space")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String alias = rs.getString("space_alias");

					Optional<Category> opCategory = categoryMapper.selectByName(alias);
					if (opCategory.isEmpty()) {
						Category category = new Category();
						Timestamp timestamp = rs.getTimestamp("createDate");
						category.setCreateTime(timestamp.toLocalDateTime());
						category.setName(rs.getString("space_alias"));
						categoryMapper.insert(category);
						categoryMapping.put(rs.getInt("id"), category.getId());
					} else {
						categoryMapping.put(rs.getInt("id"), opCategory.get().getId());
					}

				}
			}
		}
	}

	private void migrateTag(Connection fromOldDataBase) throws SQLException {
		try (PreparedStatement ps = fromOldDataBase.prepareStatement("select * from blog_tag")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String name = rs.getString("tag_name");

					Optional<Tag> opTag = tagMapper.selectByName(name);
					if (opTag.isEmpty()) {
						Tag tag = new Tag();
						Timestamp timestamp = rs.getTimestamp("create_date");
						tag.setCreateTime(timestamp.toLocalDateTime());
						tag.setName(name);
						tagMapper.insert(tag);
						tagMapping.put(rs.getInt("id"), tag.getId());
					} else {
						tagMapping.put(rs.getInt("id"), opTag.get().getId());
					}
				}
			}
		}
	}

}
