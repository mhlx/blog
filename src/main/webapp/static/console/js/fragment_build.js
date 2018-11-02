var preEditorContent = "";
	var saveFlag = false;
	var defaultFragments;
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
	    $("#beautiful").click(function(){
	    	editor.format();
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
	    $("#fragment").click(function() {
	        if (!defaultFragments) {
	            $.ajax({
	                url: root + 'api/console/template/defaultFragments',
	                success: function(data) {
	                    defaultFragments = data;
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
	        for (var i = 0; i < defaultFragments.length; i++) {
	            var p = defaultFragments[i];
	            var name = p.name;
	            html += '<tr><td>' + name + '</td><td><a href="###" onclick="loadTemplate(\'' + i + '\')">加载</a></td></tr>';
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
			url : root + 'api/console/template/fragment/preview',
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				swal('预览成功','预览成功，请自行访问拥有该模板片段的页面预览效果','success');
			},
			error:function(jqXHR){
				var error = $.parseJSON(jqXHR.responseText).error;
				swal('预览失败',error,'error');
			}
		});
	}


	function loadTemplate(i) {
	    var template = defaultFragments[i];
	    editor.setValue(template.tpl);
	    $("#templateModal").modal('hide');
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
		var url = root + "api/console/template/fragment";
		if(id != ''){
			isSave = false;
			url = root + "api/console/template/fragment/"+id;
		}
		if(!isSave){
			data.id = id;
		}
		var method = isSave ? 'POST' : 'PUT';
		saveFlag = true;
		$.ajax({
			type : method,
			url : url,
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data) {
					swal("保存成功",'','success');
					fragment_storage.removeCurrent();
					$("#fragmentId").val(data.id);
					$("#fragmentKey").val("fragment_"+data.id)
					if(!quick){
						$("#previewModal").modal('hide');
					}
			},
			error : function(jqXHR) {
				saveFlag = false;
				if(quick){
					$("#previewModal").modal('show');
					swal('保存失败',$.parseJSON(jqXHR.responseText).error,'error');
				}
			}
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

	$('#backupModal').on('show.bs.modal',function(){
		rewriteBaks();
	});
	
	function rewriteBaks(){
		var baks = fragment_storage.listAll();
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
			  var v = fragment_storage.get(key);
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
			  fragment_storage.remove(key);
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