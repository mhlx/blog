CREATE TABLE IF NOT EXISTS `blog_article` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` mediumtext  NOT NULL,
  `pubDate` datetime DEFAULT NULL,
  `lastModifyDate` datetime DEFAULT NULL,
  `title` varchar(200)  NOT NULL,
  `isPrivate` tinyint(1) NOT NULL DEFAULT '0',
  `hits` int(11) NOT NULL DEFAULT '0',
  `summary` varchar(3000)  NOT NULL,
  `art_level` int(11) DEFAULT NULL,
  `art_status` int(11) NOT NULL DEFAULT '0',
  `art_from` int(11) NOT NULL DEFAULT '0',
  `editor` int(11) NOT NULL DEFAULT '0',
  `space_id` int(11) NOT NULL,
  `art_lock` varchar(40)  DEFAULT NULL,
  `art_alias` varchar(200)  DEFAULT NULL,
  `allowComment` tinyint(1) NOT NULL DEFAULT '1',
  `feature_image` varchar(500)  DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `blog_article_tag` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `article_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `blog_comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) DEFAULT NULL,
  `parent_path` varchar(255)  NOT NULL,
  `comment_nickname` varchar(20)  DEFAULT NULL,
  `comment_email` varchar(255)  DEFAULT NULL,
  `comment_ip` varchar(255)  NOT NULL,
   `comment_admin` tinyint(1)  NOT NULL DEFAULT '0',
  `content` varchar(2000)  NOT NULL,
  `comment_date` datetime NOT NULL,
  `comment_status` int(11) NOT NULL,
  comment_website varchar(255),
  `COMMENT_GRAVATAR` varchar(255),
  `module_id` int(11) NOT NULL,
  `module_type` varchar(50) NOT NULL,
  `comment_editor` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `blog_common_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_extension` varchar(500)  NOT NULL,
  `file_size` int(11) NOT NULL,
  `file_store` int(11) NOT NULL,
  `file_originalname` varchar(500)  NOT NULL,
  `file_width` int(11) DEFAULT NULL,
  `file_height` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `blog_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_parent` int(11) DEFAULT NULL,
  `file_type` int(11) NOT NULL DEFAULT '0',
  `file_createDate` datetime NOT NULL,
  `common_file` int(11) DEFAULT NULL,
  `file_lft` int(11) NOT NULL,
  `file_rgt` int(11) NOT NULL,
  `file_path` varchar(500)  NOT NULL,
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `blog_file_delete` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_store` int(11) DEFAULT NULL,
  `file_key` varchar(2000)  NOT NULL,
  `file_type` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `blog_fragment_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fragment_name` varchar(20)  NOT NULL,
  `fragment_description` varchar(500)  NOT NULL,
  `fragment_tpl` text  NOT NULL,
  `fragment_create_date` datetime NOT NULL,
  `space_id` int(11) DEFAULT NULL,
  `is_global` tinyint(1) NOT NULL DEFAULT '0',
  `is_callable` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `blog_lock` (
  `id` varchar(40)  NOT NULL,
  `lock_type` int(11) NOT NULL DEFAULT '0',
  `lock_password` varchar(100)  DEFAULT NULL,
  `lock_name` varchar(20)  NOT NULL,
  `createDate` datetime NOT NULL,
  `lock_question` text ,
  `lock_answers` varchar(500)  DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `blog_page_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `page_tpl` mediumtext  NOT NULL,
  `page_name` varchar(20)  NOT NULL,
  `page_description` varchar(500)  NOT NULL,
  `space_id` int(11) DEFAULT NULL,
  `page_create_date` datetime NOT NULL,
  `page_alias` varchar(255)  DEFAULT NULL,
  `page_allowComment` tinyint(1) NOT NULL DEFAULT '0',
  `page_spaceGlobal` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ;

CREATE TABLE IF NOT EXISTS `blog_space` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `space_alias` varchar(20)  NOT NULL,
  `createDate` datetime NOT NULL,
  `space_name` varchar(20)  NOT NULL,
  `space_lock` varchar(40)  DEFAULT NULL,
  `is_private` tinyint(1) NOT NULL DEFAULT '0',
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `space_alias` (`space_alias`),
  UNIQUE KEY `space_name` (`space_name`)
) ;

CREATE TABLE IF NOT EXISTS `blog_tag` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(20)  NOT NULL,
  `create_date` datetime NOT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE IF NOT EXISTS blog_history_template (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(255) NOT NULL,
  `template_tpl` mediumtext NOT NULL,
  `template_time` datetime NOT NULL,
  `remark` varchar(500) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `blog_news` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `news_content` varchar(4000) NOT NULL,
  `news_private` tinyint(1) NOT NULL DEFAULT '0',
  `news_allowComment` tinyint(1) NOT NULL DEFAULT '1',
  `news_write` datetime NOT NULL,
  `news_update` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)

