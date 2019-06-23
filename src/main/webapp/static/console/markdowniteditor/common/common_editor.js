
function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}
var maxLenth = editorConfig.maxLength | 2000;
var render = (function() {
	
	var plugins = ['footnote','katex','mermaid','anchor'];
	var md = createMarkdownParser({
		html : true,
		plugins:plugins,
		lineNumber:true
	});
	var t;
	var afterRender = function(v) {
		mermaid.init({},'#out .mermaid');
		$('#out').waitForImages(function() {
			sync.rebuild();
		});
	}
	
	var preParse = function(v){
		var doc = $.parseHTML2(v);
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
	var old;
	var update = function(){
		var doc = preParse(md.render(editor.getValue()));
		var innerHTML = doc.getElementsByTagName('body')[0].innerHTML;
		var div = document.createElement('div');
		div.id = "preview-block";
		div.innerHTML = innerHTML;
		var mermaids = doc.getElementsByClassName('mermaid');
		if($("#preview-block").length == 0){
			$("#out").html(div);
		} else {
			if(morphdom){
				morphdom($("#preview-block").get(0),div,{
					onBeforeElUpdated  : function(f,t){
						if ($(window).width() <= 768) {
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
			}
		}
		afterRender();
	}
	
	function cloneAttributes(element, sourceNode) {
	  let attr;
	  let attributes = Array.prototype.slice.call(sourceNode.attributes);
	  while(attr = attributes.pop()) {
		element.setAttribute(attr.nodeName, attr.nodeValue);
	  }
	}
	
	update();

	return {
		hasPlugin : function(name){
			for(var i=0;i<plugins.length;i++){
				if(plugins[i] === name){
					return true;
				}
			}
			return false;
		},
		md : function(ms) {
			var v = editor.getValue();
			if (t) {
				clearTimeout(t);
			}
			t = setTimeout(function(cb) {
				update();
				if ($(window).width() > 768) {
					sync.resetAndSync();
				}
				if(cb)
					cb();
			}, ms);
		}
	}
})();


var resize_t;
$(window).resize(function() {
	if (resize_t) {
		clearTimeout(resize_t);
	}
	resize_t = setTimeout(function() {
		sync.rebuild();
	}, 30)
})

var stat_timer;

var syncToLine = function(){
	if ($(window).width() > 768) {
		sync.doSyncAtLine(function() {
			var line = editor.getCursor().line;
			return line ;
		});
	}
}


editor.on('change', function(e) {
	if (config.autoRender) {
		render.md(300,function(){
			syncToLine();
		});
	}
	if ($(window).width() > 768) {
		var v = editor.getValue().length;
		$("#stat").text("当前字数：" + v + "/"+maxLenth).show();
		if (stat_timer) {
			clearTimeout(stat_timer);
		}
		stat_timer = setTimeout(function() {
			$("#stat").hide();
		}, 1000);
	}
});

editor.on('scroll', function() {
	if ($(window).width() > 768) {
		$('#out').off('scroll');
		editor.on('scroll', sync.doSync);
	}
});

function toggleToolbar(o) {
	config.toolbar = !config.toolbar;
	if (config.toolbar) {
		o.addClass("fa-check-square").removeClass("fa-square");
	} else {
		inner_bar.remove();
		o.addClass("fa-square").removeClass("fa-check-square");
	}
}