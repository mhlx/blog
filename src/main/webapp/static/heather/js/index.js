$(function(){
	
	var docName;
	$("#toggle-toolbar").click(function(){
		var o = $(this);
		config.toolbar = !config.toolbar;
		if (config.toolbar) {
			o.addClass("fa-check-square").removeClass("fa-square");
		} else {
			bar.hide();
			o.addClass("fa-square").removeClass("fa-check-square");
		}
	})
	$("#cog-icon").click(function(){
		swal({
			html : '<input type="checkbox"  id="theme-mode-toggle"/>主题编辑模式'
		});
		$("#theme-mode-toggle").prop('checked',layout.isThemeMode);
	});
	var inToc = false;
	$("#toc-icon").click(function(){
		if(inToc){
			layout.toEditor(function(){inToc = false});
		} else {
			layout.toToc(function(){inToc = true});
		}
	})
	
	$(document).on('click','#theme-mode-toggle',function(){
		layout.toggleThemeMode();
	});
	
	$("#toc-body").on('click','[data-line]',function(){
		var line = parseInt($(this).data('line'));
		editor.scrollIntoView({line:line});
		setTimeout(function(){
			var top = editor.charCoords({line: line, ch: 0}, "local").top; 
			editor.scrollTo(null,top);
			if(CodeMirror.browser.mobile){
				layout.toEditor();
			}
		},500)
	});
	if(CodeMirror.browser.mobile){
		$("#toc").touchwipe({
			wipeLeft: function() {layout.toEditor() },
			min_move_x: 10,
			max_move_y:5
		});
		$(editor.getScrollerElement()).touchwipe({
			wipeLeft: function() { layout.toPreview() },
			wipeRight: function() { layout.toToc() },
			min_move_x: 10,
			max_move_y:5
		});
		$("#out").touchwipe({
			wipeRight: function() { layout.toEditor() },
			min_move_x: 10,
			max_move_y:5
		});
	}
	
	
	$("#fullscreen-icon").click(function(){
		var o = $(this);
		if(layout.toggleFullScreenMode()){
			o.removeClass("fa-window-minimize").addClass("fa-window-maximize");
		}else{
			o.removeClass("fa-window-maximize").addClass("fa-window-minimize");
		}
	})
	
	var defaultDoc = storage.get("undefined", function(data) {
		if (data != null) {
			editor.setValue(data.content);
		}
	});

	$("#upload-icon").click(function() {
		if (!docName) {
			async function setDocName() {
				const {
					value: name
				} = await Swal.fire({
					title: '请输入文档名称',
					input: 'text',
					cancelButtonText: '取消',
					confirmButtonText: '确认',
					showCancelButton: true
				})
				if (name && name != 'undefined') {
					docName = name;
					save(name);
				} else {
					if (name == "undefined") {
						swal("文档名称不能为undefined");
					} else {
						swal("需要文档名称才能储存");
					}
				}
			}
			setDocName();
		} else {
			save(docName);
		}

	});

	var save = function(name) {
		storage.del('undefined', function(data) {
			storage.add(name, editor.getValue(), function(data) {
				if (data == 'success') {
					swal("保存成功");
				} else {
					swal("保存失败");
				}
			})
		})
	}

	var cache;

	function renderStorageList() {
		var body = $("#storage-modal .modal-body");
		storage.getList(function(list) {
			for (var i = 0; i < list.length; i++) {
				var doc = list[i];
				if (doc.title == 'undefined') {
					list.splice(i, 1);
					break;
				}
			}
			if (list.length == 0) {
				body.html('<div class="alert alert-warning">没有任何本地文档</div>');
			} else {
				cache = list;
				var html = '<div class="table-responsive"><table class="table">';
				html += '<tr><th>名称</th><th></th></tr>';
				for (var i = 0; i < list.length; i++) {
					var doc = list[i];
					html += '<tr><td>' + doc.title + '</td><td><a href="javascript:void(0)" data-del="' + i + '">删除</a>&nbsp;&nbsp;&nbsp;<a href="javascript:void(0)" data-load="' + i + '">加载</a></td></tr>';
				}
				html += '</table></div>';
				body.html(html);
			}
		});
	}

	$("#storage-modal").on('show.bs.modal', function() {
		renderStorageList();
	});
	$("#storage-modal").on('hidden.bs.modal', function() {
		var body = $("#storage-modal .modal-body");
		body.html('');
	});
	$("#storage-modal").on("click", "[data-load]", function() {
		var index = $(this).data('load');
		var doc = cache[parseInt(index)];
		if (doc) {
			Swal.fire({
				title: '确定加载吗?',
				type: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#3085d6',
				cancelButtonColor: '#d33',
				confirmButtonText: '是的',
				cancelButtonText: '取消'
			}).then((result) => {
				if (result.value) {
					docName = doc.title;
					editor.setValue(doc.content);
					$("#storage-modal").modal('hide');
				}
			})
		}
	});
	$("#storage-modal").on("click", "[data-del]", function() {
		var index = $(this).data('del');
		var doc = cache[parseInt(index)];
		if (doc) {
			Swal.fire({
				title: '确定删除吗?',
				type: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#3085d6',
				cancelButtonColor: '#d33',
				confirmButtonText: '是的',
				cancelButtonText: '取消'
			}).then((result) => {
				if (result.value) {
					storage.del(doc.title, function(data) {
						renderStorageList();
					});
				}
			})
		}
	});
	
	$("#download-icon").click(function(){
		$("#storage-modal").modal('show');
	});
	
	render.render(0)
	var stat_timer;
	var auto_save_timer;
	editor.on('change', function(e) {
		if (config.autoRender) {
			render.render(300)
		}
		if (!CodeMirror.browser.mobile) {
			var v = editor.getValue().length;
			$("#stat").text("当前字数：" + v).show();
			if (stat_timer) {
				clearTimeout(stat_timer);
			}
			stat_timer = setTimeout(function() {
				$("#stat").hide();
			}, 1000);
		}
		if (auto_save_timer) {
			clearTimeout(auto_save_timer);
		}
		auto_save_timer = setTimeout(function() {
			var flag = false;
			var name = docName ? docName : 'undefined';
			storage.add(name, editor.getValue(), function(data) {
				if (!CodeMirror.browser.mobile) {
					$("#stat").text((data == 'success') ? "自动保存成功" : "自动保存失败").show();
					if (stat_timer) {
						clearTimeout(stat_timer);
					}
					stat_timer = setTimeout(function() {
						$("#stat").hide();
					}, 1000);
				}
			});
		}, 500);
	});
})
