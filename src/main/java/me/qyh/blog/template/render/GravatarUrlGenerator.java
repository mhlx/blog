package me.qyh.blog.template.render;

/**
 * <p>
 * 用于生成头像的md5地址
 * </p>
 * 
 * <pre>
 * usage:
 * <code>
 * ${gravatars.getUrl('emailMD5')}
 *</code>
 * </pre>
 * 
 * @since 5.9
 * 
 *
 */
public interface GravatarUrlGenerator {

	String getUrl(String md5);

}
