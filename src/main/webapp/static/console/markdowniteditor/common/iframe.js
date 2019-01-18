var commonEditor = (function() {
	$('head').append('<style>.noscroll{overflow: hidden;}</style>');

	return {

		'bind' : function(selector, config) {
			var basePath = config.basePath;
			var isLogin = config.isLogin;
			var csrfToken = config.csrfToken;
			var csrfHeader = config.csrfHeader;
			if (typeof basePath === "undefined") {
				basePath = "";
			}
			if (typeof isLogin === "undefined") {
				isLogin = false;
			}
			if (typeof csrfToken === "undefined") {
				csrfToken = "";
			}
			if (typeof csrfHeader === "undefined") {
				csrfHeader = "";
			}
			$(document).on('focus', selector, function() {
				var me = $(this);
				var value = '';
				if (config.getValueFun) {
					value = config.getValueFun.call(me);
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
				html += '<iframe src="'+path+'static/console/markdowniteditor/common/iframe.html?basePath='+path+'&isLogin='+isLogin+'&csrfToken='+csrfToken+'&csrfHeader='+csrfHeader+'" width="100%" height="100%" border="0" frameborder="0"></iframe>';
				html += '</div>';
				var el = $(html);
				el.appendTo($('body'));
				var iframe = $("#commonEditor").find('iframe');
				iframe.on('load',function(){
					$('body').addClass('noscroll');
					iframe.contents().find('[data-close-iframe]').click(function(){
						var v = iframe[0].contentWindow.getEditorValue();
						if(config.setValueFun){
							config.setValueFun.call(me,v);
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

})();