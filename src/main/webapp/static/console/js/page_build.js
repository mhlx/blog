var preEditorContent = "";
	var saveFlag = false;
	var defaultPages;
	var editor = createEditor('editor', [{
	    key: 'Ctrl-S',
	    fun: function() {save(true)}
	}, {
	    key: 'Ctrl-P',
	    fun: function() {preview()}
	}]);
	$(function() {
	    editor.setSize('100%', $(window).height() - 60);
	    
	    $.ajax({
			type : 'GET',
			url : root + 'api/console/locks',
			success:function(data) {
				var html = '<div class="table-responsive">';
				html += '<table class="table">';
				html += '<tr><th>名称</th><th></th></tr>'
				$.each(data, function(index, v) {
					html += '<tr><td>'+v.name+'</td><td><a href="###" data-lock="'+v.id+'" >加载</a></td></tr>';
				});
				html += '</table>';
				html += '<div>';
				$("#lockBody").html(html);
			},
			error:function(jqXHR, textStatus, errorThrown) {
				var data = $.parseJSON(jqXHR.responseText);
				swal('获取访问锁失败',data.error,'error');
			}
		  })
	    
	    $("#file").click(
	        function() {
	            fileChooser
	                .choose(function(datas) {
	                    for (var i = 0; i < datas.length; i++) {
	                        var data = datas[i];
	                        var cf = data.cf;
	                        var ext = cf.extension
	                            .toLowerCase();
	                        if ($.inArray(ext, ['jpeg',
	                                'jpg', 'png', 'gif'
	                            ]) == -1) {
	                            editor
	                                .replaceSelection('<a href="' + cf.url + '" target="_blank" title="' + cf.originalFilename + '">' +
	                                    cf.url +
	                                    '</a>')
	                        } else {
	                            var thumb = cf.thumbnailUrl;
	                            if (thumb) {
	                                editor
	                                    .replaceSelection('<a href="' + thumb.large + '" target="_blank" title="' + cf.originalFilename + '"><img src="' + thumb.middle + '" alt="' + cf.originalFilename + '"/></a>')
	                            } else {
	                                editor
	                                    .replaceSelection('<img src="' + cf.url + '"  alt="' + cf.originalFilename + '"/>')
	                            }
	                        }
	                    }
	                })
	        });


	    $("#clear").click(function() {
	        swal({
	            title: '你确定要清空吗？',
	            type: 'warning',
	            showCancelButton: true,
	            confirmButtonColor: '#3085d6',
	            cancelButtonColor: '#d33',
	            confirmButtonText: '清空!',
	            cancelButtonText: '取消'
	        }).then((result) => {
	            if (result.value) {

	                editor.clear();
	            }
	        });
	    });

	    $("#lock").click(function() {
	        $("#lockModal").modal('show')
	    });
	    $("#beautiful").click(function(){
	    	editor.format();
	    })
	    $("#page").click(function() {
	        if (!defaultPages) {
	            $.ajax({
	                url: root + 'api/console/template/defaultPages',
	                success: function(data) {
	                    defaultPages = data;
	                    renderDefaultTemplates();
	                },
	                error: function(jqXHR) {
	                    var message = $.parseJSON(jqXHR.responseText).error;
	                    swal("获取默认页面失败", "", "error")
	                }
	            });
	        } else {
	            renderDefaultTemplates();
	        }
	    });


	    var renderDefaultTemplates = function() {
	        var html = '<div class="table-responsive">';
	        html += '<table class="table">';
	        html += '<tr><th>路径</th><th></th></tr>';
	        for (var i = 0; i < defaultPages.length; i++) {
	            var p = defaultPages[i];
	            var path = p.path;
	            if (path == '') path = '/';
	            html += '<tr><td>' + path + '</td><td><a href="###" onclick="loadTemplate(\'' + i + '\')">加载</a></td></tr>';
	        }
	        html += '</table>';
	        html += '</div>';
	        $("#templateModalBody").html(html);
	        $("#templateModal").modal('show');
	    }

	    $("#preview").click(function() {
	       preview();
	    })
	    $("#save").click(function() {
	    	$("#previewModal").modal('show');
	    })
 		$("#lockModal").on('click','[data-lock]',function(){
	    	var id = $(this).data('lock');
	    	editor.replaceSelection('<lock id="'+id+'"></lock>');
	    	$("#lockModal").modal('hide');
	    })

	    $("#backup").click(function(){
	    	$('#backupModal').modal('show');
	    })
	    $('#backupModal').on('show.bs.modal',function(){
			rewriteBaks();
		});
	    $("#query").click(function(){
	    	$("#lookupModal").modal('show');
	    });
	});

	function preview() {
	    var page = {
	        "tpl": editor.getValue()
	    };
	    var space = $("#spaceSelect").val();
	    if (space != null && $.trim(space) != '') {
	        page.space = {
	            "id": space
	        }
	    }
	    var id = $("#pageId").val();
	    if (id != null && $.trim(id) != '') {
	        page.id = id;
	    }
	    page.name = "test";
	    page.description = "";
	    if ($.trim($("#alias").val()) != '') {
	        page.alias = $.trim($("#alias").val());
	    }
	    page.allowComment = $("#allowComment").prop("checked");
	    $.ajax({
	        type: "post",
	        url: root + 'api/console/template/page/preview',
	        data: JSON.stringify(page),
	        dataType: "json",
	        contentType: 'application/json',
	        success: function(data) {
	            var url = data;
	            var ext;
	            if (page.alias) {
	                ext = getFileExtension(page.alias);
	            } else {
	                ext = '';
	            }
	            $("#preview-url").val(url.url);
	            $("#previewUrlModal").modal('show');
	        },
	        error: function(jqXHR) {
	            var text = jqXHR.responseText;
	            swal($.parseJSON(text).error, '', 'error');
	        }
	    });
	    
	    
	}


	function loadTemplate(i) {
	    var template = defaultPages[i];
	    editor.setValue(template.template);
	    $("#templateModal").modal('hide');
	}
	
	function save(quick) {
		var page = {"tpl":editor.getValue()};
		var space = $("#spaceSelect").val();
		if(space != ''){
			page.space = {"id":space}
		}
		var url = root + 'api/console/template/page';
		var id = $("#pageId").val();
		var update = id != null && $.trim(id) != ''
		if(update){
			page.id = id;
			url = root + 'api/console/template/page/'+id;
		}
		if($.trim($("#alias").val()) != ''){
			page.alias = $.trim($("#alias").val());
		}
		page.name=$("#name").val();
		page.description=$("#description").val();
		page.allowComment = $("#allowComment").prop("checked");
		page.spaceGlobal = $("#spaceGlobal").prop('checked');
		saveFlag = true;
		var method = update ? 'PUT' : 'POST'
		$.ajax({
			type : method,
			data : JSON.stringify(page),
			url:url,
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				if($("#pageKey").val() == "page_"+$("#pageId").val())
					page_storage.removeCurrent();
				if(data){
					$("#pageId").val(data.id);
				}
				$("#pageKey").val("page_"+$("#pageId").val());
				swal("保存成功",'','success');
				if(!quick){
					$("#previewModal").modal('hide');
				}
			},error:function(jqXHR){
				saveFlag = false;
				var error = $.parseJSON(jqXHR.responseText).error;
				if(quick){
					$("#previewModal").modal('show');
						swal('保存失败',error,'error')
				} else {
					swal('保存失败',error,'error')
				}
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
			swal({
				  title: '要加载备份吗？',
				  type: 'warning',
				  text : "系统发现在"+moment(v.time).format('YYYY-MM-DD HH:mm:ss')+"留有备份，是否加载？",
				  showCancelButton: true,
				  confirmButtonColor: '#3085d6',
				  cancelButtonColor: '#d33',
				  confirmButtonText: '加载!',
				  cancelButtonText: '取消'
				}).then((result) => {
			  if (result.value) {
				  editor.setValue(v.content);
					preEditorContent= v.content;
			  }
			});
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
	$('#backupModal').on('show.bs.modal',function(){
		rewriteBaks();
	});
	
	function rewriteBaks(){
		var baks = page_storage.listAll();
		var html = '<div class="table-responsive">';
		html += '<table class="table">';
		html += '<tr><th>ID</th><th>时间</th><th>操作</th></tr>'
		for(var i=0;i<baks.length;i++){
			var v = $.parseJSON(baks[i].value);
			html += '<tr><td>'+v.id+'</td><td>'+moment(v.time).format('YYYY-MM-DD HH:mm:ss')+'</td><td><a href="###" onclick="loadBak(\''+v.id+'\')" style="margin-right:5px">加载</a><a href="###" onclick="delBak(\''+v.id+'\')">删除</a></td></tr>';
		}
		html += '</table>';
		html += '</div>';
		$("#backup-body").html(html);
	}
	
	var clipboard=new Clipboard('[data-clipboard-target]');
	clipboard.on('success',function(e){
		 e.clearSelection();
		swal('拷贝成功','','success');
	});
	clipboard.on('error',function(){
		swal('拷贝失败','','error');
	});
	
	function loadBak(key){
		swal({
			  title: '你确定吗？',
			  type: 'warning',
			  showCancelButton: true,
			  confirmButtonColor: '#3085d6',
			  cancelButtonColor: '#d33',
			  confirmButtonText: '加载!',
			  cancelButtonText: '取消'
			}).then((result) => {
		  if (result.value) {
			  var v = page_storage.get(key);
				if(v != null){
					v = $.parseJSON(v);
					$("#pageKey").val(v.id);
					editor.setValue(v.content);
				}
				$("#backupModal").modal('hide');
		  }
		});
	}
	
	function delBak(key){
		swal({
			  title: '你确定吗？',
			  type: 'warning',
			  showCancelButton: true,
			  confirmButtonColor: '#3085d6',
			  cancelButtonColor: '#d33',
			  confirmButtonText: '加载!',
			  cancelButtonText: '取消'
			}).then((result) => {
		  if (result.value) {
			  page_storage.remove(key);
				rewriteBaks();
		  }
		});
	}
	
	var dataTags;
	
	var dataTable = datatable('dataTable',{
		url :function(){
			return  root+'api/console/template/datas';
		},
		dataConverter:function(data){
			dataTags = data;
			return data;
		},
		columns:[{
			bind:'xx',
			render:function(v,d){
				return '<a href="###" onclick="addDataTag(\''+d.dataName+'\')">加载</a>';
			}
		}]
	});
	
	var fragmentTable = datatable('fragmentTable',{
		url :function(){
			return  root+'api/console/template/fragments';
		},
		paging:true,
		columns:[{
			bind:'xx',
			render:function(v,d){
				return '<a href="###" onclick="addFragment(\''+d.name+'\')">加载</a>';
			}
		}]
	});
	
	function addDataTag(dataName){
		for(var i=0;i<dataTags.length;i++){
			var tag =dataTags[i];
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
	
