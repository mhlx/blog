var editor = createEditor('editor',[{key:'Ctrl-S',fun:function(){
	save(true);
}},{key:'Ctrl-P',fun:function(){
	preview();
}}]);
var preEditorContent;
var saveFlag = false;
var templates = [];
$.ajax({
	type : 'get',
	url : rootPath + '/mgr/template/page/default',
	success : function(data){
		if(data.success){
			var pages = data.data;
			for(var i=0;i<pages.length;i++){
				var page = pages[i];
				templates.push({name : page.path,content : page.template})
			}
		} else {
			bootbox.alert(data.message);
		}
	}
	
})
$(document).ready(function() {
	$("#fsModal").on('shown.bs.modal',function(){
		$("#fsModal .modal-body").css({"overflow-y":'hidden',"padding":"0px"})
		$("#fs-url").css({'height':$("#fsModal .modal-body").height()+"px"});
	});
	editor.setSize('100%', $(window).height() - 30);
	sfq.setFileClickFunction(function(path){
		editor.insertUrl(path,true);
		return true;
	});
	
	preEditorContent = editor.getValue();
	
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
				fileChooser.choose(function(data){
					handleFile(data);
					return true;
				});
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
	function addDataTag(dataName){
		for(var i=0;i<dataTags.length;i++){var tag =dataTags[i];
			if(tag.dataName == dataName){
				
				var html = '<data name="'+dataName+'"';
				if(tag.attrs.length > 0){
					for(var j=0;j<tag.attrs.length;j++){
						html += " "+tag.attrs[j]+"=\"\"";
					}
				}
				html += '/>';
				
				editor.replaceSelection(html);
				break;
			}
		}
		$("#lookupModal").modal('hide');
	}
	function addFragment(name){
		editor.replaceSelection('<fragment name="'+name+'"/>');
		$("#lookupModal").modal('hide')
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
		html += '<tr><td>路径</td><td></td></tr>';
		for(var i=0;i<templates.length;i++){
			var name = templates[i].name;
			if(name == ''){
				name = '/'
			}
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
	var dataTags = [];
	function showDataTags(){
		var html = '';
		$('[aria-labelledby="data-tab"]').html('<img src="'+basePath+'/static/img/loading.gif" class="img-responsive center-block"/>')
		$.get(basePath+"/mgr/template/dataTags",{},function(data){
			if(!data.success){
				bootbox.alert(data.message);
				return ;
			}
			data = data.data;
			dataTags = data;
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
	
	function preview() {
		var page = {"tpl":editor.getValue()};
		var space = $("#spaceSelect").val();
		if(space != null && $.trim(space) != ''){
			page.space = {"id":space}
		}
		var id = $("#pageId").val();
		if(id != null && $.trim(id) != ''){
			page.id = id;
		}
		page.name="test";
		page.description="";
		if($.trim($("#alias").val()) != ''){
			page.alias = $.trim($("#alias").val());
		}
		page.allowComment = $("#allowComment").prop("checked");
		$.ajax({
			type : "post",
			url : basePath + '/mgr/template/page/preview',
			data : JSON.stringify(page),
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				if (data.success) {
					var url = data.data;
                  var ext;
                  if(page.alias){
                  	ext = getFileExtension(page.alias);
                  }else{
                  	ext = '';
                  }
					if(ext != 'html' && ext != ''){
						if(url.hasPathVariable){
							bootbox.prompt({title : "预览路径为<p><b>"+url.url+"</b></p><p>该地址不是一个网页地址，并且路径中包含可变参数，请输入确切地址</p>",value: url.url,callback: function(result){ 
								if(result != null){
									window.open(result);
								}
							}});
						} else {
							bootbox.alert("<p>预览地址不是一个网页地址，请点击链接进行预览：</p><p><a href='"+url.url+"' target='_blank'>"+url.url+"</a></p>")
						}
					} else {
						if(url.hasPathVariable){
							bootbox.prompt({title : "预览路径为<p><b>"+url.url+"</b></p><p>该地址中包含可变参数，请输入确切地址</p>",value: url.url,callback: function(result){ 
								if(result != null){
									$("#fs-url").attr('src',result);
									$("#fsModalLabel").html("预览:"+result);
									$("#fsModal").modal('show');
								}
							}});
						} else {
							$("#fs-url").attr('src',url.url);
							$("#fsModalLabel").html("预览:"+url.url);
							$("#fsModal").modal('show');
						}
					}
				} else {
					bootbox.alert(data.message);
				}
			},
			complete:function(){
			}
		});
	}
	
	function save(quick) {
		var page = {"tpl":editor.getValue()};
		var space = $("#spaceSelect").val();
		if(space != ''){
			page.space = {"id":space}
		}
		var url = basePath + '/mgr/template/page/create';
		var id = $("#pageId").val();
		if(id != null && $.trim(id) != ''){
			page.id = id;
			url = basePath + '/mgr/template/page/update';
		}
		if($.trim($("#alias").val()) != ''){
			page.alias = $.trim($("#alias").val());
		}
		page.name=$("#name").val();
		page.description=$("#description").val();
		page.allowComment = $("#allowComment").prop("checked");
		page.spaceGlobal = $("#spaceGlobal").prop('checked');
		saveFlag = true;
		$.ajax({
			type : "post",
			data : JSON.stringify(page),
			url:url,
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				if (data.success) {
					if($("#pageKey").val() == "page_"+$("#pageId").val())
						page_storage.removeCurrent();
					$("#pageId").val(data.data.id);
					$("#pageKey").val("page_"+$("#pageId").val());
					bootbox.alert("保存成功");
					if(!quick){
						$("#previewModal").modal('hide');
					}
				} else {
					saveFlag = false;
					if(quick){
						$("#previewModal").modal('show');
						setTimeout(function(){
							bootbox.alert(data.message);
						},500)
					} else {
						bootbox.alert(data.message);
					}
				}
			},
			complete:function(){
			
			}
		});
	}
	
	
	var page_storage = (function() {
		
		var current_tpl;
		
		var getKey = function(){
			var key = $("#pageKey").val();
			if($.trim(key) == ''){
				key = "page_"+$.now();
				$("#pageKey").val(key);
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
					$("#auto-save-timer").html("最近备份："+new Date(time).format('yyyy-mm-dd HH:MM:ss'));
					preEditorContent = content;
				}
			}
			else
				page_storage.removeCurrent();
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
					if(key.indexOf('page_') > -1){
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
				var key = $("#pageKey").val();
				if($.trim(key) != ''){
					local_storage.remove(key);
				}
			}
		}
	}());
	
	function loadBak(key){
		bootbox.confirm("确定要加载吗",function(result){
			if(result){
				var v = page_storage.get(key);
				if(v != null){
					v = $.parseJSON(v);
					$("#pageKey").val(v.id);
					editor.setValue(v.content);
				}
				$("#backupModal").modal('hide');
			}
		})
	}
	
	function rewriteBaks(){
		var baks = page_storage.listAll();
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
				page_storage.remove(key);
				rewriteBaks();
			}
		})
	}
	
	function loadHistoryTemplate(){
		var id = $("#pageId").val();
		if(id == ''){
			return ;
		}
		$.get(basePath + '/mgr/template/page/'+id+'/history',{},function(data){
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
									preEditorContent = data.data.tpl;
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

var getFileExtension = function (url) {
    "use strict";
    if (url === null) {
        return "";
    }
    var index = url.lastIndexOf("/");
    if (index !== -1) {
        url = url.substring(index + 1); // Keep path without its segments
    }
    index = url.indexOf("?");
    if (index !== -1) {
        url = url.substring(0, index); // Remove query
    }
    index = url.indexOf("#");
    if (index !== -1) {
        url = url.substring(0, index); // Remove fragment
    }
    index = url.lastIndexOf(".");
    return index !== -1
        ? url.substring(index + 1) // Only keep file extension
        : ""; // No extension found
};

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