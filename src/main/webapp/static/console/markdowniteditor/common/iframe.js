var commonEditor = (function() {
	$('head').append('<style>.noscroll{overflow: hidden;}</style>');

	return {

		'bind' : function(selector, config,evt) {
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
				if(evt && evt.before){
					evt.before();
				}
				var stamp = $.now();
				var me = $(this);
				var value = '';
				if (config.getValueFun) {
					value = config.getValueFun(me);
				} else {
					value = me.val();
				}
				window.sessionStorage['commonEditor_'+stamp] = JSON.stringify(config);
				$("#commonEditor").remove();
				var html = '';
				html += '<div id="commonEditor"  style="position:fixed;top:0px;left:0px;width:100%;height:100%;z-index:99999">';
				var path = basePath;
				if(!path.endsWith('/')){
					path = path + '/';
				}
				html += '<iframe src="'+path+'static/console/markdowniteditor/common/iframe.html?stamp='+stamp+'" width="100%" height="100%" border="0" frameborder="0" ></iframe>';
				html += '</div>';
				var el = $(html);
				el.appendTo($('body'));
				var iframe = $("#commonEditor").find('iframe');
				iframe.on('load',function(){
					$('body').addClass('noscroll');
					iframe.contents().find('[data-close-iframe]').click(function(){
						var v = iframe[0].contentWindow.getEditorValue();
						if(config.setValueFun){
							config.setValueFun(me,v);
						} else {
							me.val(v);
						}
						if(evt && evt.close){
							evt.close(v);
						}
						$("#commonEditor").remove();
						$('body').removeClass('noscroll');
					});
					iframe[0].contentWindow.setEditorValue(value);
					
					if(evt && evt.afterLoad){
						evt.afterLoad();
					}
				});
				
			});
		}

	}

})();