var maxLenth = editorConfig.maxLength | 2000;
var render = (function() {
	var t;
	var md = window.md;
	var renderCodeBlock = function() {
		 $('#out pre code').each(function(i, block) {
		    hljs.highlightBlock(block);
		  });
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