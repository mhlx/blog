(function(config, editor) {
	
	'use strict';
	
	var path ;
	try{
		path = basePath;
	}catch(e){
		path = '../../../../';
	}
	
	var o = $("#toggle-toolbar");
	if(CodeMirror.browser.mobile){
		$("#toc").touchwipe({
			wipeLeft: function() {toEditor() },
			min_move_x: 50,
			max_move_y:5
		});
		
		$("#wrapper").animate({scrollLeft: $("#toc").outerWidth()},0);
		
		
		var toEditor = function() {
			$("#wrapper").animate({scrollLeft: $("#in").width()}, 800);
		}
		
		function renderToc(){
			render.render(0);
			var headings = render.getRenderElement().querySelectorAll("h1, h2, h3, h4, h5, h6");
			var toc = [];
			for(var i=0;i<headings.length;i++){
				var head = headings[i];
				var index = head.tagName.substring(1);
				var line = head.getAttribute('data-line');
				if(toc.length == 0){
					toc.push([{indent:index,name:head.textContent,line:line}]);
				} else {
					var last = toc[toc.length-1];
					var first = last[0];
					if(index > first.indent){
						last.push({indent:index,name:head.textContent,line:line});
					} else {
						toc.push([{indent:index,name:head.textContent,line:line}]);
					}
				}
			}
			
			
  
			var html = '<h1>TOC</h1><hr>';
			if(toc.length > 0){
				for(var i=0;i<toc.length;i++){
					var block = toc[i];
					for(var j=0;j<block.length;j++){
						var item = block[j];
						var indent = item.indent;
						html += '<h'+indent+' data-line="'+item.line+'">'+item.name+'</h'+indent+'>';
					}
				}
			} 
			try{
				var div = document.createElement("div");
				div.setAttribute('id',"toc");
				div.innerHTML = html;
				morphdom($("#toc")[0],div);
			}catch(e){$("#toc").html(html)};
			
		}
		
		$("#toc").on('click','[data-line]',function(){
			var line = parseInt($(this).data('line'));
			editor.scrollIntoView({line:line});
			setTimeout(function(){
				var top = editor.charCoords({line: line, ch: 0}, "local").top; 
				editor.scrollTo(null,top);
				toEditor();
			},500)
			
		})
		
		var toDoc = function() {
			editor.getInputField().blur();
			$("#wrapper").animate({scrollLeft: 0}, 800,function(){
				renderToc();
			});
		}
		var toPreview = function() {
			editor.getInputField().blur();
			render.render(0);
			sync.doSync(0);
			$("#wrapper").animate({scrollLeft: $("#out")[0].offsetLeft}, 800);
		}
		
		config.toolbar = false;
		config.autoRender = false;
		bar.hide();
		o.addClass("fa-square").removeClass("fa-check-square");
		
		$(editor.getScrollerElement()).touchwipe({
			wipeLeft: function() { toPreview() },
			wipeRight: function() { toDoc() },
			min_move_x: 50,
			max_move_y:5
		});
		$("#out").touchwipe({
			wipeRight: function() { toEditor() },
			min_move_x: 50,
			max_move_y:5
		});
		
	} else {
		$("#wrapper").animate({scrollLeft: $("#toc").outerWidth()},0);
		
		function renderToc(){
			var headings = render.getRenderElement().querySelectorAll("h1, h2, h3, h4, h5, h6");
			var toc = [];
			for(var i=0;i<headings.length;i++){
				var head = headings[i];
				var index = head.tagName.substring(1);
				var line = head.getAttribute('data-line');
				if(toc.length == 0){
					toc.push([{indent:index,name:head.textContent,line:line}]);
				} else {
					var last = toc[toc.length-1];
					var first = last[0];
					if(index > first.indent){
						last.push({indent:index,name:head.textContent,line:line});
					} else {
						toc.push([{indent:index,name:head.textContent,line:line}]);
					}
				}
			}
			
			
  
			var html = '<h1>TOC</h1><hr>';
			if(toc.length > 0){
				for(var i=0;i<toc.length;i++){
					var block = toc[i];
					for(var j=0;j<block.length;j++){
						var item = block[j];
						var indent = item.indent;
						html += '<h'+indent+' data-line="'+item.line+'">'+item.name+'</h'+indent+'>';
					}
				}
			} 
			try{
				var div = document.createElement("div");
				div.setAttribute('id',"toc");
				div.innerHTML = html;
				morphdom($("#toc")[0],div);
			}catch(e){$("#toc").html(html)};
			
		}
		
		render.onRendered(function(){
			renderToc();
		})
		
		$("#toc").on('click','[data-line]',function(){
			var line = parseInt($(this).data('line'));
			
			editor.scrollIntoView({line:line});
			setTimeout(function(){
				var top = editor.charCoords({line: line, ch: 0}, "local").top; 
				editor.scrollTo(null,top);
			},500)
			
		})
		renderToc();
		var sideBar = false;
		$("#toc-icon").click(function(){
			if(sideBar){
				sideBar = false;
				$("#wrapper").animate({scrollLeft: $("#toc").outerWidth()},800);
			} else {
				sideBar = true;
				$("#wrapper").animate({scrollLeft: 0},800);
			}
			
		});
		o.addClass("fa-square").removeClass("fa-check-square");
		config.toolbar = false;
		config.autoRender = true;
		$("#in").show();
		$("#out").css({'visibility':'visible'});
		$("#fullscreen-icon").click(function(){
			var o = $(this);
			if (o.hasClass("fa-window-minimize")) {
				$("#out").show();
				$("#in").css({
					width : '50%'
				}).show();
				$(".CodeMirror").css({margin:0})
				$("#fullscreen-style").remove();
				o.removeClass("fa-window-minimize").addClass("fa-window-maximize");
				sync.doSync();
			} else {
				$("#out").hide();
				$("#toolbar").css({
					width : '100%'
				}).show();
				$("#in").css({
					width : '100%'
				}).show();
				$(".CodeMirror").css({margin:'30px 80px 30px 80px'})
				$("head").append("<style type='text/css' id='fullscreen-style'> .inner-toolbar{padding-left:90px !important;width:100% !important} #searchHelper{width:80% !important;left:10% !important;} #stat{width:80% !important;left:10% !important;}</style>");
				o.removeClass("fa-window-maximize").addClass("fa-window-minimize");
			}
		})
	}
	
	
	editor.setOption('dropContentHandler', function(fileName, content) {
		var ext = fileName.split(".").pop().toLowerCase();
		if (ext == "html" || ext == 'htm') {
			return turndownService.turndown(content);
		}
		if(ext == 'md')
			return content;
		swal('只能将md|html文件拖曳到编辑框');
	});

	editor.setOption('dropFileHandler', function(file) {
		var ext = file.name.split(".").pop().toLowerCase();
		if (ext == "html" || ext == 'htm' || ext == 'md') {
			return false;
		}
		base64Upload(file);
		return true;
	});
	
	editor.focus();
	editor.setCursor({
		line : 0
	});
	

	
	$("#cog-icon").click(function(){
		swal({
			html : '<input type="checkbox"  id="theme-mode-toggle"/>主题编辑模式'
		});
		$("#theme-mode-toggle").prop('checked',themeMode.isThemeMode);
	})
	
	$("#eye-icon").click(function(){
		window.location.href = 'view.html'
	});
	
	
	var themeMode = (function(){
		var toolbarHandler = function(e){
			if($(e.target).attr('id') == 'cog-icon'){
				return ;
			}
			colorPicker(theme.toolbar.color,function(color){
				theme.toolbar.color = color;
				theme.render();theme.store();
			});
		}
		var statHandler = function(){
			colorPicker(theme.stat.color,function(color){
				theme.stat.color = color;
				theme.render();theme.store();
			});
		}
		var searchHelprHandler = function(){
			colorPicker(theme.searchHelper.color,function(color){
				theme.searchHelper.color = color;
				theme.render();theme.store();
			});
		}
		var cursorHelprHandler = function(){
			colorPicker(theme.cursorHelper.color,function(color){
				theme.cursorHelper.color = color;
				theme.render();theme.store();
			});
		}
		
		var barHandler = function(){
			colorPicker(theme.bar.color,function(color){
				theme.bar.color = color;
				theme.render();theme.store();
			});
		}
		var cloneBar;
		var setTheme = false;
		var changeThemeHandler = function(){
			async function getTheme() {
				setTheme = true;
				const {value: _theme} = await Swal.fire({
					input: 'select',
					inputValue: theme.editor.theme || '',
					inputOptions: {
						'3024-day': '3024-day',
						'3024-night': '3024-night',
						'abcdef': 'abcdef',
						'ambiance-mobile': 'ambiance-mobile',
						'ambiance': 'ambiance',
						'base16-dark': 'base16-dark',
						'base16-light': 'base16-light',
						'bespin': 'bespin',
						'blackboard': 'blackboard',
						'cobalt': 'cobalt',
						'colorforth': 'colorforth',
						'darcula': 'darcula',
						'dracula': 'dracula',
						'duotone-dark': 'duotone-dark',
						'duotone-light': 'duotone-light',
						'eclipse': 'eclipse',
						'elegant': 'elegant',
						'erlang-dark': 'erlang-dark',
						'gruvbox-dark': 'gruvbox-dark',
						'hopscotch': 'hopscotch',
						'icecoder': 'icecoder',
						'idea': 'idea',
						'isotope': 'isotope',
						'lesser-dark': 'lesser-dark',
						'liquibyte': 'liquibyte',
						'lucario': 'lucario',
						'material': 'material',
						'mbo': 'mbo',
						'mdn-like': 'mdn-like',
						'midnight': 'midnight',
						'monokai': 'monokai',
						'neat': 'neat',
						'neo': 'neo',
						'night': 'night',
						'oceanic-next': 'oceanic-next',
						'panda-syntax': 'panda-syntax',
						'paraiso-dark': 'paraiso-dark',
						'paraiso-light': 'paraiso-light',
						'pastel-on-dark': 'pastel-on-dark',
						'railscasts': 'railscasts',
						'rubyblue': 'rubyblue',
						'seti': 'seti',
						'shadowfox': 'shadowfox',
						'solarized': 'solarized',
						'ssms': 'ssms',
						'the-matrix': 'the-matrix',
						'tomorrow-night-bright': 'tomorrow-night-bright',
						'tomorrow-night-eighties': 'tomorrow-night-eighties',
						'ttcn': 'ttcn',
						'twilight': 'twilight',
						'vibrant-ink': 'vibrant-ink',
						'xq-dark': 'xq-dark',
						'xq-light': 'xq-light',
						'yeti': 'yeti',
						'zenburn': 'zenburn'
					},
					inputPlaceholder: '选择主题',
					showCancelButton: true
				});
				if (_theme) {
					theme.editor.theme = _theme;
					theme.loadEditorTheme(function(_theme){
						setTimeout(function(){
							editor.setOption("theme", _theme);
							var bgColor = window.getComputedStyle(document.body.querySelector('.CodeMirror'),null).getPropertyValue('background-color');
							theme.inCss.background = bgColor;
							theme.render();theme.store();
						},100)
					})
				}
				setTimeout(function(){
					setTheme = false;
				},1000)
			}
			if(!setTheme){
				getTheme();
			}
		}
		var changeMemaidThemeHandler = function(){
			async function getTheme() {
				const {value: _theme} = await Swal.fire({
					input: 'select',
					inputValue: theme.mermaid.theme || '',
					inputOptions: {
						'default': 'default',
						'forest': 'forest',
						'dark': 'dark',
						'neutral': 'neutral'
					},
					inputPlaceholder: '选择主题',
					showCancelButton: true
				});
				if (_theme) {
					theme.mermaid.theme = _theme;
					theme.store();
					render.renderAll(0,function(){
						sync.doSync();
					});
				}
			}
			getTheme();
		}
		
	
		var clonedTheme;
		var isThemeMode = false;
		function inThemeMode(){
			isThemeMode = true;
			clonedTheme  = theme.clone();
			$('<link>').appendTo('head').attr({
				  id:'colorpicker-css',
				  type: 'text/css', 
				  rel: 'stylesheet',
				  href: path+'static/colorpicker/dist/css/bootstrap-colorpicker.min.css'
			  });
			$('<script>').appendTo('body').attr({id:'colorpicker-js',src:path+'static/colorpicker/dist/js/bootstrap-colorpicker.min.js'});	  
			editor.setOption('readOnly',true);
			$("#searchHelper input").attr('value','点击设置字体颜色');
			$("#searchHelper").children().addClass('noclick');
			searchHelper.open();
			$("#searchHelper").on('click',searchHelprHandler);
			$("#cursorHelper").children().addClass('noclick');
			cursorHelper.open();
			$("#cursorHelper").on('click',cursorHelprHandler);
			$("#toolbar").children().addClass('noclick');
			$("#cog-icon").removeClass('noclick');
			$("#toolbar").on('click',toolbarHandler);
			$("#stat").text("点击设置字体颜色").show();
			$("#stat").on('click',statHandler);
			editor.on('cursorActivity',changeThemeHandler);
			$("#out").on('click','.mermaid',changeMemaidThemeHandler);
			cloneBar = $(".inner-toolbar").clone();
			
			cloneBar.css({'visibility':'visible','top':'100px'});
			cloneBar.children().addClass('noclick');
			$("#in").append(cloneBar);
			cloneBar.on('click',barHandler);
		}
		
		function outThemeMode(){
			isThemeMode = false;
			cloneBar.off('click',barHandler);
			cloneBar.remove();
			editor.off('cursorActivity',changeThemeHandler);
			$("#searchHelper").off('click',searchHelprHandler);
			$("#searchHelper input").removeAttr('value');
			searchHelper.close();
			$("#cursorHelper").off('click',cursorHelprHandler);
			cursorHelper.close();
			$("#toolbar").off('click',toolbarHandler);
			$("#stat").off('click',statHandler);
			$("#out").off('click','.mermaid',changeMemaidThemeHandler);
			$("#stat").text("").hide();
			$('.noclick').removeClass('noclick');
			editor.setOption('readOnly',false);
		}
		
		
		var colorPicker = function (currentColor,callback){
			async function getColor() {
				const {value: color} = await Swal.fire({
					html : '<div id="colorpicker"></div>',
					showCancelButton: true
				});
			}
			getColor();
			$('#colorpicker').colorpicker({
				  inline: true,
				  container: true,
				  template: '<div class="colorpicker">' +
				  '<div class="colorpicker-saturation"><i class="colorpicker-guide"></i></div>' +
				  '<div class="colorpicker-hue"><i class="colorpicker-guide"></i></div>' +
				  '<div class="colorpicker-alpha">' +
				  '   <div class="colorpicker-alpha-color"></div>' +
				  '   <i class="colorpicker-guide"></i>' +
				  '</div>' +
				  '</div>'
			});
			if(currentColor){
				$('#colorpicker').colorpicker('setValue',currentColor);
			}
			$('#colorpicker').on('colorpickerChange', function(event) {
				if(event.color && callback){
					callback(event.color.toString());
				}
		  });
		}
		
		return {
			toggle : function(){
				if(isThemeMode){
					outThemeMode();
				} else {
					inThemeMode();
				}
				return isThemeMode;
			},
			isThemeMode : function(){
				return isThemeMode;
			}
		}
	})();
	
	$(document).on('click',"#theme-mode-toggle",function(){
		var isThemeMode = themeMode.toggle();
		$(this).prop('checked',themeMode.isThemeMode);
	});

})(config, editor);