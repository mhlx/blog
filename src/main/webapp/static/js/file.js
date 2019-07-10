var fileChooser = (function(){
	var basePath = root;
	if(basePath == '/'){
		basePath = '';
	}
	var modal = '<div class="modal" tabindex="-1"';
	modal += 'role="dialog" aria-labelledby="fileSelectModalLabel">';
	modal += '<div class="modal-dialog modal-lg" role="document">';
	modal += '<div class="modal-content">';
	modal += '<div class="modal-header">';
	modal += '<h5 class="modal-title">文件选择</h5>';
	modal += '<button type="button" class="close" data-dismiss="modal" aria-label="Close">';
	modal += '<span aria-hidden="true">&times;</span>';
	modal += '</button>';
	modal += '</div>';
	modal += '<div class="modal-body">';
	modal += '<div class="container-fluid"></div>';
	modal += '</div>';
	modal += '<div class="modal-footer">';
	modal += '<button type="button" class="btn btn-primary" data-create-folder>新建文件夹</button>';
	modal += '<button type="button" class="btn btn-primary" data-upload>上传</button>';
	modal += '<button type="button" class="btn btn-primary" data-choose>选择</button>';
	modal += '</div>';
	modal += '</div>';
	modal += '</div>';
	modal += '</div>';
	

	var selected = [];
	var inSelected = function(id){
		for(var i=0;i<selected.length;i++){
			if(selected[i].id == id){
				return true;
			}
		}
		return false;
	}


	var fileSelectModal = $(modal);
	fileSelectModal.appendTo($('body'));
	
	var fileSelectMain = fileSelectModal.find('.container-fluid').eq(0);
	
	(function(){
		var createFolderModal = '<div class="modal fade"tabindex="-1" role="dialog" aria-labelledby="createFolderModalLabel">';
		createFolderModal += '<div class="modal-dialog" role="document">';
		createFolderModal += '<div class="modal-content">';
		createFolderModal += '<div class="modal-header">';
		createFolderModal += '<h5 class="modal-title">新文件夹</h5>';
		createFolderModal += '<button type="button" class="close" data-dismiss="modal" aria-label="Close">';
		createFolderModal += '<span aria-hidden="true">&times;</span>';
		createFolderModal += '</button>';
		createFolderModal += '</div>';
		createFolderModal += '<div class="modal-body">';
		createFolderModal += '<div class="create-tip"></div>';
		createFolderModal += '<form autocomplete="off">';
		createFolderModal += '<input type="hidden" name="parent">';
		createFolderModal += '<div class="form-group">';
		createFolderModal += '<label for="name" class="control-label">路径(1~20个字符):</label> <input type="text" class="form-control" name="path"><input type="text" class="form-control" style="display:none">';
		createFolderModal += '</div>';
		createFolderModal += '</form>';
		createFolderModal += '</div>';
		createFolderModal += '<div class="modal-footer">';
		createFolderModal += '<button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>';
		createFolderModal += '<button type="button" class="btn btn-primary" data-create>创建</button>';
		createFolderModal += '</div>';
		createFolderModal += '</div>';
		createFolderModal += '</div>';
		createFolderModal += '</div>';
		
		

		createFolderModal = $(createFolderModal);
		createFolderModal.appendTo($('body'));
		
		createFolderModal.find('[data-create]').click(function(){
			var me = $(this);
			me.prop("disabled",true);
			var data = createFolderModal.find("form").serializeObject();
			var parent = data.parent;
			if(parent){
				delete data["parent"];
				var parent = {"id":parent};
				data.parent = parent;
			}
			if(parent == ""){
				delete data["parent"];
			}
			var url = basePath + '/api/console/folder?path='+data.path
			if(data.parent){
				url += '&parent='+data.parent.id;
			}
			$.ajax({
				type : "post",
				url : url,
				success : function(data){
					createFolderModal.modal("hide");
				},
				error:function(jqXHR){
					var message = $.parseJSON(jqXHR.responseText).error;
					createFolderModal.find(".create-tip").html('<div class="alert alert-danger">'+message+'</div>')
				},
				complete:function(){
					me.prop("disabled",false);
				}
			});
		});
		
		createFolderModal.on('hidden.bs.modal',function(){
			createFolderModal.find(".create-tip").html('');
			createFolderModal.find("form")[0].reset();
			fileSelectModal.modal("show");
		});
		
		fileSelectModal.find('[data-create-folder]').click(function(){
			fileSelectModal.modal('hide');
			createFolderModal.find("input[name='parent']").val(lastParam.parent);
			createFolderModal.modal("show");
		});
		
		fileSelectModal.find('[data-choose]').click(function(){
			try{
				if(fileCallback){
					fileCallback(selected);
				} 
			}catch (e) {
				
			}
			fileSelectModal.modal('hide');
		});
	})();
	
	
	
	
	(function(){
		var stores = [];
		$.ajax({
			type : "get",
			url : basePath+"/api/console/stores",
            contentType:"application/json",
            async: false,
			data : {},
			success : function(data){
				data.unshift({id:-1,name:'自动选择'})
				stores = data;
			},
			error:function(jqXHR){
				var message = $.parseJSON(jqXHR.responseText).error;
				swal('获取文件存储器失败',message,'error')
			},
		});
		
		var uploadModal = '<div class="modal" tabindex="-1" role="dialog"';
		uploadModal += '	aria-labelledby="uploadModalLabel">';
		uploadModal += '<div class="modal-dialog modal-lg" role="document">';
		uploadModal += '<div class="modal-content">';
		uploadModal += '<div class="modal-header">';
		uploadModal += '<h5 class="modal-title">文件上传</h5>';
		uploadModal += '<button type="button" class="close" data-dismiss="modal"';
		uploadModal += '	aria-label="Close">';
		uploadModal += '<span aria-hidden="true">&times;</span>';
		uploadModal += '</button>';
		uploadModal += '</div>';
		uploadModal += '<div class="modal-body">';
		uploadModal += '<form class="form-horizontal" autocomplete="off"';
		uploadModal += 'enctype="multipart/form-data">';
		uploadModal += '<div class="col-md-12" style="margin-bottom: 20px">';
		uploadModal += '<label class="control-label">存储器：</label> <select';
		uploadModal += ' class="form-control" name="store">';
		for(var i=0;i<stores.length;i++){
			uploadModal += '<option value="'+stores[i].id+'">'+stores[i].name+'</option>';
		}
		uploadModal += '</select>';
		uploadModal += '</div>';
		uploadModal += '<div class="fileupload-buttonbar">';
		uploadModal += '<div class="col-lg-8 col-md-8 col-sm-12 col-xs-12">';
		uploadModal += '<span class="btn btn-success fileinput-button" style="margin-right:10px"> <i';
		uploadModal += ' class="glyphicon glyphicon-plus"></i> <span>添加文件</span> <input';
		uploadModal += ' type="file" name="files" multiple="">';
		uploadModal += '</span>';
		uploadModal += '<button type="submit" class="btn btn-primary start" style="margin-right:10px">';
		uploadModal += '<i class="glyphicon glyphicon-upload"></i> <span>文件上传</span>';
		uploadModal += '</button>';
		uploadModal += '<button type="reset" class="btn btn-warning cancel">';
		uploadModal += '<i class="glyphicon glyphicon-ban-circle"></i> <span>取消</span>';
		uploadModal += '</button>';
		uploadModal += '<span class="fileupload-process"></span>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '<div style="margin-top: 10px">';
		uploadModal += '<div';
		uploadModal += ' class="fileupload-progress fade col-lg-12 col-sm-12 col-md-12 col-xs-12">';
		uploadModal += '<div class="progress progress-striped active"';
		uploadModal += ' role="progressbar" aria-valuemin="0" aria-valuemax="100">';
		uploadModal += '<div class="progress-bar progress-bar-success"';
		uploadModal += ' style="width: 0%;"></div>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '<div class="col-lg-12 col-sm-12 col-md-12 col-xs-12">';
		uploadModal += '<div class="table-responsive">';
		uploadModal += '<table role="presentation" class="table table-striped"';
		uploadModal += ' style="text-align: center">';
		uploadModal += '<tbody class="files"></tbody>';
		uploadModal += '</table>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '<input type="hidden" name="parent"/>';
		uploadModal += '</form>';
		uploadModal += '</div>';
		uploadModal += '<div class="modal-footer">';
		uploadModal += '<button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		
		uploadModal = $(uploadModal);
		uploadModal.appendTo($("body"));
		
		uploadModal.on('hidden.bs.modal',function(){
			uploadModal.find("form")[0].reset();
			uploadModal.find(".files").html("");
			fileSelectModal.modal("show");
		});
		uploadModal.find("form").eq(0).fileupload({
			dataType : 'json',
			autoUpload : false,
			singleFileUploads : false,
			limitConcurrentUploads : 1,
			uploadTemplate: function (o) {
		        var rows = $();
		        $.each(o.files, function (index, file) {
		        	 var row = $('<tr class="template-upload" style="max-height:80px">' +
				                '<td style="white-space: nowrap!important"><span class="preview" ></span></td>' +
				                '<td style="white-space: nowrap!important"><p class="name"></p>' +
				                '<div class="error"></div>' +
				                '</td>' +
				                '<td style="white-space: nowrap!important"><p class="size"></p>' +
				                '<div class="progress"><div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div></div>' +
				                '</td>' +
				                '<td style="white-space: nowrap!important">' +
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
			                '<td style="white-space: nowrap!important"><span class="preview"></span></td>' +
			                '<td style="white-space: nowrap!important"><p class="name"></p>' +
			                (file.error ? '<div class="error alert alert-danger"></div>' : '') +
			                '</td>' +
			                '<td style="white-space: nowrap!important"><span class="size"></span></td>' +
			                '<td style="white-space: nowrap!important"><button class="delete btn btn-info">完成</button></td>' +
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
		
		uploadModal.find("form").attr('action',root+'api/console/store/'+uploadModal.find('select[name="store"]').val()+'/files')
		uploadModal.find('select[name="store"]').change(function(){
			var v = $(this).val();
			var url = root+'api/console/store/'+v+'/files';
			uploadModal.find("form").attr('action',url)
		})
		
		fileSelectModal.find('[data-upload]').click(function(){
			fileSelectModal.modal('hide');
			uploadModal.find("input[name='parent']").val(lastParam.parent);
			uploadModal.modal("show");
		});
	})();
	
	fileSelectModal.on('hidden.bs.modal',function(){
		fileSelectMain.html('');
		selected = [];
//		hasSelected = false;
	});
	
	fileSelectModal.on('click','a[data-parent][data-page]',function(){
		var page = $(this).attr("data-page");
		var parent = $(this).attr("data-parent");
		fileSelectPageQuery(page, parent);
	});
	
	var lastParam = {currentPage : 1};
	var datas = [];
	var fileSelectPageQuery = function(page,parent){
		var param = {};
		param.currentPage = page;
		if(parent && parent != "")
			param.parent = parent;
		fileSelectMain.html("<img src='"+basePath+"/static/img/loading.gif' class='img-fluid mx-auto d-block' />")
		$.ajax({
			type : "get",
			url : basePath+"/api/console/files",
            contentType:"application/json",
            async: false,
			data : param,
			success : function(data){
				lastParam = param;
				fileSelectMain.html(getRenderHtml(data));
				fileSelectMain.find('[data-toggle="tooltip"]').tooltip();
			},
			error:function(jqXHR){
				var message = $.parseJSON(jqXHR.responseText).error;
				fileSelectMain.html('<div class="row"><div class="col-md-12"><div class="alert alert-danger">'+message+'</div></div></div>');
			},
		});
		
	}
	
	var getRenderHtml = function(data){
		var html = '';
		var paths = data.paths;
		if(paths.length > 0){
			html += '<div class="row">';
			html += '<div class="col-md-12">';
			html += '<ol class="breadcrumb">';
			html += '<li class="breadcrumb-item"><a href="###"  data-parent="" data-page="1" >根目录</a></li>'	;
			for(var i=0;i<paths.length;i++){
				var path = paths[i];
				html += '<li class="breadcrumb-item"><a href="###" data-parent="'+path.id+'" data-page="1" >'+path.path+'</a></li>';
			}
			html += '</ol>';
			html += '</div>';
			html += '</div>';
		}
		var page = data.page;
		datas = page.datas;
		html += '<div class="row">';
		html += '<div class="col-md-12"><div class="tip"></div>';
		if(datas.length > 0){
			html += '<div class="row">';
			for(var i=0;i<datas.length;i++){
				var data = datas[i];
				html += '<div class="col-6 col-md-4">';
				var isInSelected = inSelected(data.id);
				var style= isInSelected ? 'border:1px solid red;margin-bottom:10px;' : 'margin-bottom:10px;';
				html += '<div class="img-thumbnail text-center" style="'+style+'">';
				if(data.type == 'DIRECTORY'){
					html += '<a href="###" data-page="1" data-parent="'+data.id+'" "><img src="'+basePath+'/static/img/folder.png" class="img-fluid" style="height:100px"/></a>';
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
						html += '<img  src="'+data.cf.thumbnailUrl.small+'" data-middle="'+data.cf.thumbnailUrl.middle+'" class="img-fluid" style="height:100px"/>';
					} else {
						html += '<img src="'+basePath+'/static/img/file.png" class="img-fluid" style="height:100px"/>';
					}
					html += '</a>';
				}
				html += '<div class="caption" style="height:35px">';
				var name = data.path
				if(name.length > 10){
					name = name.substring(0,10)+"...";
				}
				html += '<a title="'+data.path+'" data-toggle="tooltip">'+name+'</a>';
				html += '</div>';
				html += '</div>';
				html += '</div>';
			}
			html += '</div>';
		} else {
			html += '<div class="alert alert-info">当前没有任何文件</div>'
		}
		if(page.totalPage > 1){
			html += '<nav>';
			html += '<ul class="pagination flex-wrap">';
			for(var i=page.listbegin;i<=page.listend-1;i++){
				html += '<li class="page-item">';
				html += '<a href="###" class="page-link" data-page="'+i+'" data-parent="'+(lastParam.parent ? lastParam.parent : "")+'" >'+i+'</a>';
				html += '</li>';
			}
			html += '</ul>';
			html += '</nav>';
		}
		html += '</div>';
		html += '</div>';
		return html;
	}
	var fileCallback;
	fileSelectModal.on("click","[data-file]",function(){
		var me = $(this);
		var id = me.data('file');
		for(var i=0;i<selected.length;i++){
			if(selected[i].id == id){
				selected.splice(i,1);
				me.parent().css({"border":"1px solid #ddd"});
//				if(selected.length == 0){
//					fileSelectedMain.html('');
//					hasSelected = false;
//				}else{
//					fileSelectedMain.find('[data-selected="'+id+'"]').remove();
//				}
				return; 
			}
		}
		for(var i=0;i<datas.length;i++){
			var data = datas[i];
			if(data.id == id){
				selected.push(data);
				var html = '';
//				if(!hasSelected){
//					html += '<div class="table-responsive">';
//					html += '<table class="table">';
//					html += '<tr><th>文件名</th></tr>';
//					html += '<tr data-selected="'+id+'"><td>'+data.path+'</td></tr>';
//					html += '</table>';
//					html += '</div>';
//					fileSelectedMain.html(html);
//				} else {
//					fileSelectedMain.find('table').append('<tr data-selected="'+id+'"><td>'+data.path+'</td></tr>');
//				}
//				hasSelected = true;
				me.parent().css({"border":"1px solid red"})
				break;
			}
		}
	}); 
	
	fileSelectModal.on("show.bs.modal",function(){
		fileSelectPageQuery(lastParam.currentPage,lastParam.parent);
	});
	
	return {
		choose:function(callback){
			fileCallback = callback;
			fileSelectModal.modal('show');
		}
	};
})();