var parents = [];
var table = datatable(
		'fileTable',
		{
			paging : true,
			url : function() {
				return root + 'api/console/files';
			},

			dataConverter : function(data) {
				parents = data.paths;
				if (parents.length > 0) {
					$("[data-prev]").show();
				} else {
					$("[data-prev]").hide();
				}
				return data.page;
			},

			columns : [
					{
						bind : 'id',
						classes : 'nowrap',
						render : function(v, row) {
							var html = '';
							html += '<a href="###" data-delete="'+v+'"><i class="fas fa-trash-alt"></i></a>';
							html += '<a href="###" data-properties="'+v+'" style="margin-left:10px"><i class="fas fa-info"></i></a>'
							if (row.type == 'DIRECTORY') {
							} else {
								html += '<a href="###" data-copy="'+v+'" style="margin-left:10px"><i class="fas fa-copy"></i></a>';
								html += '<a href="###" data-rename="'+v+'" data-oldname="'+row.path+'" data-ext="'+row.ext+'" style="margin-left:10px"><i class="fas fa-edit"></i></a>';
								html += '<a href="###" data-move="'+v+'" style="margin-left:10px"><i class="fas fa-arrows-alt"></i></a>';
							}
							return html;
						}
					},
					{
						bind : 'xx3',
						render : function(v, row) {
							return row.path;
						}
					},
					{
						bind : 'xx2',
						render : function(v, row) {
							if (row.type == 'DIRECTORY') {
								return '';
							} else {
								return humanFileSize(row.cf.size, false);
							}
						}
					},
					{
						bind : 'xx1',
						render : function(v, row) {
							if (row.type == 'DIRECTORY') {
								var path = '';
								for (var i = 0; i < parents.length; i++) {
									var p = parents[i];
									path += p.path;
									path += '/';
								}
								path += row.path;
								return '<a href="###" data-clipboard-text="'
										+ path + '">复制路径</a>';
							} else {
								return '<a href="###" data-clipboard-text="'
										+ row.cf.url + '">复制地址</a>';
							}
						}
					},
					{
						bind : 'path',
						render : function(v, d) {
							var html = '';
							if (d.cf) {
								if (d.cf.thumbnailUrl) {
									html = '<img src="'
											+ d.cf.thumbnailUrl.small
											+ '" class="img-fluid " style="max-width:100px"/>';
								} else {
									html = '<img src="'
											+ root
											+ 'static/img/file.png" class="img-fluid " style="max-width:100px"/>'
								}
							} else {
								html = '<a href="###" data-dir="'
										+ d.id
										+ '"><img src="'
										+ root
										+ 'static/img/folder.png" class="img-fluid " style="max-width:100px"/></a>'
							}
							return html;
						}
					} ]

		});

var parentFolder;
$(function() {
	$("#query").click(function () {
		var data = {};
		if(parentFolder){
			data.parent = parentFolder;
		}
		data.type = $('input[name=queryType]:checked').val();
		data.querySubDir = $("#querySubDir").is(":checked");
		data.name = $("#queryName").val();
		data.currentPage=1;
		table.reload(data);
	} );
	
	$("#fileTable").on("click","[data-dir]",function(){
		parentFolder = $(this).data('dir');
		table.reload({parent:parentFolder})
	});
	
	$("#fileTable").on("click","[data-delete]",function(){
		var id = $(this).data('delete');
		swal({
		  title: '你确定吗？',
		  text: "这个操作无法被撤销",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonColor: '#3085d6',
		  cancelButtonColor: '#d33',
		  confirmButtonText: '删除!',
		  cancelButtonText: '取消'
		}).then((result) => {
		  if (result.value) {
			  $.ajax({
				type : 'DELETE',
				url : root + 'api/console/file/'+id,
				success:function(data) {
					table.reload();
					swal('删除成功','文件已经被删除','success');
				},
				error:function(jqXHR, textStatus, errorThrown) {
					var data = $.parseJSON(jqXHR.responseText);
					swal('删除失败',data.error,'error');
				}
			  })
		  }
		});
	});
	
	$("#fileTable").on("click","[data-properties]",function(){
		var id = $(this).data('properties');
		$.ajax({
			url : root + 'api/console/file/'+id+'/properties',
			success:function(data) {
				var html = '<div class="table-responsive">';
				html += "<table class='table'>";
				if(data.base.type == 'DIRECTORY'){
					html += "<tr><th>文件类型</th><td>文件夹</td></tr>";
					html += "<tr><th>文件大小</th><td>"+humanFileSize(data.base.size,false)+"</td></tr>";
					var counts = data.base.counts;
					if(counts.length > 0){
						for(var i=0;i<counts.length;i++){
							var count = counts[i];
							if(count.type == 'DIRECTORY'){
								html += "<tr><th>子文件夹数目</th><td>"+count.count+"</td></tr>";
							} else {
								html += "<tr><th>子文件数目</th><td>"+count.count+"</td></tr>";
							}
						}
					}
				}else{
					html += "<tr><th>文件类型</th><td>文件</td></tr>";
					html += "<tr><th>文件大小</th><td>"+humanFileSize(data.base.size,false)+"</td></tr>";
					html += "<tr><th>文件访问地址</th><td>"+data.base.url+"</td></tr>";
					if(data.other){
						Object.keys(data.other).forEach(function(key,index) {
							html += "<tr><th>"+key+"</th><td>"+data.other[key]+"</td></tr>";
						});
					}
				}
				html += "</table>";
				html += '</div>';
				swal({
				  title: '文件属性',
				  type: 'info',
				  html:html,
				  showCancelButton: false,
				  showConfirmButton: false,
				  showCloseButton: true
				})
			},
			error:function(jqXHR, textStatus, errorThrown) {
				var data = $.parseJSON(jqXHR.responseText);
				swal('获取文件属性失败',data.error,'error');
			}
		  })
	});
	
	$("#fileTable").on("click","[data-copy]",function(){
		var id = $(this).data('copy');
		(async function getPath () {
			const {value: path} = await swal({
			  title: '拷贝文件',
			  input: 'text',
			  inputValue: '',
			  inputPlaceholder:'请输入目标文件夹路径',
			  showCancelButton: true,
			  confirmButtonText:'确定',
			  cancelButtonText:'取消'
			})

			if (path) {
				$.ajax({
					type : 'POST',
					url : root + 'api/console/file?folderPath='+path+"&id="+id,
					success:function(data) {
						table.reload();
						swal('拷贝成功','文件已经拷贝完成','success');
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						swal('拷贝失败',data.error,'error');
					}
			  	})
			}

		})()
	});
	
	$("#fileTable").on("click","[data-rename]",function(){
		var id = $(this).data('rename');
		var oldname = $(this).data('oldname').split('.').slice(0, -1).join('.');
		(async function getName () {
			const {value:name} = await swal({
			  title: '重命名文件',
			  input: 'text',
			  inputValue: oldname,
			  inputPlaceholder:'请输入新文件名，不包含后缀',
			  showCancelButton: true,
			  confirmButtonText:'确定',
			  cancelButtonText:'取消'
			})

			if (name) {
				$.ajax({
					type : 'PATCH',
					url : root + 'api/console/file/'+id+"?name="+name,
					success:function(data) {
						table.reload();
						swal('重命名成功','文件已经完成重命名','success');
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						swal('重命名失败',data.error,'error');
					}
			  	})
			}

		})()
	});
	
	$("#fileTable").on("click","[data-move]",function(){
		var id = $(this).data('move');
		(async function getPath () {
			const {value: path} = await swal({
			  title: '移动文件',
			  input: 'text',
			  inputValue: '',
			  inputPlaceholder:'请输入目标文件夹路径',
			  showCancelButton: true,
			  confirmButtonText:'确定',
			  cancelButtonText:'取消'
			})

			if (path) {
				$.ajax({
					type : 'PATCH',
					url : root + 'api/console/file/'+id+"?path="+path,
					success:function(data) {
						table.reload();
						swal('移动成功','文件已经移动完成','success');
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						swal('移动失败',data.error,'error');
					}
			  	})
			}

		})()
	});
	
	$("[data-create-folder]").click(function() {
		$("#folderModal").modal("show");
	});
	
	$("[data-upload]").click(function() {
		$("#uploadModal").modal("show");
	});
	
	$.ajax({
		url : root + 'api/console/stores',
		success:function(data){
			for(var i=0;i<data.length;i++){
				$("#store").append('<option value="'+data[i].id+'">'+data[i].name+'</option>');
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			var data = $.parseJSON(jqXHR.responseText);
			swal('查询存储服务器失败', data.error, 'error');
		}
	});
	
	$("[data-prev]").click(function() {
		if(parents.length == 0 || parents.length == 1){
			parentFolder = undefined;	
		} else {
			var p = parents[parents.length-2];
			parentFolder = p.id;
		}
		table.reload({parent:parentFolder});
	});

	$("#create-folder").click(function() {
		var data = {
			"path" : $("#folderName").val()
		};
		if (parent) {
			data.parent = parentFolder;
		}
		var me = $(this);
		$.ajax({
			type : "POST",
			data : data,
			url : root + 'api/console/folder',
			complete : function(jqXHR, textStatus, errorThrown) {
				var code = jqXHR.status;
				if (code == 201) {
					me.prop("disabled", false);
					$("#folderModal").modal("hide");
					swal('创建成功', '文件夹已经被创建', 'success');
					table.reload();
				} else {
					me.prop("disabled", false);
					var data = $.parseJSON(jqXHR.responseText);
					swal('创建失败', data.error, 'error');
				}
			}
		});
	});
	
	$('#fileupload').fileupload({
		dataType : 'json',
		autoUpload : false,
		singleFileUploads : true,
		limitConcurrentUploads : 1,
		  uploadTemplate: function (o) {
		        var rows = $();
		        $.each(o.files, function (index, file) {
		            var row = $('<tr class="template-upload" style="max-height:80px">' +
		                '<td><span class="preview" ></span></td>' +
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
		            var row = $('<tr class="template-download">' +
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
	}).bind('fileuploadadd', function (e, data) {
		var url = root+'api/console/store/'+$("#store").val()+'/files';
		if(parentFolder){
			url += "?parent="+parentFolder;
		}
	    data.url = url;
	});
	
	$("#uploadModal").on('hide.bs.modal',function(){
		table.reload();
	});
	var clipboard=new Clipboard('[data-clipboard-text]');
	clipboard.on('success',function(e){
		swal('拷贝成功','','success'); e.clearSelection();
	});
	clipboard.on('error',function(){
		swal('拷贝失败','','error');
	});

});	