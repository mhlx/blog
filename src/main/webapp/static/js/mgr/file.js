$(document).ready(function(){
		var successUpload = false;
		$("a[data-page]").click(function(){
			var me = $(this);
			var page = me.attr("data-page");
			$("#pageForm input[name='currentPage']").val(page);
			$("#pageForm").submit();
		})
		$("#uploadModal").on("hidden.bs.modal",function(){
			if(successUpload){
				window.location.reload();
			}
		})
		$("#uploadModal").on("show.bs.modal",function(){
			clearTip();
			$(this).find("form")[0].reset();
		})
		$("#createFolderModal").on("show.bs.modal",function(){
			clearTip();
			$(this).find("form")[0].reset();
		})
		$("#copyModal").on("show.bs.modal",function(){
			clearTip();
			$(this).find("form")[0].reset();
		})
		
		var queryType = $("#pageFormType").val();
		if(queryType != undefined){
			$("#query-type").val(queryType);
		}
		
		var querySubDir = $("#pageFormSubDir").val();
		if(querySubDir != undefined){
			$("#query-sub").prop('checked',querySubDir);
		}
		
		$('[data-toggle="tooltip"]').tooltip();
		$('#fileupload').fileupload({
			dataType : 'json',
			autoUpload : false,
			singleFileUploads : true,
			limitConcurrentUploads : 1,
			  uploadTemplate: function (o) {
			        var rows = $();
			        $.each(o.files, function (index, file) {
			            var row = $('<tr class="template-upload fade">' +
			                '<td><span class="preview"></span></td>' +
			                '<td><p class="name"></p>' +
			                '<div class="error"></div>' +
			                '</td>' +
			                '<td><p class="size"></p>' +
			                '<div class="progress"></div>' +
			                '</td>' +
			                '<td>' +
			                (!index && !o.options.autoUpload ?
			                    '  <button class="btn btn-primary start" disabled><i class="glyphicon glyphicon-upload"></i> <span>上传</span></button>' : '') +
			                    (!index ? '<button class="btn btn-warning cancel" style="margin-left:10px">  <i class="glyphicon glyphicon-ban-circle"></i> <span>取消</span></button>' : '') +
			                '</td>' +
			                '</tr>');
			            var name = file.name;
			            if(name.length > 10){
			            	name = name.substring(0,10)
			            }
			            row.find('.name').text(name);
			            row.find('.size').text(o.formatFileSize(file.size));
			            if (file.error) {
			                row.find('.error').addClass("alert alert-danger").text(file.error);
			            } 
			            rows = rows.add(row);
			        });
			        return rows;
			    },
			    downloadTemplate: function (o) {
			        var rows = $();
			        $.each(o.files, function (index, file) {
			            var row = $('<tr class="template-download fade">' +
			                '<td><span class="preview"></span></td>' +
			                '<td><p class="name"></p>' +
			                (file.error ? '<div class="error alert alert-danger"></div>' : '') +
			                '</td>' +
			                '<td><span class="size"></span></td>' +
			                '<td><button class="delete btn btn-info">完成</button></td>' +
			                '</tr>');
			            var name = file.name;
			            if(name.length > 10){
			            	name = name.substring(0,10)
			            }
			            row.find('.size').text(o.formatFileSize(file.size));
			            if (file.error) {
			                row.find('.name').text(name);
			                row.find('.error').text(file.error);
			            } else {
				            successUpload = true;
			                row.find('.name').text(name);
			                if (file.thumbnailUrl) {
			                    row.find('.preview').append(
			                        $('<a></a>').append(
			                            $('<img>').prop('src', file.thumbnailUrl.small)
			                        )
			                    );
			                }
			                row.find('button.delete')
			                    .attr('data-url', file.delete_url);
			            }
			            rows = rows.add(row);
			        });
			        return rows;
			    }
		});
		$("[data-action]").click(function(){
			var me = $(this);
			var action = $(this).attr("data-action");
			var id = $(this).attr("data-id");
			var ext = me.attr('data-ext');
			current = id;
			switch(action){
			case "property":
				$.get(basePath + "/mgr/file/"+id+"/pro",{},function(data){
					if(data.success){
						data = data.data;
						var base = data.base
						var html = '';
						var typename = base.type == "DIRECTORY" ? '文件夹' : '文件'
						html += '<p><strong>文件类型</strong>：'+typename+'</p>'
						html += '<p><strong>文件大小</strong>：'+base.size+'字节</p>';
						if(base.type == "DIRECTORY"){
							var counts = base.counts;
							if(counts.length > 0){
								for(var i=0;i<counts.length;i++){
									var count = counts[i];
									var _typename = count.type == "DIRECTORY" ? '子文件夹' : '子文件';
									html += '<p><strong>'+_typename+'个数</strong>：'+count.count+'</p>';
								}
							} else {
								html += '<p>没有任何子文件夹和子文件</p>';
							}
						} else {
							html += '<p><strong>访问路径</strong>：<a href="'+base.url+'" target="blank">'+base.url+'</a></p>';
						}
						var other = data.other;
						if(other){
							for(var k in other){
								html += '<p><strong>'+k+'</strong>：'+other[k]+'</p>';
							}
						}
						bootbox.alert(html);
					}else{
						bootbox.alert(data.message);
					}
				});
				break;
			case "delete":
				bootbox.confirm("确定要删除吗？",function(result){
					if(result){
						$.ajax({
							type : "post",
							url : basePath+"/mgr/file/delete",
							data : {"id":id},
							success : function(data){
								if(data.success){
									success(data.message);
									setTimeout(function(){
										window.location.reload();
									},500)
								} else {
									error(data.message);
								}
							},
							complete:function(){
							}
						});
					}
				})
				break;
			case "copy":
				$("#copyModal").modal("show");
				$("#copyModal input[name='id']").val(id);
				$("#copyModal input[name='path']").val('');
				break;
			case "move":
				$("#moveModal").modal("show");
				$("#moveModal input[name='id']").val(id);
				$("#move-path-container").html('<span class="input-group-addon">路径</span><input type="text" value="" class="form-control" name="path"><span class="input-group-addon" >.'+ext+'</span>');
				$("#moveModal input[name='path']").val('');
				break;
			case "rename":
				$("#rename-path-container").html('<span class="input-group-addon">名称</span><input type="text" value="" class="form-control" name="newName"><span class="input-group-addon" >.'+ext+'</span>');
				$("#renameModal").modal("show");
				$("#renameModal input[name='id']").val(id);
				$("#renameModal input[name='newName']").val('');
				break;
			default : 
				break;
			}
		});
		
		$("#query").click(function(){
			var form = "";
			$("#query-form").remove();
			form += '<form id="query-form" style="display:none" action="'+basePath+'/mgr/file/index" method="get">';
			var name = $.trim($("#query-name").val());
			if(name != ''){
				form += '<input type="hidden" name="name" value="'+name+'"/>';
			}
			if($("#query-sub").is(":checked")){
				form += '<input type="hidden" name="querySubDir" value="true"/>';
			}
			form += '<input type="hidden" name="type" value="'+$("#query-type").val()+'"/>';
			form += '</form>';
			$("body").append(form);
			$("#query-form").submit();
		});
		
		$("#createFolder").click(function(){
			$("#createFolder").prop("disabled",true);
			var data = $("#createFolderModal").find("form").serializeObject();
			var parent = data.parent;
			var url ;
			if(parent){
				url = basePath+"/mgr/file/"+parent+"/createFolder";
			}else{
				url = basePath+"/mgr/file/createFolder";
			}
			$.ajax({
				type : "post",
				url : url,
				data : {path:data.path},
				success : function(data){
					if(data.success){
						success(data.message);
						setTimeout(function(){
							window.location.reload();
						},500)
					} else {
						error(data.message);
					}
				},
				complete:function(){
					$("#createFolder").prop("disabled",false);
				}
			});
		});
		
		$("#copy").click(function(){
			$("#copy").prop("disabled",true);
			$.ajax({
				type : "post",
				url : basePath+"/mgr/file/copy?sourceId="+$("#copyModal input[name='id']").val()+"&folderPath="+$("#copyModal input[name='path']").val(),
				data : {},
				contentType : 'application/json',
				success : function(data){
					if(data.success){
						success(data.message);
						setTimeout(function(){
							window.location.reload();
						},500)
					} else {
						error(data.message);
					}
				},
				complete:function(){
					$("#copy").prop("disabled",false);
				}
			});
		});
		
		$("#move").click(function(){
			$("#move").prop("disabled",true);
			$.ajax({
				type : "post",
				url : basePath+"/mgr/file/move?sourceId="+$("#moveModal input[name='id']").val()+"&destPath="+$("#moveModal input[name='path']").val(),
				data : {},
				contentType : 'application/json',
				success : function(data){
					if(data.success){
						success(data.message);
						setTimeout(function(){
							window.location.reload();
						},500)
					} else {
						error(data.message);
					}
				},
				complete:function(){
					$("#move").prop("disabled",false);
				}
			});
		});
		
		$("#rename").click(function(){
			$("#rename").prop("disabled",true);
			$.ajax({
				type : "post",
				url : basePath+"/mgr/file/rename?sourceId="+$("#renameModal input[name='id']").val()+"&newName="+$("#renameModal input[name='newName']").val(),
				data : {},
				contentType : 'application/json',
				success : function(data){
					if(data.success){
						success(data.message);
						setTimeout(function(){
							window.location.reload();
						},500)
					} else {
						error(data.message);
					}
				},
				complete:function(){
					$("#rename").prop("disabled",false);
				}
			});
		});
		
		$("#directorySelectModal").on('shown.bs.modal',function(){
			$("#directorySelectMain").html('');
			directorySelectQuery();
		});
		
	});

	var param = {"type":"DIRECTORY"};
	
	function directorySelectQuery(){
		param = {"type":"DIRECTORY"};
		directorySelectPageQuery(1);
	}
	
	function directorySelectPageQuery(page,parent){
		param.currentPage = page;
		if(parent)
			param.parent = parent;
		$.get(basePath+'/mgr/file/query',param,function(data){
			if(data.success){
				$("#directorySelectMain").html(getRenderHtml(data.data));
				$('#directorySelectMain [data-toggle="tooltip"]').tooltip();
			} else {
				$("#directorySelectMain").html('<div class="row"><div class="col-md-12"><div class="alert alert-danger">'+data.message+'</div></div></div>');
			}
		})
	}
	var current;
	var moving = false;
	function move(id){
		if(moving){
			return ;
		} else {
			moving = true;
			$.post(basePath+"/mgr/file/move",{"src":current,"parent":id},function callBack(data){
				moving = false;
				if(data.success){
					success(data.message);
					
					setTimeout(function(){
						window.location.reload();
					},500)
				} else {
					error(data.message);
				}
			});
		}
	}
	
	function getRenderHtml(data){
		var html = '';
		var paths = data.paths;
		if(paths.length > 0){
			html += '<div class="row">';
			html += '<div class="col-md-12">';
			html += '<ol class="breadcrumb">';
			html += '<li><a href="###" onclick="directorySelectQuery()">根目录</a></li>'	;
			for(var i=0;i<paths.length;i++){
				var path = paths[i];
				html += '<li><a href="###" onclick="directorySelectPageQuery(\'1\',\''+path.id+'\')">'+path.name+'</a></li>';
			}
			html += '</ol>';
			html += '</div>';
			html += '</div>';
		}
		var page = data.page;
		var datas = page.datas;
		html += '<div class="row">';
		html += '<div class="col-md-12"><div class="tip"></div>';
		if(datas.length > 0){
			html += '<div class="row">';
			for(var i=0;i<datas.length;i++){
				var data = datas[i];
				html += '<div class="col-xs-6 col-md-4">';
				html += '<div class="thumbnail text-center">';
				html += '<a href="###" onclick="directorySelectPageQuery(\'1\',\''+data.id+'\')"><img src="'+basePath+'/static/fileicon/folder.png" class="img-responsive"/></a>';
				html += '<div class="caption">';
				var name = data.name
				if(name.length > 5){
					name = name.substring(0,5);
				}
				html += '<a title="'+data.name+'" data-toggle="tooltip">'+name+'</a>';
				html += '</div>';
				html += '<div class="caption">';
				html += '<a href="###" onclick="move(\''+data.id+'\')"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></a>';
				html += '</div>';
				html += '</div>';
				html += '</div>';
			}
			html += '</div>';
		} else {
			html += '<div class="alert alert-info">当前没有任何文件</div>'
		}
		if(page.totalPage > 1){
			html += '<div>';
			html += '<ul class="pagination">';
			for(var i=page.listbegin;i<=page.listend-1;i++){
				html += '<li>';
				html += '<a href="###" onclick="directorySelectPageQuery(\''+i+'\')">'+i+'</a>';
				html += '</li>';
			}
			html += '</ul>';
			html += '</div>';
		}
		html += '</div>';
		html += '</div>';
		return html;
	}