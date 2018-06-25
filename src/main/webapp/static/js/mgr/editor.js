	var publishing = false;
	var tags = [];
	var editor;
	$(function() {
		var mixedMode = {
		        name: "htmlmixed",
		        scriptTypes: [{matches: /\/x-handlebars-template|\/x-mustache/i,
		                       mode: null},
		                      {matches: /(text|application)\/(x-)?vb(a|script)/i,
		                       mode: "vbscript"}]
		      };
	  editor = CodeMirror.fromTextArea(document.getElementById("text"), {
        mode: mixedMode,
        lineNumbers: true,
        autoCloseTags: true
      });
		editor.setSize('100%',600)
		$("#previewLab").click(function(){
			var preview = $("#preview iframe").contents().find('#preview');
			preview.get(0).innerHTML = "<img src='"+basePath+"/static/img/loading.gif'/>";
			parseAndRender();
		});
		$('[data-md-handler]').click(function(){
			var m = $(this).attr("data-md-handler");
			switch(m){
			case 'file':
				fileChooser.choose(function(data){
					handleFile(data);
					return true;
				});
				break;
			case 'code':
				$("#code_area").val('');
				$("#codeModal").modal('show');
				break;
			}
		});
		
		$("#code-insert").click(function(){
			var code = $("#code_area").val();
			if($.trim(code) != ''){
				editor.replaceSelection('<pre><code>'+escapeHtml(code)+'</code></pre>')
			}
			$("#codeModal").modal('hide');
		});

		$("#status").change(function() {
			if ($(this).val() == 'SCHEDULED') {
				$("#scheduleContainer").show();
			} else {
				$("#scheduleContainer").hide();
			}
		});
		
		var oldTags = $("#oldTags").val();
		if(oldTags != ''){
			var oldTagArray = oldTags.split(",");
			for(var i=0;i<oldTagArray.length;i++){
				var tag = oldTagArray[i];
				if(tag != ''){
					addTag(tag);
					renderTag();
				}
			}
		}
		var oldSpace = $("#oldSpace").val();
		if(oldSpace != ""){
			$("#space").val(oldSpace);
		}

		$("#tags-input").keypress(function(e) {
			var me = $(this);
			// 回车键事件  
			if (e.which == 13) {
				addTag($.trim(me.val()));
				renderTag();
				me.val("");
			}
		});
		
		setInterval(function(){
			save();
		}, 10*1000)
		
		$("#submit-art").click(function(){
			publishing = true;
			var me = $(this);
			var article = getArticle();
			me.prop("disabled",true);
			var url = "";
			if(article.id && article.id != null){
				url = basePath+"/mgr/article/update";
			}else{
				url = basePath+"/mgr/article/write";
			}
			$.ajax({
				type : "post",
				url : url,
	            contentType:"application/json",
				data : JSON.stringify(article),
				success : function(data){
					if(data.success){
						bootbox.alert("保存成功");
						setTimeout(function(){
							window.location.href = basePath+'/mgr/article/index';
						},500)
					} else {
						bootbox.alert(data.message);
						publishing = false;
					}
				},
				complete:function(){
					me.prop("disabled",false);
				}
			});
		})
		$.get(basePath + '/mgr/lock/all',{},function(data){
			var oldLock = $("#oldLock").val();
			if(data.success){
				var locks = data.data;
				if(locks.length > 0){
					var html = '';
					html += '<div class="form-group">'
					html += '<label for="lockId" class="control-label">锁:</label> ';
					html += '<select id="lockId" class="form-control">';
					html += '<option value="">无</option>';
					for(var i=0;i<locks.length;i++){
						var lock = locks[i];
						if(lock.id == oldLock){
							html += '<option value="'+lock.id+'" selected="selected">'+lock.name+'</option>';
						}else{
							html += '<option value="'+lock.id+'">'+lock.name+'</option>';
						}
					}
					html += '</select>';
					html += '</div>';
					$("#lock_container").html(html);
				}
			}else{
				console.log(data.data);
			}
		});
	});

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
			showTagError('<div id="tag-tip" class="alert alert-danger">最多只能有10标签</div>')
		} else if (tag == "" || tag.length > 20) {
			showTagError('<div id="tag-tip" class="alert alert-danger">标签名在1~20个字符之间</div>')
		} else {
			for (var i = 0; i < tags.length; i++) {
				var _tag = tags[i];
				if (_tag.name == tag) {
					me.val("");
					return;
				}
			}
			tags.push({
				"name" : $.trim(tag)
			});
		}
	}

	function renderTag() {
		if(tags.length == 0){
			$("#tags-container").html('');
			return ;
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
	
	function save(){
		if(publishing){
			return ;
		}
		var article = getArticle();
		article.status = 'DRAFT';
		if(article.content == ''){
			return ;
		}
		var url = "";
		if(article.id && article.id != null){
			url = basePath+"/mgr/article/update";
		}else{
			url = basePath+"/mgr/article/write";
		}
		publishing = true;
		$.ajax({
			type : "post",
			url : url,
			async:false,
            contentType:"application/json",
			data : JSON.stringify(article),
			success : function(data){
				if(data.success){
					$("#id").val(data.data.id);
				}
			},
			complete:function(){
				publishing = false;
			}
		});
	}

	function getLabel_html(tag) {
		return '<td><span class="label label-success">'
				+ tag
				+ '<a href="###" onclick="removeTag($(this))" style="margin-left:5px"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></a></span></td>';
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
	
	var render = function(parsed) {
		if (parsed === undefined) {
			return;
		}
		var preview = $("#preview iframe").contents().find('#preview');
		try{
			preview.get(0).innerHTML = parsed;
		}catch(e){
			
		}
	};

	var parseAndRender = function() {
		$.ajax({
			type : "post",
			url : basePath+'/mgr/article/write/preview',
            contentType:"application/json",
			data : JSON.stringify(getArticle()),
			success : function(data){
				if(data.success){
					render(data.data);
				} else {
					render("");
				}
			},
			complete:function(){
			}
		});
	};
	
	
	function getArticle(){
		var article = {};
		article.title = $("#title").val();
		if($.trim(article.title) == ""){
			article.title = "No title";
		}
		article.content = editor.getValue();
		article.from = $("#from").val();
		article.status = $("#status").val();
		if($("#level").val() != ''){
			article.level = $("#level").val();
		}
		if(article.status == 'SCHEDULED'){
			article.pubDate = $("#scheduleDate").val()
		};
		article.isPrivate = $("#private").prop("checked");
		article.allowComment = $("#allowComment").prop("checked");
		article.tags = tags;
		article.featureImage = $("#featureImage").val();
		article.summary = $("#summary").val();
		article.space = {"id":$("#space").val()};
		article.editor = 'HTML';
		if($("#lockId").val() != ""){
			article.lockId = $("#lockId").val();
		}
		article.alias = $("#alias").val();
		if($("#id").val() != ""){
			article.id = $("#id").val();
		}
		return article;
	}
	
	function handleFile(data){
		var cf = data.cf;
		var ext = cf.extension.toLowerCase();
		if($.inArray(ext,['jpeg','jpg','png','gif']) == -1){
			editor.replaceSelection('<a href="'+cf.url+'" target="_blank" title="'+cf.originalFilename+'">'+cf.url+'</a>')
		} else {
			var thumb = cf.thumbnailUrl;
			if(thumb){
				editor.replaceSelection('<a href="'+thumb.large+'" target="_blank" title="'+cf.originalFilename+'"><img src="'+thumb.middle+'" alt="'+cf.originalFilename+'"/></a>')
			} else {
				editor.replaceSelection('<img src="'+cf.url+'"  alt="'+cf.originalFilename+'"/>')
			}
		}
	}
