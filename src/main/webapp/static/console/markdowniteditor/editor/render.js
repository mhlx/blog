var render = (function() {
	'use strict';
	var plugins = ['footnote','mermaid','anchor','task-lists','sup','sub','abbr'];
	
	function MarkdownParser(plugins,html){
		this.plugins = plugins;
		this.md = createMarkdownParser({
			html : html,
			plugins:plugins,
			lineNumber:true
		});
	}
	
	var path ;
	try{
		path = basePath;
	}catch(e){
		path = '../../../';
	}
	
	var mermaidLoading = false;
	function loadMermaid(){
		if(mermaidLoading) return ;
		mermaidLoading = true;
		$('<script>').appendTo('body').attr({
			 src: path+'static/js/mermaid.min.js'
		});
		var t = setInterval(function(){
			try{	
				mermaid.initialize({theme:theme.mermaid.theme||'default'});
				mermaid.init({},'#out .mermaid');
				clearInterval(t);
			}catch(e){
				
			}
		},20)
	}
	
	MarkdownParser.prototype.parse = function(){
		return this.md.render(editor.getValue());
	}
	
	function MarkdownRender(parser){
		var t;
		this.parser = parser;
		this.handlers = [];
		this.renderElement = document.getElementById("out")
	}
	
	MarkdownRender.prototype.hasPlugin = function(name){
		for(var i=0;i<this.parser.plugins.length;i++){
			if(this.parser.plugins[i] === name){
				return true;
			}
		}
		return false;
	}
	
	MarkdownRender.prototype.preParse = function(){
		var doc = $.parseHTML2(this.parser.parse());
		var hasMermaid = doc.querySelector('.mermaid') != null;
		if(hasMermaid){
			loadMermaid();
		}
		var videos = doc.getElementsByTagName('video');
		for(var i=videos.length-1;i>=0;i--){
			var video = videos[i];
			var poster = video.getAttribute('poster');
			if(!poster){
				poster = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAABD4AAAJSCAYAAAArsQXOAAAPNElEQVR4nO3YwQ3AIBDAsNKlb3yYAiFF9gR5Z83M/gAAAACC/tcBAAAAALcYHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZBkfAAAAQJbxAQAAAGQZHwAAAECW8QEAAABkGR8AAABAlvEBAAAAZB3EsAcIUtpjKQAAAABJRU5ErkJggg==';
			}
			
			var html = '<div><a style="position: relative;display: inline-block;" ><i class="fas fa-video" style="color:blueviolet;z-index:1;    position: absolute;left: 50%;top: 50%; transform: translate(-50%, -50%);color: white;font-size: 20px !important;"></i><img class="img-fluid" src="'+poster+'"></a></div>';
			var doc2 = $.parseHTML2(html);
			insertAfter(doc2.getElementsByTagName('body')[0].getElementsByTagName('div')[0],video);
			video.parentNode.removeChild(video);
		}
		return doc;
	}
	
	MarkdownRender.prototype.onRendered = function(handler){
		this.handlers.push(handler);
	}
	
	MarkdownRender.prototype.offRendered = function(handler){
		for(var i=0;i<handlers.length;i++){
			if(handlers[i] === handler){
				handlers.splice(i,1);
				break;
			}
		}
	}
	
	MarkdownRender.prototype.setRenderElement = function(element){
		this.renderElement = element;
	}
	
	MarkdownRender.prototype.getRenderElement = function(element){
		return this.renderElement;
	}
	
	MarkdownRender.prototype.update = function(patch){
		var doc = this.preParse();
		var innerHTML = doc.body.innerHTML;
		if(patch){
			var div = document.createElement('div');
			cloneAttributes(div,this.renderElement)
			div.innerHTML = innerHTML;
			morphdom(this.renderElement,div,{
				onBeforeElUpdated  : function(f,t){
					if (CodeMirror.browser.mobile) {
						return true;
					}
					if (f.isEqualNode(t)) {
						return false;
					}
					if(f.classList.contains('mermaid-block')
						&& t.classList.contains('mermaid-block')){
							var old =  f.getElementsByClassName('mermaid-source')[0].textContent;
							var now =  t.getElementsByClassName('mermaid-source')[0].textContent;
							if(old == now){
								//更新属性
								cloneAttributes(f,t);
								return false;
							}
						}
					return true;
				}
			});
		} else {
			this.renderElement.innerHTML = innerHTML;
		}
		try{
			mermaid.initialize({theme:theme.mermaid.theme||'default'});
			mermaid.init({},'.mermaid');
		}catch(e){}
	}
	
	
	MarkdownRender.prototype.render = function(ms,cb){
		doRender(this,ms,true,cb);
	}
	
	MarkdownRender.prototype.renderAll = function(ms,cb){
		doRender(this,ms,false,cb);
	}
	
	function doRender(render,ms,patch,cb){

		var updateAndCallback = function(){
			render.update(patch);
			for(var i=0;i<render.handlers.length;i++){
				var handler = render.handlers[i];
				try{
					handler(patch);
				}catch(e){}
			}
			if(cb)
				cb();
		}
		
		if (render.t) {
			clearTimeout(render.t);
		}
		if(ms == 0){
			updateAndCallback();
			return ;
		}
		render.t = setTimeout(function(cb) {
			updateAndCallback();
		}, ms);
	}
	
	var parser = new MarkdownParser(plugins,true);
	var render = new MarkdownRender(parser);
	
	
	render.update(false);
	
	
	function cloneAttributes(element, sourceNode) {
		let attr;
		let attributes = Array.prototype.slice.call(sourceNode.attributes);
		while(attr = attributes.pop()) {
			element.setAttribute(attr.nodeName, attr.nodeValue);
		}
	}
	
	function insertAfter(newNode, referenceNode) {
		referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
	}
	
	return render;
})();