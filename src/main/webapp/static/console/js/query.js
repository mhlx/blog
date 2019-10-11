var querier = (function($) {
	var names = [ 'data_archives.md', 'data_articeNav.md', 'data_article.md',
			'data_articlePage.md', 'data_articleStatistics.md',
			'data_articleTag.md', 'data_commentCount.md',
			'data_commentStatistics.md', 'data_filePage.md',
			'data_lastNews.md', 'data_news.md', 'data_newsNav.md',
			'data_newsPage.md', 'data_newsStatistics.md', 'data_spaces.md',
			'data_tagStatistics.md', 'data_user.md', 'formats.md',
			'fragment.md', 'fragments.md', 'gravatars.md', 'hander.md',
			'ip.md', 'jsons.md', 'jsoups.md', 'lock.md', 'markdown.md',
			'messages.md', 'period.md', 'plugin_data_commentPage.md',
			'plugin_data_lastComments.md',
			'plugin_data_recentlyViewdArticles.md', 'private.md',
			'redirect.md', 'space.md', 'times.md', 'transaction.md',
			'unlocked.md', 'urls.md', 'user.md', 'validators.md', 'csrf.md',
			'data_articles.md' ];

	var templates = [ 'page_article_detail.html', 'page_index.html',
			'page_error.html', 'page_news_detail.html', 'login.html',
			'page_news.html', 'fragment_articles.html', 'fragment_foot.html',
			'fragment_top.html', 'guestbook.html', 'markdown.html',
			'simple.html' ];

	var cache = [];
	var status = 'unloaded';

	var read = function(name) {
		$.ajax({
			url : root + 'doc/' + name,
			dataType : "text",
			success : function(data) {
				var result = window.markdownit().render(data);
				cache.push({
					'name' : name,
					'content' : result
				});
				count++;
			}
		});
	}

	if (status == 'unloaded') {
		status = 'loading';

		var count = 0;

		for (var i = 0; i < names.length; i++) {
			var name = names[i];
			read(name);
		}

		for (var i = 0; i < templates.length; i++) {
			var template = templates[i];
			cache.push({
				'name' : template,
				'content' : template
			});
		}

		var t = setInterval(function() {
			if (count == names.length) {
				status = 'loaded';
				clearInterval(t);
			}
		}, 10);

		return {
			search : function(text, cb) {
				var result = [];
				$.each(cache, function(i, d) {
					var name = d.name;
					if (name.indexOf(text) != -1) {
						result.push(d.name);
					} else {
						var content = d.content;
						if (content.indexOf(text) != -1) {
							result.push(d.name);
						}
					}
				});
				cb(result);
			},
			getFile : function(name, cb) {
				$.ajax({
					url : root + 'doc/' + name,
					dataType : "text",
					success : function(data) {
						if ($.inArray(name, templates) != -1) {
							var result = '<pre class="pre-scrollable">';
							result += data.replace(/&/g, '&amp;').replace(/</g,
									'&lt;').replace(/>/g, '&gt;');
							result += '</pre>';
							cb(result);
						} else {
							var result = window.markdownit().render(data);
							cb(result);
						}
					}
				});
			}
		}

	}

})(jQuery);