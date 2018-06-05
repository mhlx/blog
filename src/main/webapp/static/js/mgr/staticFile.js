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
		});
		$("#createFileModal").on("show.bs.modal",function(){
			clearTip();
			$(this).find("form")[0].reset();
		})
		$("#copyModal").on("show.bs.modal",function(){
			clearTip();
			$(this).find("form")[0].reset();
		});
		
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
			var path = $(this).attr("data-path");
			current = path;
			switch(action){
			case "delete":
				bootbox.confirm("确定要删除吗？",function(result){
					if(result){
						$.ajax({
							type : "post",
							url : basePath+"/mgr/static/delete",
							data : {"path":path},
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
			case "move":
				$("#moveModal").modal("show");
				var currentPath = current;
				if(me.attr("data-dir") == 'false'){
					var index = currentPath.lastIndexOf('.');
					if(index > -1){
						currentPath = currentPath.substring(0,index);
					}
				}
				$("#move-path-container").html('<span class="input-group-addon">路径</span><input type="text" value="'+currentPath+'" class="form-control" name="path">');
				break;
			case "unzip":
				$("#unzipModal input[name='path']").val('');
				$("#unzipModal").modal("show");
				break;
			case "zip":
				$("#zipModal input[name='path']").val('');
				$("#zipModal").modal("show");
				break;
			case "copy":
				$("#copyModal").modal("show");
				$("#copyModal input[name='path']").val(current)
				break;
			default : 
				break;
			}
		});
		
		$("#query").click(function(){
			var form = "";
			$("#query-form").remove();
			form += '<form id="query-form" style="display:none" action="'+basePath+'/mgr/static/index" method="get">';
			var name = $.trim($("#query-name").val());
			if(name != ''){
				form += '<input type="hidden" name="name" value="'+name+'"/>';
			}
			var path = $.trim($("#query-path").val());
			if(path && path != ''){
				form += '<input type="hidden" name="path" value="'+path+'"/>';
			}
			if($("#query-sub").is(":checked")){
				form += '<input type="hidden" name="querySubDir" value="true"/>';
			}
			form += '</form>';
			$("body").append(form);
			$("#query-form").submit();
		});
		
		$("#createFolder").click(function(){
			$("#createFolder").prop("disabled",true);
			var data = $("#createFolderModal").find("form").serializeObject();
			var parent = data.parent;
			var url ;
			$.ajax({
				type : "post",
				url : basePath+"/mgr/static/createFolder",
				data : {path:$("#dir-path").val() + '/'+data.path},
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
		
		$("#createFile").click(function(){
			$("#createFile").prop("disabled",true);
			var data = $("#createFileModal").find("form").serializeObject();
			var url ;
			$.ajax({
				type : "post",
				url : basePath+"/mgr/static/createFile",
				data : {path:$("#dir-path").val() + '/'+data.path},
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
					$("#createFile").prop("disabled",false);
				}
			});
		});
		
		$("#copy").click(function(){
			$("#copy").prop("disabled",true);
			$.ajax({
				type : "post",
				url : basePath+"/mgr/static/copy?path="+current+"&destPath="+$("#copyModal input[name='path']").val(),
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
				url : basePath+"/mgr/static/move?path="+current+"&destPath="+$("#moveModal input[name='path']").val(),
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
		
		$("#unzip").click(function(){
			$("#unzip").prop("disabled",true);
			var data = $("#unzipModal").find("form").serializeObject();
			$.ajax({
				type : "post",
				url : basePath+"/mgr/static/unzip",
				data : {zipPath:current,path:data.path,deleteAfterSuccessUnzip:$("#deleteAfterSuccessUnzip").is(":checked"),encoding:data.encoding},
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
					$("#unzip").prop("disabled",false);
				}
			});
		});
		
		$("#zip").click(function(){
			$("#zip").prop("disabled",true);
			var data = $("#zipModal").find("form").serializeObject();
			$.ajax({
				type : "post",
				url : basePath+"/mgr/static/zip",
				data : {path:current,zipPath:data.path},
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
					$("#zip").prop("disabled",false);
				}
			});
		});
		
		$("#directorySelectModal").on('shown.bs.modal',function(){
			$("#directorySelectMain").html('');
			directorySelectQuery();
		});
		
	});
	var current;
	var moving = false;
	function move(path){
		if(moving){
			return ;
		} else {
			moving = true;
			$.post(basePath+"/mgr/static/move",{"path":current,"destPath":path},function callBack(data){
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
