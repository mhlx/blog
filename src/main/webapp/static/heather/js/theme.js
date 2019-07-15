var theme = (function(){
	function Theme(){
		this.toolbar = {};
		this.bar = {};
		this.stat = {};
		this.editor = {};
		this.inCss = {};
		this.cursorHelper = {};
		this.searchHelper = {};
		this.mermaid = {};
		this.hljs = {theme:'github'};
		this.customCss = undefined;
		this.timer = undefined;
	}
	
	Theme.prototype.store = function(callback){
		if(this.timer){
			clearTimeout(this.timer);
		}
		var theme = this;
		this.timer = setTimeout(function(){	
			storage.saveTheme(theme,function(cb){
				if(callback) callback(cb == "success" ? true : false);
			});
		},500)
	}
	
	Theme.prototype.reset = function(){
		var theme = new Theme();
		storage.saveTheme(theme);
		theme.render();
		return theme;
	}
	
	Theme.prototype.clone = function(){
		var copy = JSON.parse(JSON.stringify(this));
		var theme = new Theme();
		theme.toolbar = copy.toolbar;
		theme.bar = copy.bar;
		theme.stat = copy.stat;
		theme.editor = copy.editor;
		theme.inCss = copy.inCss;
		theme.cursorHelper = copy.cursorHelper;
		theme.searchHelper = copy.searchHelper;
		theme.mermaid = copy.mermaid;
		theme.customCss = copy.customCss;
		return theme;
	}
	
	var getCurrentTheme = function(){
		var current;
		storage.getTheme(function(cb){
			current = cb
		});
		if(!current || current == null){
			return new Theme();
		}
		var theme = new Theme();
		theme.toolbar = current.toolbar;
		theme.bar = current.bar;
		theme.stat = current.stat;
		theme.editor = current.editor;
		theme.inCss = current.inCss;
		theme.cursorHelper = current.cursorHelper;
		theme.searchHelper = current.searchHelper;
		theme.mermaid = current.mermaid;
		theme.customCss = current.customCss;
		return theme;
	}
	
	Theme.prototype.loadEditorTheme = function(onLoad){
		if(this.editor.theme){
			var editorTheme = this.editor.theme;
			var loadHandler = function(theme){
				onLoad(theme);
			}
			if($('head link[href="'+rootPath+'static/codemirror/theme/'+editorTheme+'.css"]').length == 0){
				$('<link id="codemirror-theme-'+editorTheme+'" >').appendTo('head').attr({
				  type: 'text/css', 
				  rel: 'stylesheet',
				  onload:function(){
					  loadHandler(editorTheme);
				  },
				  href: ''+rootPath+'static/codemirror/theme/'+editorTheme+'.css'
			   })
			}else{
				loadHandler(editorTheme);
			}
		}
	}
	
	Theme.prototype.loadHljsTheme = function(onLoad){
		if(this.hljs.theme){
			var hljsTheme = this.hljs.theme;
			var loadHandler = function(theme){
				onLoad(theme);
			}
			if($('head link[href="'+rootPath+'static/heather/highlight/styles/'+hljsTheme+'.css"]').length == 0){
				$('<link id="hljs-theme-'+hljsTheme+'" >').appendTo('head').attr({
				  type: 'text/css', 
				  rel: 'stylesheet',
				  onload:function(){
					  loadHandler(hljsTheme);
				  },
				  href: ''+rootPath+'static/heather/highlight/styles/'+hljsTheme+'.css'
			   })
			}else{
				loadHandler(hljsTheme);
			}
		}
	}
	
	
	Theme.prototype.render = function(pro){
		var css = "";
		css += "#toolbar{color:"+(this.toolbar.color || 'inherit')+"}\n";
		css += ".inner-toolbar{color:"+(this.bar.color || 'inherit')+"}\n"
		css += "#stat{color:"+(this.stat.color || 'inherit')+"}\n";
		css += "#in{background:"+(this.inCss.background || 'inherit')+"}\n";
		css += "#cursorHelper{color:"+(this.cursorHelper.color || 'inherit')+"}\n";
		css += "#searchHelper{color:"+(this.searchHelper.color || 'inherit')+"}\n#searchHelper input{color:"+(this.searchHelper.color || 'inherit')+"}\n";
		if(this.customCss){
			css += customCss;
		}
		var theme = this;
		this.loadEditorTheme(function(theme){
			if(pro && pro.editorThemeLoad){pro.editorThemeLoad(theme)}
		})
		this.loadHljsTheme(function(theme){
			if(pro && pro.hljsThemeLoad){pro.hljsThemeLoad(theme)}
		})
		$("#custom_theme").remove();
		if($.trim(css) != ''){
			$("head").append("<style type='text/css' id='custom_theme'>"+css+"</style>");
		}
	}
	
	return getCurrentTheme();
})();