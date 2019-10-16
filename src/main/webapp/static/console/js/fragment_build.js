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

		editor.setOption("fullScreen", true);
		editor.getWrapperElement().style.top = 60+'px'
	    
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
				Swal.fire('获取访问锁失败',data.error,'error');
			}
		  })
	    $("#beautiful").click(function(){
	    	editor.format();
	    })
	    		$("#doc").click(function(){
			$("#docModal").modal('show');
		});
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
	        Swal.fire({
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
	    
	    $("#back").click(function(){
	    	 Swal.fire({
		            title: '你确定要返回吗？',
		            type: 'warning',
		            showCancelButton: true,
		            confirmButtonColor: '#3085d6',
		            cancelButtonColor: '#d33',
		            confirmButtonText: '确定!',
		            cancelButtonText: '取消'
		        }).then((result) => {
		            if (result.value) {
		            	window.location.href = root + 'console/template/fragment'
		            }
		        });
	    });
	    
	    var historyTable;
	    $("#history").click(function() {
	    	var id = $("#fragmentId").val();
	    	if(id){
	    		if(!historyTable){
	    			historyTable = datatable('historyTable',{
	    				url : function(){
	    					return root+'api/console/template/fragment/'+id+'/histories'
	    				},
	    				columns:[{
	    					bind : 'time',
	    					render:function(v){
	    						return moment(v).format('YYYY-MM-DD HH:mm');
	    					}
	    				},{
	    					bind : 'id',
	    					render:function(v){
	    						return '<a href="###" data-load="'+v+'">加载</a>';
	    					}
	    				}]
	    			});
	    		}else{
	    			historyTable.reload();
	    		}
	    		$("#historyModal").modal('show');
	    	}else{
	    		Swal.fire("新模板片段无法获取历史模板", "", "error")
	    	}
	    });
	    
	    $("#historyTable").on('click','[data-load]',function(){
	    	var id = $(this).data('load');
	    	Swal.fire({
				  title: '你确定吗？',
				  type: 'warning',
				  showCancelButton: true,
				  confirmButtonColor: '#3085d6',
				  cancelButtonColor: '#d33',
				  confirmButtonText: '加载!',
				  cancelButtonText: '取消'
				}).then((result) => {
			  if (result.value) {
				  $.ajax({
						type : 'GET',
						url : root + 'api/console/template/history/'+id,
						success:function(data) {
							 editor.setValue(data.tpl);$("#historyModal").modal('hide');
						},
						error:function(jqXHR, textStatus, errorThrown) {
							if(jqXHR.status == 404){
								Swal.fire('模板不存在','','error');return;
							}
							var data = $.parseJSON(jqXHR.responseText);
							Swal.fire('保存失败',data.error,'error');
						}
					  })
			  }
			});
	    })


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
	                    Swal.fire("获取默认模板片段失败", "", "error")
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
				Swal.fire('预览成功','预览成功，请自行访问拥有该模板片段的页面预览效果','success');
			},
			error:function(jqXHR){
				var error = $.parseJSON(jqXHR.responseText).error;
				Swal.fire('预览失败',error,'error');
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
		data.enable = $("#enable").prop("checked");
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
					Swal.fire("保存成功",'','success');
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
				}
				Swal.fire('保存失败',$.parseJSON(jqXHR.responseText).error,'error');
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
			current_tpl = editor.getValue();
			Swal.fire({
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
		Swal.fire('拷贝成功','','success');
	});
	clipboard.on('error',function(){
		Swal.fire('拷贝失败','','error');
	});
	
	function loadBak(key){
		Swal.fire({
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
					$("#fragmentKey").val(v.id);
					editor.setValue(v.content);
				}
				$("#backupModal").modal('hide');
		  }
		});
	}
	
	function delBak(key){
		Swal.fire({
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
	
	
	
	
	var timeout;
	$("#doc-search").on(
			'paste keyup',
			function() {
				if($(this).val() != ''){
					$("#doc-search-clear").show();
				}else{
					$("#doc-search-clear").hide();
				}
				var text = $.trim($(this).val());
				if (timeout) {
					clearTimeout(timeout);
				}
				timeout = setTimeout(function() {
					if (text == '') {
						$("#search-results").html('').hide();
					} else {
						querier.search(text, function(result) {
							$("#search-results").html('').hide();
							if (result.length > 0) {
								$.each(result, function(i, d) {
									$("#search-results").append(
											'<div style="padding:3px"><a href="###" onclick="loadFile(\''
													+ d + '\')">' + d
													+ '</a></div>');
								})
								$("#search-results").show();
							}
						})
					}
				}, 100)
			});

	function loadFile(d) {
		$("#doc-search").hide();
		$("#search-results").hide();
		$("#doc-search-clear").hide();
		querier.getFile(d,function(data){
			$("#doc-title").html(d);
			$("#doc-content").html(data);
			if($(window).width() <= 1024){
				$("#doc-card").css({"max-width":($(window).width()-100)+'px'})
			}else{
				$("#doc-card").css({"max-width":($(window).width()/2)+'px'})
			}
			$("#doc-card").show();
		})
	}
	
	$("#close-doc-card").click(function(){
		$("#doc-card").hide();
		$("#doc-search").show();
		if($("#doc-search").val() != ''){
			$("#doc-search-clear").show();
		}
		$("#search-results").show();
	});
	
	$("#doc-search-clear").click(function(){
		$("#search-results").html('').hide();
		$("#doc-search").val('');
		$("#doc-search-clear").hide();
	});
