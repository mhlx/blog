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
	var t;
	var md = window.md;
	var renderCodeBlock = function() {
		var p = false;
		$("#out pre").each(function() {
			var me = $(this);
			if (me.hasClass('prettyprint prettyprinted'))
				return true;
			if (me.find('code').length == 0)
				return true;
			else {
				p = true;
				me.addClass("prettyprint");
			}
		});
		if (p) {
			prettyPrint();
		}

	}
	
	var v = md.render(editor.getValue());
	$("#out").html(v);
    renderCodeBlock();

	return {
		md : function(ms) {
			var v = editor.getValue();
			if (t) {
				clearTimeout(t);
			}
			t = setTimeout(function() {
				v = md.render(v);
				$("#out").html(v);
                renderCodeBlock();
				sync.reset();
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
		sync.reset();
	}, 30)
})

var stat_timer;

editor.on('change', function(e) {
	if (config.autoRender) {
		render.md(300);
	}
	if ($(window).width() > 768) {
		sync.doSyncAtLine(function() {
			var line = editor.getCursor().line;
			return line > 1 ? line - 1 : line;
		});
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
	console.log(clipboardData);
	if (files.length > 0) {
		var f = files[0];// 上传第一张
		var type = f.type;
		if (type.indexOf('image/') == -1) {
			bootbox.alert("只能上传图片文件");
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