var querier = (function($) {
	var names = [ 'data_archives.md', 'data_articleNav.md',
			'data_article.md', 'data_articlePage.md',
			'data_articleStatistics.md', 'data_articleTag.md',
			'data_commentCount.md', 'data_commentStatistics.md',
			'data_filePage.md', 'data_lastNews.md', 'data_news.md',
			'data_newsNav.md', 'data_newsPage.md', 'data_newsStatistics.md',
			'data_spaces.md', 'data_tagStatistics.md', 'data_user.md',
			'formats.md', 'fragment.md', 'fragments.md', 'gravatars.md',
			'handler.md', 'jsoups.md', 'lock.md', 'locked.md', 'markdown.md',
			'period.md', 'private.md', 'redirect.md', 'times.md',
			'transaction.md', 'unlocked.md', 'validators.md' ];
	
	var cache = [];
	var status = 'unloaded';
	
	var read = function(name){
		$.ajax({
            url : root+'doc/'+name,
            dataType: "text",
            success : function (data) {
            	var md = window.markdownit();
            	var result = md.render(data);
            	cache.push({'name':name,'content':result});
            	count ++ ;
            }
        });
	}
	
	if(status == 'unloaded'){
		status = 'loading';
		
		var count = 0;
		
		for(var i=0;i<names.length;i++){
			var name = names[i];
			read(name);
		}
		
		
		var t = setInterval(function(){
			if(count == names.length){
				status = 'loaded';
				clearInterval(t);
			}
		},10);
		
		return {
			search : function(text,cb){
				var result = [];
				 $.each(cache,function(i,d){
					 var content = d.content;
					 if(content.indexOf(text) != -1){
						 result.push(d.name);
					 }
				 });
				 cb(result);
			},
			getFile : function(name,cb){
				$.ajax({
		            url : root+'doc/'+name,
		            dataType: "text",
		            success : function (data) {
		            	var md = window.markdownit({
		            		html:         true
		            	});
		            	var result = md.render(data);
		            	cb(result);
		            }
		        });
			}
		}
		
	}
	
})(jQuery);