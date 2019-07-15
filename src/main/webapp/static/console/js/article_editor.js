var editor ;
(function(){
	editor = Editor.create(document.getElementById("editor"),function(editor){
		editor.toolbarHelper.removeIcon(function(icon){
			return icon.classList.contains('fa-upload') || icon.classList.contains('fa-download');
		})
		editor.toolbarHelper.insertIcon(['fas','fa-backward'],function(){
			swal({
	           title: '你确定要返回吗？',
	           type: 'warning',
	           showCancelButton: true,
	           confirmButtonColor: '#3085d6',
	           cancelButtonColor: '#d33',
	           confirmButtonText: '确定!',
	           cancelButtonText: '取消'
	       }).then((result) => {
	           if (result.value) {
	              window.location.href = rootPath + 'console/article';
	           }
	       });
		},-1);

		editor.toolbarHelper.insertIcon(['fas','fa-save'],function(){
			$("#baseModal").modal('show')
		},2);
		
		var t;
		editor.editor.on('change',function(doc){
			if(t){
				clearTimeout(t);
			}
			t = setTimeout(function(){
				save(false);
			},3000)
		});
		
		editor.editor.on('paste', function(editor, evt) {
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
		
		var extraKeys = editor.editor.getOption('extraKeys');
		extraKeys['Ctrl-S'] = function(cm){
			$("#baseModal").modal('show');
		}
		editor.editor.setOption("extraKeys",extraKeys);
	})
})();


var base64Upload = function(f) {
	// show pick up
	if (f.size == 0) {
		bootbox.alert('不能上传空文件或者文件夹');
		return;
	}
	var reader = new FileReader();

	dirChooser.choose(function(dir, store) {
		var left = $(window).width() / 2 - 64;
		var top = $(window).height() / 2 - 64;
		$("#upload-loading").remove();
		$('body')
				.append(
						"<div id='upload-loading'><div class='modal-backdrop show'></div><img src='"
								+ rootPath
								+ "static/img/loading.gif' style='position:absolute;top:"
								+ top
								+ "px;left:"
								+ left
								+ "px' /></div>");
		reader.onload = (function(theFile) {
			return function(e) {
				var base64 = e.target.result;
				$.ajax({
					url : rootPath + 'api/console/store/'+store+'/files?base64Upload',
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
								editor.editor.replaceSelection(md);
							} else {
								var md = '['
										+ result.name
										+ ']('
										+ result.url
										+ ')';
								editor.editor.replaceSelection(md);
							}
						}
					}
				})
			};
		})(f);
		reader.readAsDataURL(f);
	});
}



var tags = [];
$(function() {

	$.ajax({
		
		url : rootPath + 'api/console/locks',
		success:function(data){
			var oldLock = $("#oldLock").val();
			var locks = data;
			if (locks.length > 0) {
				var html = '';
				html += '<div class="form-group">'
				html += '<label for="lockId" class="control-label">锁:</label> ';
				html += '<select id="lockId" class="form-control">';
				html += '<option value="">无</option>';
				for (var i = 0; i < locks.length; i++) {
					var lock = locks[i];
					if (lock.id == oldLock) {
						html += '<option value="' + lock.id
								+ '" selected="selected">'
								+ lock.name + '</option>';
					} else {
						html += '<option value="' + lock.id
								+ '">' + lock.name
								+ '</option>';
					}
				}
				html += '</select>';
				html += '</div>';
				$("#lock_container").html(html);
			}
		},
		error : function(jqXHR){
			swal('获取锁失败',$.parseJSON(jqXHR.responseText).error,'error');
		}
		
	})

	$("#status").change(function() {
		if ($(this).val() == 'SCHEDULED') {
			$("#scheduleContainer").show();
		} else {
			$("#scheduleContainer").hide();
		}
	});

	var oldTags = $("#oldTags").val();
	if (oldTags != '') {
		var oldTagArray = oldTags.split(",");
		for (var i = 0; i < oldTagArray.length; i++) {
			var tag = oldTagArray[i];
			if (tag != '') {
				addTag(tag);
				renderTag();
			}
		}
	}
	var oldSpace = $("#oldSpace").val();
	if (oldSpace != "") {
		$("#space").val(oldSpace);
	}



	$("#submit-art").click(function() {
		publishing = true;
		var me = $(this);
		var article = getArticle();
		me.prop("disabled", true);
		var type;
		var url = "";
		if (article.id && article.id != null) {
			type = 'put',
			url = rootPath + "api/console/article/"+article.id;
		} else {
			type = 'post',
			url = rootPath + "api/console/article";
		}
		$.ajax({
			type : type,
			url : url,
			contentType : "application/json",
			data : JSON.stringify(article),
			success : function(data) {
				swal("保存成功");
				setTimeout(function() {
					window.location.href = rootPath + 'console/article';
				}, 500)
			},
			error:function(jqXHR){
				swal('保存失败',$.parseJSON(jqXHR.responseText).error,'error');
			},
			complete : function() {
				me.prop("disabled", false);
			}
		});
	});

	$("#baseModal").on("show.bs.modal", function() {
		$("#error-tip").html('').hide();
	});

	$("#baseModal").on("shown.bs.modal", function() {
	});

});

function _addTag() {
	var me = $("#tags-input");
	addTag($.trim(me.val()));
	renderTag();
	me.val("");
	$("#add-tag-sign").remove();
}

function getArticle() {
	var article = {};
	article.title = $("#title").val();
	if ($.trim(article.title) == "") {
		article.title = "No title";
	}
	article.content = editor.editor.getDoc().getValue();
	article.from = $("#from").val();
	article.status = $("#status").val();
	if ($("#level").val() != '') {
		article.level = $("#level").val();
	}
	if (article.status == 'SCHEDULED') {
		article.pubDate = $("#scheduleDate").val()
	}
	article.isPrivate = $("#private").prop("checked");
	article.allowComment = $("#allowComment").prop("checked");
	article.tags = tags;
	article.featureImage = $("#featureImage").val();
	article.summary = $("#summary").val();
	article.space = {
		"id" : $("#space").val()
	};
	article.editor = 'MD';
	if ($("#lockId").val() != "") {
		article.lockId = $("#lockId").val();
	}
	article.alias = $("#alias").val();
	if ($("#id").val() != "") {
		article.id = $("#id").val();
	}
	return article;
}
var publishing = false;
function save(tip) {
	if (publishing) {
		return;
	}
	var article = getArticle();
	article.status = 'DRAFT';
	if (article.content == '') {
		return;
	}
	var type;
	var url = "";
	if (article.id && article.id != null) {
		type = 'put',
		url = rootPath + "api/console/article/"+article.id;
	} else {
		type = 'post',
		url = rootPath + "api/console/article";
	}
	publishing = true;
	$.ajax({
		type : type,
		url : url,
		async : false,
		contentType : "application/json",
		data : JSON.stringify(article),
		success : function(data) {
			if(data && data.id)
				$("#id").val(data.id);
		},
		error:function(jqXHR){
			if(tip){
				swal('保存失败',$.parseJSON(jqXHR.responseText).error,'error');
			}
		},
		complete : function() {
			publishing = false;
		}
	});
}

function showTagError(error) {
	if ($("#tag-tip").length == 0)
		$("#tags-input").before(error);
	setTimeout(function() {
		$("#tag-tip").remove();
	}, 1000);
}

function addTag(tag) {
	var tag = $.trim(tag);
	if (tags.length >= 10) {
		swal('最多只能有10标签','','error');
	} else if (tag == "" || tag.length > 20) {
		swal('标签名在1~20个字符之间','','error');
	} else {
		for (var i = 0; i < tags.length; i++) {
			var _tag = tags[i];
			if (_tag.name == tag) {
				swal('已经存在该标签','','error');
				$("#tags-input").val("");
				return;
			}
		}
		tags.push({
			"name" : $.trim(tag)
		});
	}
}

function renderTag() {
	if (tags.length == 0) {
		$("#tags-container").html('');
		return;
	}
	var html = '<div class="table-responsive"><table class="table table-borderless">';
	if (tags.length > 5) {
		html += '<tr>';
		for (var i = 0; i < 5; i++) {
			html += getLabel_html(tags[i].name);
		}
		html += '</tr>';
		html += '<tr>';
		for (var i = 5; i < tags.length; i++) {
			html += getLabel_html(tags[i].name);
		}
		html += '</tr>';
	} else {
		html += '<tr>';
		for (var i = 0; i < tags.length; i++) {
			html += getLabel_html(tags[i].name);
		}
		html += '</tr>';
	}
	html += '<table></div>';
	$("#tags-container").html(html);
}


function getLabel_html(tag) {
	return '<td><span class="badge badge-success">'
			+ tag
			+ '<a href="###" onclick="removeTag($(this))" style="margin-left:5px"><i class="fas fa-times"></i></a></span></td>';
}

function removeTag(o) {
	var tag = o.parent().text();
	for (var i = 0; i < tags.length; i++) {
		if (tags[i].name == tag) {
			tags.splice(i, 1);
			renderTag();
			return;
		}
	}
}

function previewSummary(o) {
	var content = $("#summary").val();
	var html = md.render(content);
	o.removeClass("fa-eye").addClass("fa-eye-slash").attr(
			"onclick", "inputSummry($(this))");
	$("#summary").hide();
	$("#summary-rendered").html(html).show();
}

function inputSummry(o) {
	o.removeClass("fa-eye-slash").addClass("fa-eye").attr(
			"onclick", "previewSummary($(this))");
	$("#summary-rendered").html('').hide();
	$("#summary").show();
}
