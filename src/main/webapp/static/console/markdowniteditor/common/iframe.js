var commonEditor = (function(basePath,isLogin) {
	
	$('head').append('<style>.noscroll{overflow: hidden;}</style>');

	return {

		'bind' : function(selector, getValueFun, setValueFun) {
			$(document).on('focus', selector, function() {
				var me = $(this);
				var value = '';
				if (getValueFun) {
					value = getValueFun.call(me);
				} else {
					value = me.val();
				}
				$("#commonEditor").remove();
				var html = '';
				html += '<div id="commonEditor"  style="position:fixed;top:0px;left:0px;width:100%;height:100%;">';
				var path = basePath;
				if(!path.endsWith('/')){
					path = path + '/';
				}
				html += '<iframe src="'+path+'static/console/markdowniteditor/common/iframe.html?basePath='+basePath+'&isLogin='+isLogin+'" width="100%" height="100%" border="0" frameborder="0"></iframe>';
				html += '</div>';
				var el = $(html);
				el.appendTo($('body'));
				var iframe = $("#commonEditor").find('iframe');
				iframe.on('load',function(){
					$('body').addClass('noscroll');
					iframe.contents().find('[data-close]').click(function(){
						var v = iframe[0].contentWindow.getEditorValue();
						if(setValueFun){
							setValueFun.call(me,v);
						} else {
							me.val(v);
						}
						$("#commonEditor").remove();
						$('body').removeClass('noscroll');
					});
					iframe[0].contentWindow.setEditorValue(value);
				});
				
			});
		}

	}

})(basePath,isLogin);