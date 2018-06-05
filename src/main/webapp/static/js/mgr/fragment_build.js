var editor = createEditor('editor',[{key:'Ctrl-S',fun:function(){
	save(true);
}},{key:'Ctrl-P',fun:function(){
	preview();
}}]);
var saveFlag = false;
var preEditorContent;
var templates = [];
$.ajax({
	type : 'get',
	url : rootPath + '/mgr/template/fragment/default',
	success : function(data){
		if(data.success){
			var fragments = data.data;
			for(var i=0;i<fragments.length;i++){
				var fragment = fragments[i];
				templates.push({name : fragment.name,content : fragment.tpl})
			}
		} else {
			bootbox.alert(data.message);
		}
	}
	
})
$(document).ready(function() {
	$("input[name='global']").change(function(){
		if($(this).is(":checked")){
			$("#spaceSelector").hide();
		}else{
			$("#spaceSelector").show();
		}
	})
	editor.setSize('100%', $(window).height() - 30);
	
	preEditorContent = editor.getValue();
	sfq.setFileClickFunction(function(path){
		editor.insertUrl(path,true);
		return true;
	});
	
	$('#backupModal').on('show.bs.modal',function(){
		rewriteBaks();
	});
	
		$("#lookupModal").on("show.bs.modal", function() {
			showDataTags();
		});
		$('[data-handler]').click(function(){
			var m = $(this).attr("data-handler");
			switch(m){
			case 'file':
				fileSelectPageQuery(1,'');
	        	$("#fileSelectModal").modal("show");
				break;
			case 'localFile':
				sfq.show();
				break;
			case 'clear':
				bootbox.confirm("确定要清空吗？",function(result){
					if(result){
						editor.clear();
					}
				})
				break;
			case 'format':
				editor.format();
				break;
			case 'lookup':
				lookup();
				break;
			case 'template':
				showTemplateModal();
				break;
			case 'lock':
				showLock();
				break;
			case 'historyTemplate':
				loadHistoryTemplate();
				break;
			default:
				break;
			}
		})
		$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
			var html = '';
			var id = $(e.target).attr('id');
			switch(id){
			case "data-tab":
				showDataTags();
				break;
			case "fragment-tab":
				showUserFragment(1)
				break;
			}
		});
		
	});
	 $(document).keydown(function(e) {
	    if (e.ctrlKey && e.shiftKey && e.which === 70) {
	        editor.format()
	        return false;
	    }
	    return true;
	 });
	function addDataTag(name){
		editor.replaceSelection('<data name="'+name+'"/>');
		$("#lookupModal").modal('hide');
	}
	function addFragment(name){
		editor.replaceSelection('<fragment name="'+name+'"/>');
		$("#lookupModal").modal('hide')
	}
	function preview() {
		var data = $("#previewModal form").serializeObject();
		var space = data.space;
		delete data['space'];
		if(space != ''){
			data.space = {"id":space};
		}
		data.global = $("#previewModal form input[type=checkbox]").eq(0).prop("checked");
		data.callable = $("#previewModal form input[type=checkbox]").eq(1).prop("checked");
		data.tpl = editor.getValue();
		$.ajax({
			type : "post",
			url : basePath + '/mgr/template/fragment/preview',
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				if (data.success) {
					bootbox.alert("预览成功，请自行访问拥有该模板片段的页面预览效果");
				} else {
					bootbox.alert(data.message);
				}
			},
			complete:function(){
			}
		});
	}
	function showLock(){
		$.get(basePath + '/mgr/lock/all',{},function(data){
			var oldLock = $("#oldLock").val();
			if(data.success){
				var locks = data.data;
				var html = '';
				if(locks.length > 0){
					html += '<div class="table-responsive">';
					html += '<table class="table">';
					for(var i=0;i<locks.length;i++){
						html += '<tr>';
						var lock = locks[i];
						html += '<tr>';
						html += '<td>'+lock.name+'</td>';
						html += '<td><a href="###" onclick="addLock(\''+lock.id+'\')"><span class="glyphicon glyphicon-ok-sign"></span></a></td>';
						html += '</tr>';
					}
					html += '</table>';
					html += '</div>';
					$("#lockBody").html(html);
					$("#lockModal").modal('show')
				} else {
					bootbox.alert("当前没有任何锁");
				}
			}else{
				console.log(data.data);
			}
		});
	}
	
	
	function showTemplateModal(){
		if(!templates || templates.length == 0){
			bootbox.alert("沒有可供访问的地址");
			return ;
		}
		var html = "<div class='table-responsive'>";
		html += '<table class="table">';
		html += '<tr><td>名称</td><td></td></tr>';
		for(var i=0;i<templates.length;i++){
			var name = templates[i].name;
			html += '<tr><td>'+name+'</td><td><a href="javascript:void(0)" onclick="loadTemplate(\''+templates[i].name+'\')">加载</a></td></tr>';
		}
		html += '</table>';
		html += '</div>';
		$("#templateModalBody").html(html);
		$("#templateModal").modal('show');
	}
	
	function loadTemplate(name){
		for(var i=0;i<templates.length;i++){
			var template = templates[i];
			if(template.name == name){
				editor.setValue(template.content);
				preEditorContent = template.content;
				$("#templateModal").modal('hide');
				break;
			}
		}
	}
	
	function addLock(id){
		editor.replaceSelection('<lock id="'+id+'"/>');
		$("#lockModal").modal('hide')
	}
	
	function lookup(){
		$("#lookupModal").modal('show');
	}
	
	function showDataTags(){
		var html = '';
		$('[aria-labelledby="data-tab"]').html('<img src="'+basePath+'/static/img/loading.gif" class="img-responsive center-block"/>')
		$.get(basePath+"/mgr/template/dataTags",{},function(data){
			if(!data.success){
				bootbox.alert(data.message);
				return ;
			}
			data = data.data;
			html += '<div class=" table-responsive" style="margin-top:10px">';
			html += '<table class="table">';
			for(var i=0;i<data.length;i++){
				html += '<tr>';
				html += '<td>'+data[i].name+'</td>';
				html += '<td><a onclick="addDataTag(\''+data[i].dataName+'\')" href="###"><span class="glyphicon glyphicon-ok-sign" ></span>&nbsp;</a></td>';
				html += '</tr>';
			}
			html += '</table>';
			html += '</div>';
			$('[aria-labelledby="data-tab"]').html(html);
		});
	}
	
	function showUserFragment(i){
		var html = '';
		$('#fragment').html('<img src="'+basePath+'/static/img/loading.gif" class="img-responsive center-block"/>')
		$.get(basePath+"/mgr/template/fragment/list",{"currentPage":i},function(data){
			if(!data.success){
				bootbox.alert(data.message);
				return ;
			}
			var page = data.data;
			html += '<div class=" table-responsive" style="margin-top:10px">';
			html += '<table class="table">';
			for(var i=0;i<page.datas.length;i++){
				html += '<tr>';
				html += '<td>'+page.datas[i].name+'</td>';
				html += '<td><a onclick="addFragment(\''+page.datas[i].name+'\')" href="###"><span class="glyphicon glyphicon-ok-sign" ></span>&nbsp;</a></td>';
				html += '</tr>';
			}
			html += '</table>';
			html += '</div>';
			
			if(page.totalPage > 1){
				html += '<div>';
				html += '<ul class="pagination">';
				for(var i=page.listbegin;i<=page.listend-1;i++){
					html += '<li>';
					html += '<a href="###" onclick="showUserFragment(\''+i+'\')" >'+i+'</a>';
					html += '</li>';
				}
				html += '</ul>';
				html += '</div>';
			}
			$('#fragment').html(html);
		});
	}
	
	
	var fragment_storage = (function() {
		
		var current_tpl;
		
		var getKey = function(){
			var key = $("#fragmentKey").val();
			if($.trim(key) == ''){
				key = "fragment_"+$.now();
				$("#fragmentKey").val(key);
			}
			return key;
		}
		
		setInterval(function(){
			if(saveFlag) return ;
			var content = editor.getValue();
			if($.trim(content) != ''){
				
				if(!preEditorContent || content != preEditorContent){
					var time = $.now();
					local_storage.store(getKey(),JSON.stringify({"id":getKey(),"content":content,"time":time}));
					$("#auto-save-timer").html("最近备份："+new Date(time).format('yyyy-mm-dd HH:MM:ss'))
					preEditorContent = content;
				}
			}
			else
				fragment_storage.removeCurrent();
		},15000);
		
		var v = local_storage.get(getKey());
		if(v != null){
			v = $.parseJSON(v);
			current_tpl = editor.getValue();
			bootbox.confirm("系统发现在"+new Date(v.time).format('yyyy-mm-dd HH:MM:ss')+"留有备份，是否加载？",function(result){
				if(result){
					editor.setValue(v.content);
					preEditorContent= v.content;
				}
			})
		}
		
		return {
			listAll:function(){
				var arr = [];
				local_storage.each(function(key,v){
					if(key.indexOf('fragment_') > -1){
						arr.push({'key':key,"value":v});
					}
				});
				arr.sort(function(x,y){
					var v1 = $.parseJSON(x.value);
					var v2 = $.parseJSON(y.value);
					return  -v1.time + v2.time;
				})
				return arr;
			},
			get:function(key){
				return local_storage.get(key);
			},
			remove:function(key){
				local_storage.remove(key);
			},
			removeCurrent:function(){
				var key = $("#fragmentKey").val();
				if($.trim(key) != ''){
					local_storage.remove(key);
				}
			}
		}
	}());
	
	function loadBak(key){
		bootbox.confirm("确定要加载吗",function(result){
			if(result){
				var v = fragment_storage.get(key);
				if(v != null){
					v = $.parseJSON(v);
					$("#fragmentKey").val(v.id);
					editor.setValue(v.content);
				}
				$("#backupModal").modal('hide');
			}
		})
	}
	
	function rewriteBaks(){
		var baks = fragment_storage.listAll();
		var html = '<div class="table-responsive">';
		html += '<table class="table">';
		html += '<tr><th>ID</th><th>时间</th><th>操作</th></tr>'
		for(var i=0;i<baks.length;i++){
			var v = $.parseJSON(baks[i].value);
			html += '<tr><td>'+v.id+'</td><td>'+new Date(v.time).format('yyyy-mm-dd HH:MM:ss')+'</td><td><a href="###" onclick="loadBak(\''+v.id+'\')" style="margin-right:5px">加载</a><a href="###" onclick="delBak(\''+v.id+'\')">删除</a></td></tr>';
		}
		html += '</table>';
		html += '</div>';
		$("#backup-body").html(html);
	}
	
	function delBak(key){
		bootbox.confirm("确定要删除吗",function(result){
			if(result){
				fragment_storage.remove(key);
				rewriteBaks();
			}
		})
	}
	
	function save(quick){
		var data = $("#previewModal form").serializeObject();
		var space = data.space;
		delete data['space'];
		if(space != ''){
			data.space = {"id":space};
		}
		data.global = $("#previewModal form input[type=checkbox]").eq(0).prop("checked");
		data.callable = $("#previewModal form input[type=checkbox]").eq(1).prop("checked");
		data.tpl = editor.getValue();
		
		var id = $("#fragmentId").val();
		var isSave = true;
		var url = basePath + "/mgr/template/fragment/create";
		if(id != ''){
			isSave = false;
			url = basePath + "/mgr/template/fragment/update";
		}
		if(!isSave){
			data.id = id;
		}
		saveFlag = true;
		$.ajax({
			type : "post",
			url : url,
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data) {
				if (data.success) {
					bootbox.alert("保存成功");
					fragment_storage.removeCurrent();
					$("#fragmentId").val(data.data.id);
					$("#fragmentKey").val("fragment_"+data.data.id)
					if(!quick){
						$("#previewModal").modal('hide');
					}
				} else {
					if(quick){
						$("#previewModal").modal('show');
						setTimeout(function(){
							bootbox.alert(data.message);
						})
					}
				}
			},
			complete : function() {
				saveFlag = false;
			}
		});
	}

	function loadHistoryTemplate(){
		var id = $("#fragmentId").val();
		if(id == ''){
			return ;
		}
		$.get(basePath + '/mgr/template/fragment/'+id+'/history',{},function(data){
			if(data.success){
				data = data.data;
				if(data.length == 0){
					bootbox.alert("没有历史模板记录");
				} else {
					var html = '<table class="table">';
					html += '<tr><th>备注</th><th>时间</th><th>操作</th></tr>';
					for(var i=0;i<data.length;i++){
						var remark = data[i].remark;
						if(remark.length > 10){
							remark = remark.substring(0,10)+"...";
						}
						html += '<tr><td><a href="###" data-toggle="tooltip" title="'+data[i].remark+'">'+remark+'</a></td><td>'+new Date(data[i].time).format('yyyy-mm-dd HH:MM:ss')+'</td><td><a href="###" data-id="'+data[i].id+'" data-toggle="confirmation"  style="margin-right:10px"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></a></td></tr>';
					}
					html += '</table>';
					$("#historyTableContainer").html(html);
					$('#historyTableContainer [data-toggle="tooltip"]').tooltip();
					$("#history-tip").html('');
					$("#historyModal").modal('show');
					
					$('[data-toggle=confirmation]').confirmation({
						 rootSelector: '#historyTableContainer',
						 onConfirm:function(){
							 var me = $(this);
							 var id = me.attr('data-id');
							 $.get(basePath + '/mgr/template/history/get/'+id,{},function(data){
								if(data.success){
									editor.setValue(data.data.tpl);
									preEditorContent= data.data.tpl;
									$("#historyModal").modal('hide')
								}else{
									$("#history-tip").html('<div class="alert alert-warning alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+data.message+'</div>');
								}
							});
						 }
					});
				}
			}else{
				bootbox.alert(data.message);
			}
		});
	}
