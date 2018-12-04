var parents = [];

var param = {currentPage:1};
var loadFiles = function(){
	$.ajax({
		url : root + 'api/console/files',
		data : param,
		success:function(data){
			var paths = data.paths;
			parents = paths;
			var page = data.page;
			var datas = page.datas;
			var html = '';
			if(paths.length > 0){
				html += '<nav aria-label="breadcrumb">';
				html += '<ol class="breadcrumb">';
				html += '<li class="breadcrumb-item" data-parent=""><a href="#">根目录</a></li>';
				for(var i=0;i<paths.length;i++){
					html += '<li class="breadcrumb-item"><a href="#" data-parent="'+paths[i].id+'">'+paths[i].path+'</a></li>';
				}    
			    html += '</ol>';
			    html += '</nav>';
			}
			$("#fileTable_breadcrumb").html(html);
			html = '';
			for(var i=0;i<datas.length;i++){
				html += '<div class="col-md-3 col-6" style="margin-bottom:10px">'
				html += '<div class="card h-100" >';
				html += '<div class="card-body wrap text-center" >';
				var data = datas[i];
				var icons = '';
				icons += '<a href="###" data-delete="'+data.id+'"><i style="font-size:20px" class="fas fa-trash-alt fa-fw"></i></a>';
				icons += '<a href="###" data-properties="'+data.id+'" ><i style="font-size:20px" class="fas fa-fw fa-info"></i></a>';
				var addPath = true;
				if(data.type == 'DIRECTORY'){
					html += '<a href="###" data-page="1" data-parent="'+data.id+'" "><img src="'+root+'static/img/folder.png" class="img-fluid " style="height:100px"/></a>';
				} else {
					var url =data.cf.url;
					
					var video = data.cf.extension.toUpperCase() == 'MP4' || data.cf.extension.toUpperCase() == 'MOV';
					if(video){
						html += '<a href="###" data-file="'+data.id+'" style="position: relative;display: inline-block;">';
					}else{
						html += '<a href="###" data-file="'+data.id+'">';
					}
					if(video){
						html += '<i class="fas fa-video " style="position: absolute;left: 50%;top: 50%;font-size:2rem;transform: translate(-50%, -50%);color:grey;"></i>';
					}
					if(data.cf.thumbnailUrl){
						addPath = false;
						html += '<img  src="'+data.cf.thumbnailUrl.small+'" data-middle="'+data.cf.thumbnailUrl.middle+'" class="img-fluid img-thumbnail" style="height:100px"/>';
					} else {
						html += '<img src="'+root+'static/img/file.png" class="img-fluid" style="height:100px"/>';
					}
					html += '</a>';
					icons += '<a href="###" data-copy="'+data.id+'"><i style="font-size:20px" class="fas fa-copy fa-fw"></i></a>';
					icons += '<a href="###" data-rename="'+data.id+'" data-oldname="'+data.path+'" data-ext="'+data.ext+'" ><i style="font-size:20px" class="fas fa-edit fa-fw"></i></a>';
					icons += '<a href="###" data-move="'+data.id+'" ><i style="font-size:20px" class="fas fa-arrows-alt fa-fw"></i></a>';
				}
				if (addPath) {
					html += '<p class="text-center">'+data.path+'</p>';
				}
				html += '<p style="padding:5px">'+icons+'</p>';
				if (data.type == 'DIRECTORY') {
					var path = '';
					for (var j = 0; j < parents.length; j++) {
						var p = parents[j];
						path += p.path;
						path += '/';
					}
					path += data.path;
					html += '<p><a href="###" data-clipboard-text="'
							+ path + '">复制路径</a></p>';
				} else {
					html += '<p><a href="###" data-clipboard-text="'
							+ data.cf.url + '">复制地址</a></p>';
				}
				html += '</div>';
				html += '</div>';
				html += '</div>';
			}
			$("#fileTable").html(html);
			var p = $("#fileTable_paging");
			html = '';
			if (page.totalPage > 1) {
				html += '<nav >';
				html += '<ul  class="pagination flex-wrap">';
				html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1"><span aria-hidden="true">&laquo;</span></a></li>';
				for (var j = page.listbegin; j < page.listend; j++) {
					if (j == page.currentPage) {
						html += '<li class="page-item active"><a class="page-link" href="javascript:void(0)" >'
								+ j + '</a></li>';
					} else {
						html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
								+ j + '">' + j + '</a></li>';
					}
				}
				html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
						+ page.totalPage
						+ '"><span aria-hidden="true">&raquo;</span></a></a></li>';
				html += '</ul>';
				html += '</nav>';
			}
			p.html(html);
		},
		error : function(jqXHR){
			swal('查询文件失败',$.parseJSON(jqXHR.responseText).error,'error');
		}
	})
}
	
loadFiles();

$(document).on('click','[data-parent]',function(){
	var p = $(this).data('parent');
	param.parent = p;
	param.currentPage=1;
	loadFiles();
})
$(document).on('click','[data-page]',function(){
	var p = $(this).data('page');
	param.currentPage=p;
	loadFiles();
})
var parentFolder;
$(function() {
	$("#query").click(function () {
		param.type = $('input[name=queryType]:checked').val();
		param.querySubDir = $("#querySubDir").is(":checked");
		param.name = $("#queryName").val();
		param.currentPage=1;
		loadFiles();
	} );
	
	
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
					loadFiles();
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
						loadFiles();
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
						loadFiles();
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
						loadFiles();
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
			$("#fileupload").attr('action',root+'api/console/store/'+$("#store").val()+'/files')
		},
		error : function(jqXHR, textStatus, errorThrown) {
			var data = $.parseJSON(jqXHR.responseText);
			swal('查询存储服务器失败', data.error, 'error');
		}
	});
	
	$("#create-folder").click(function() {
		var data = {
			"path" : $("#folderName").val()
		};
		if(parents.length>0){
			data.parent = parents[parents.length-1].id;
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
					loadFiles();
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
		                '<td class="nowrap"><span class="preview" ></span></td>' +
		                '<td class="nowrap"><p class="name"></p>' +
		                '<div class="error"></div>' +
		                '</td>' +
		                '<td class="nowrap"><p class="size"></p>' +
		                '<div class="progress"><div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div></div>' +
		                '</td>' +
		                '<td class="nowrap">' +
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
		                '<td class="nowrap"><span class="preview"></span></td>' +
		                '<td class="nowrap"><p class="name"></p>' +
		                (file.error ? '<div class="error alert alert-danger"></div>' : '') +
		                '</td>' +
		                '<td class="nowrap"><span class="size"></span></td>' +
		                '<td class="nowrap"><button class="delete btn btn-info">完成</button></td>' +
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
		if(parents.length>0){
			url += "?parent="+parents[parents.length-1].id;
		}
	    data.url = url;
	});
	
	
	$("#store").change(function(){
		var v = $(this).val();
		var url = root+'api/console/store/'+v+'/files';
		if(parents.length>0){
			url += "?parent="+parents[parents.length-1].id;
		}
		$("#fileupload").attr('action',url)
	})
	
	$("#uploadModal").on('hide.bs.modal',function(){
		loadFiles();
	});
	var clipboard=new Clipboard('[data-clipboard-text]');
	clipboard.on('success',function(e){
		swal('拷贝成功','','success'); e.clearSelection();
	});
	clipboard.on('error',function(){
		swal('拷贝失败','','error');
	});

});	