(function ($, undefined) {
	var dom_parser = false;

	// based on: https://developer.mozilla.org/en/DOMParser
	// does not work with IE < 9
	// Firefox/Opera/IE throw errors on unsupported types
	try {
		// WebKit returns null on unsupported types
		if ((new DOMParser()).parseFromString("", "text/html")) {
			// text/html parsing is natively supported
			dom_parser = true;
		}
	} catch (ex) {}

	if (dom_parser) {
		$.parseHTML2 = function (html) {
			return new DOMParser().parseFromString(html, "text/html");
		};
	}
	else if (document.implementation && document.implementation.createHTMLDocument) {
		$.parseHTML2 = function (html) {
			var doc = document.implementation.createHTMLDocument("");
			var doc_el = doc.documentElement;

			doc_el.innerHTML = html;

			var els = [], el = doc_el.firstChild;

			while (el) {
				if (el.nodeType === 1) els.push(el);
				el = el.nextSibling;
			}

  			// are we dealing with an entire document or a fragment?
			if (els.length === 1 && els[0].localName.toLowerCase() === "html") {
				doc.removeChild(doc_el);
				el = doc_el.firstChild;
				while (el) {
					var next = el.nextSibling;
					doc.appendChild(el);
					el = next;
				}
			}
			else {
				el = doc_el.firstChild;
				while (el) {
					var next = el.nextSibling;
					if (el.nodeType !== 1 && el.nodeType !== 3) doc.insertBefore(el,doc_el);
					el = next;
				}
			}

			return doc;
		};
	}
})(jQuery);
function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}
var base64Upload = function(f) {
	// show pick up
	if (f.size == 0) {
		bootbox.alert('不能上传空文件或者文件夹');
		return;
	}
	var reader = new FileReader();

	dirChooser
			.choose(function(dir, store) {
				var left = $(window).width() / 2 - 64;
				var top = $(window).height() / 2 - 64;
				$("#upload-loading").remove();
				$('body')
						.append(
								"<div id='upload-loading'><div class='modal-backdrop show' style='z-index: 1040;'></div><img src='"
										+ basePath
										+ "static/img/loading.gif' style='position:absolute;top:"
										+ top
										+ "px;left:"
										+ left
										+ "px' /></div>");
				reader.onload = (function(theFile) {
					return function(e) {
						var base64 = e.target.result;
						$.ajax({
							url : basePath + 'api/console/store/'+store+'/files?base64Upload',
							type : 'post',
							data:{
								parent : dir.id,
								name : f.name,
								base64 : base64
							},
							error : function(jqXHR){
								$("#upload-loading").remove();
								swal("上传失败",$.parseJSON(jqXHR.responseText).error,'error');
							},
							success:function(data){
								$("#upload-loading").remove();
								var result = data[0];
								if (result.error) {
									swal("上传失败",result.error,'error');
								} else {
									var name = result.name;
									var ext = name.split('.')
											.pop()
											.toLowerCase();
									if (ext == 'jpg'
											|| ext == 'jpeg'
											|| ext == 'png'
											|| ext == 'gif') {
										var middle = result.thumbnailUrl ? result.thumbnailUrl.middle
												: result.url;
										var large = result.thumbnailUrl ? result.thumbnailUrl.large
												: result.url;
										var md = '[!['
												+ result.name
												+ '](' + middle
												+ ' "'
												+ result.name
												+ '")]('
												+ large + ' "'
												+ result.name
												+ '")';
										editor
												.replaceSelection(md);
									} else {
										var md = '['
												+ result.name
												+ ']('
												+ result.url
												+ ')';
										editor
												.replaceSelection(md);
									}
								}
							}
						})
					};
				})(f);
				reader.readAsDataURL(f);
			});
}

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
		$("#stat").text("当前字数：" + v + "/200000").show();
		if (stat_timer) {
			clearTimeout(stat_timer);
		}
		stat_timer = setTimeout(function() {
			$("#stat").hide();
		}, 1000);
	}
});

editor.on('paste', function(editor, evt) {
	var clipboardData, pastedData;
	clipboardData = evt.clipboardData || window.clipboardData;
	var files = clipboardData.files;
	if (files.length > 0) {
		var f = files[0];// 上传第一张
		var type = f.type;
		if (type.indexOf('image/') == -1) {
			swal("只能上传图片文件");
			return;
		}

		base64Upload(f);
	}
});

editor.setOption('dropFileContentHandler', function(fileName, content) {
	var ext = fileName.split(".").pop().toLowerCase();
	if (ext == "md") {
		return content;
	} else if (ext == "html" || ext == 'htm') {
		return turndownService.turndown(content);
	}
	return "";
});

editor.setOption('dropFileHandler', function(file) {
	base64Upload(file);
});

var keymap = editor.getOption("extraKeys");
keymap["Ctrl-S"] = function(){
	showBase();
}
editor.setOption("extraKeys",keymap);

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