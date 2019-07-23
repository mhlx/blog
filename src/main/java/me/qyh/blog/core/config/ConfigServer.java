package me.qyh.blog.core.config;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import me.qyh.blog.core.exception.SystemException;
import me.qyh.blog.core.util.FileUtils;
import me.qyh.blog.core.util.Resources;

@Service
public class ConfigServer implements InitializingBean {

	private static final String PAGE_SIZE_FILE = "pagesize.file";
	private static final String PAGE_SIZE_USERFRAGEMENT = "pagesize.userfragment";
	private static final String PAGE_SIZE_USERPAGE = "pagesize.userpage";
	private static final String PAGE_SIZE_ARICLE = "pagesize.article";
	private static final String PAGE_SIZE_TAG = "pagesize.tag";
	private static final String PAGE_SIZE_NEWS = "pagesize.news";
	private static final String PAGE_SIZE_ARTICLE_ARCHIVE = "pagesize.articleArchive";

	private final Properties config = new Properties();

	private static final Path RES_PATH = Constants.CONFIG_DIR.resolve("config.properties");
	static {
		FileUtils.createFile(RES_PATH);
	}

	private final Resource resource = new FileSystemResource(RES_PATH);

	/**
	 * 获取全局配置
	 * 
	 * @return
	 */
	@Cacheable(key = "'globalConfig'", value = "configCache")
	public GlobalConfig getGlobalConfig() {
		GlobalConfig config = new GlobalConfig();
		config.setArticlePageSize(getInt(PAGE_SIZE_ARICLE, 5));
		config.setFilePageSize(getInt(PAGE_SIZE_FILE, 5));
		config.setTagPageSize(getInt(PAGE_SIZE_TAG, 5));
		config.setPagePageSize(getInt(PAGE_SIZE_USERPAGE, 5));
		config.setFragmentPageSize(getInt(PAGE_SIZE_USERFRAGEMENT, 5));
		config.setNewsPageSize(getInt(PAGE_SIZE_NEWS, 5));
		config.setArticleArchivePageSize(getInt(PAGE_SIZE_ARTICLE_ARCHIVE, 5));
		return config;
	}

	/**
	 * 保存全局配置
	 * 
	 * @param globalConfig
	 * @return
	 */
	@CachePut(key = "'globalConfig'", value = "configCache")
	public GlobalConfig updateGlobalConfig(GlobalConfig globalConfig) {
		config.setProperty(PAGE_SIZE_FILE, Integer.toString(globalConfig.getFilePageSize()));
		config.setProperty(PAGE_SIZE_TAG, Integer.toString(globalConfig.getTagPageSize()));
		config.setProperty(PAGE_SIZE_ARICLE, Integer.toString(globalConfig.getArticlePageSize()));
		config.setProperty(PAGE_SIZE_USERFRAGEMENT, Integer.toString(globalConfig.getFragmentPageSize()));
		config.setProperty(PAGE_SIZE_USERPAGE, Integer.toString(globalConfig.getPagePageSize()));
		config.setProperty(PAGE_SIZE_NEWS, Integer.toString(globalConfig.getNewsPageSize()));
		store();
		return globalConfig;
	}

	private Integer getInt(String key, Integer defaultValue) {
		if (config.containsKey(key)) {
			return Integer.parseInt(config.getProperty(key));
		}
		return defaultValue;
	}

	private void store() {
		try (OutputStream os = new FileOutputStream(resource.getFile())) {
			config.store(os, "");
		} catch (Exception e) {
			throw new SystemException(e.getMessage(), e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Resources.readResource(resource, config::load);
	}

}
