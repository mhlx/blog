var modal = '<div class="modal" id="fileSelectModal" tabindex="-1"';
	modal += 'role="dialog" aria-labelledby="fileSelectModalLabel">';
	modal += '<div class="modal-dialog modal-lg" role="document">';
	modal += '<div class="modal-content">';
	modal += '<div class="modal-header">';
	modal += '<button type="button" class="close" data-dismiss="modal"';
	modal += 'aria-label="Close">';
	modal += '<span aria-hidden="true">&times;</span>';
	modal += '</button>';
	modal += '<h4 class="modal-title" id="fileSelectModalLabel">文件选择</h4>';
	modal += '</div>';
	modal += '<div class="modal-body">';
	modal += '<div class="container-fluid" id="fileSelectMain"></div>';
	modal += '</div>';
	modal += '<div class="modal-footer">';
	modal += '<button type="button" class="btn btn-default" id="create-folder">新建文件夹</button>';
	modal += '<button type="button" class="btn btn-default" id="file-upload">上传</button>';
	modal += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
	modal += '</div>';
	modal += '</div>';
	modal += '</div>';
	modal += '</div>';
	
	var imageModal = '<div class="modal " id="imageModal" tabindex="-1"';
	imageModal += 'role="dialog">';
	imageModal += '<div class="modal-dialog modal-lg" role="document">';
	imageModal += '<div class="modal-content">';
	imageModal += '<div class="modal-header">';
	imageModal += '<button type="button" class="close" data-dismiss="modal"';
	imageModal += 'aria-label="Close">';
	imageModal += '<span aria-hidden="true">&times;</span>';
	imageModal += '</button>';
	imageModal += '<h4 class="modal-title"></h4>';
	imageModal += '</div>';
	imageModal += '<div class="modal-body">';
	imageModal += '<div class="container-fluid">';
	imageModal += '<div class="form-group">';
	imageModal += '<label >图片地址</label> <input  class="form-control" placeholder="图片地址" >';
	imageModal += '</div>';
	imageModal += '<div class="form-group">';
	imageModal += '<label >图片描述</label> <input  class="form-control" placeholder="图片描述" >';
	imageModal += '</div>';
	imageModal += '<div class="form-group">';
	imageModal += '<label >图片链接</label> <input class="form-control" placeholder="图片链接" >';
	imageModal += '</div>';
	imageModal += '</div>';
	imageModal += '<div class="modal-footer">';
	imageModal += '<button type="button" class="btn btn-default" id="image-confirm-btn">确定</button>';
	imageModal += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
	imageModal += '</div>';
	imageModal += '</div>';
	imageModal += '</div>';
	imageModal += '</div>';
	
	
	var linkModal = '<div class="modal " id="linkModal" tabindex="-1"';
	linkModal += 'role="dialog">';
	linkModal += '<div class="modal-dialog modal-lg" role="document">';
	linkModal += '<div class="modal-content">';
	linkModal += '<div class="modal-header">';
	linkModal += '<button type="button" class="close" data-dismiss="modal"';
	linkModal += 'aria-label="Close">';
	linkModal += '<span aria-hidden="true">&times;</span>';
	linkModal += '</button>';
	linkModal += '<h4 class="modal-title"></h4>';
	linkModal += '</div>';
	linkModal += '<div class="modal-body">';
	linkModal += '<div class="container-fluid">';
	linkModal += '<div class="form-group">';
	linkModal += '<label >链接地址</label> <input  class="form-control" placeholder="链接地址" >';
	linkModal += '</div>';
	linkModal += '<div class="form-group">';
	linkModal += '<label >链接标题</label> <input  class="form-control" placeholder="链接标题" >';
	linkModal += '</div>';
	linkModal += '</div>';
	linkModal += '<div class="modal-footer">';
	linkModal += '<button type="button" class="btn btn-default" id="link-confirm-btn">确定</button>';
	linkModal += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
	linkModal += '</div>';
	linkModal += '</div>';
	linkModal += '</div>';
	linkModal += '</div>';
	
	var createFolderModal = '<div class="modal fade" id="createFolderModal" tabindex="-1" role="dialog" aria-labelledby="createFolderModalLabel">';
	createFolderModal += '<div class="modal-dialog" role="document">';
	createFolderModal += '<div class="modal-content">';
	createFolderModal += '<div class="modal-header">';
	createFolderModal += '<button type="button" class="close" data-dismiss="modal" aria-label="Close">';
	createFolderModal += '<span aria-hidden="true">&times;</span>';
	createFolderModal += '</button>';
	createFolderModal += '<h4 class="modal-title" id="createFolderModalLabel">新文件夹</h4>';
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
	createFolderModal += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
	createFolderModal += '<button type="button" class="btn btn-primary" id="createFolder">创建</button>';
	createFolderModal += '</div>';
	createFolderModal += '</div>';
	createFolderModal += '</div>';
	createFolderModal += '</div>';
	
	if($("#createFolderModal").length == 0){
		$(createFolderModal).appendTo($("body"));
		
		$("#createFolder").click(function(){
			$("#createFolder").prop("disabled",true);
			var data = $("#createFolderModal").find("form").serializeObject();
			var parent = data.parent;
			if(parent){
				delete data["parent"];
				var parent = {"id":parent};
				data.parent = parent;
			}
			if(parent == ""){
				delete data["parent"];
			}
			var url ;
			if(data.parent){
				url = basePath + '/mgr/file/'+data.parent.id+'/createFolder';
			}else{
				url = basePath + '/mgr/file/createFolder'
			}
			$.ajax({
				type : "post",
				url : url + "?path="+data.path,
				success : function(data){
					if(data.success){
						$("#createFolderModal").modal("hide");
					} else {
						$("#createFolderModal").find(".create-tip").html('<div class="alert alert-danger">'+data.message+'</div>');
					}
				},
				complete:function(){
					$("#createFolder").prop("disabled",false);
				}
			});
		});
		
		$("#createFolderModal").on('hidden.bs.modal',function(){
			$("#createFolderModal").find(".create-tip").html('');
			$("#createFolderModal").find("form")[0].reset();
			fileSelectPageQuery(lastParam.currentPage,lastParam.parent);
			$("#fileSelectModal").modal("show");
		});
		
	}
	
	
	if($("#uploadModal").length == 0){
		var stores = [];
		$.ajax({
			type : "get",
			url : basePath+"/mgr/file/stores",
            contentType:"application/json",
            async: false,
			data : {},
			success : function(data){
				stores = data;
			},
			complete:function(){
			}
		});
		
		var uploadModal = '<div class="modal" id="uploadModal" tabindex="-1" role="dialog"';
		uploadModal += '	aria-labelledby="uploadModalLabel">';
		uploadModal += '<div class="modal-dialog modal-lg" role="document">';
		uploadModal += '<div class="modal-content">';
		uploadModal += '<div class="modal-header">';
		uploadModal += '<button type="button" class="close" data-dismiss="modal"';
		uploadModal += '	aria-label="Close">';
		uploadModal += '<span aria-hidden="true">&times;</span>';
		uploadModal += '</button>';
		uploadModal += '<h4 class="modal-title" id="uploadModalLabel">文件上传</h4>';
		uploadModal += '</div>';
		uploadModal += '<div class="modal-body">';
		uploadModal += '<div class="container-fluid">';
		uploadModal += '<div class="row" style="padding: 5px">';
		uploadModal += '<form id="fileupload" class="form-horizontal" autocomplete="off"';
		uploadModal += ' action="'+basePath+'/mgr/file/upload" method="POST"';
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
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '<div class="modal-footer">';
		uploadModal += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		uploadModal += '</div>';
		
		$(uploadModal).appendTo($("body"));
		
		$("#uploadModal").on('hidden.bs.modal',function(){
			$("#uploadModal").find("form")[0].reset();
			$("#uploadModal .files").html("");
			fileSelectPageQuery(lastParam.currentPage,lastParam.parent);
			$("#fileSelectModal").modal("show");
		});
		
		$('#fileupload').fileupload({
			dataType : 'json',
			autoUpload : false,
			singleFileUploads : false,
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
	}
	
	if($("#fileSelectModal").length == 0){
		$(modal).appendTo($("body"));
		
		$("#file-upload").click(function(){
			$("#fileSelectModal").modal('hide');
			$("#uploadModal input[name='parent']").val(lastParam.parent);
			$("#uploadModal").modal("show");
		});
		
		$("#create-folder").click(function(){
			$("#fileSelectModal").modal('hide');
			$("#createFolderModal input[name='parent']").val(lastParam.parent);
			$("#createFolderModal").modal("show");
		});
		$("#fileSelectModal").on('hidden.bs.modal',function(){
			$("#fileSelectMain").html('');
		});
		$("#fileSelectModal").on('click','a[data-parent][data-page]',function(){
			var page = $(this).attr("data-page");
			var parent = $(this).attr("data-parent");
			fileSelectPageQuery(page, parent);
		})
		
		$("#fileSelectModal").on('click','a[data-extension][data-url][data-description]',function(){
			$("#fileSelectModal").modal('hide');
			var me = $(this);
			var ext = me.attr("data-extension");
			if(isImage(ext)){
				$("#imageModal").find("input").eq(0).val(me.find('img').attr("data-middle"));
				$("#imageModal").find("input").eq(1).val(me.attr("data-description"));
				$("#imageModal").find("input").eq(2).val(me.attr("data-url"));
				$("#imageModal").modal("show")
			} else {
				$("#linkModal").find("input").eq(0).val(me.attr("data-url"));
				$("#linkModal").find("input").eq(1).val(me.attr("data-description"));
				$("#linkModal").modal("show")
			}
		});
		
		
		if($("#imageModal").length == 0){
			$(imageModal).appendTo($('body'));
			$("#imageModal").on('hidden.bs.modal',function(){
				fileSelectPageQuery(lastParam.currentPage,lastParam.parent);
				$("#fileSelectModal").modal("show");
			});
			
			
			$("#image-confirm-btn").click(function(){
				  var url  = $("#imageModal").find("input").eq(0).val();
                var alt  = $("#imageModal").find("input").eq(1).val();
                var link = $("#imageModal").find("input").eq(2).val();

                if (url === "")
                {
              	  $("#imageModal").modal("hide");
              	  return ;
                }

					var altAttr = (alt !== "") ? " \"" + alt + "\"" : "";

                if (link === "" || link === "http://" || link === "https://")
                {
                	editor.replaceSelection("![" + alt + "](" + url + altAttr + ")")
                }
                else
                {
                    editor.replaceSelection("[![" + alt + "](" + url + altAttr + ")](" + link + altAttr + ")")
                	
                }

                $("#imageModal").modal("hide");
			});
		}
		
		if($("#linkModal").length == 0){
			$(linkModal).appendTo($('body'));
			$("#linkModal").on('hidden.bs.modal',function(){
				fileSelectPageQuery(lastParam.currentPage,lastParam.parent);
				$("#fileSelectModal").modal("show");
			});
			
			$("#link-confirm-btn").click(function(){
				  var url  = $("#linkModal").find("input").eq(0).val();
                var title  = $("#linkModal").find("input").eq(1).val();

                if (url === "http://" || url === "https://" || url === "")
                {
              	  $("#linkModal").modal("hide");return;
                }
                
                var str  = "[" + title + "](" + url + " \"" + title + "\")"
                
                if (title == "")
                {
                	str = "[" + url + "](" + url + ")";
                }                                

                editor.replaceSelection(str);

                $("#linkModal").modal("hide");
			});
		}
	}
	

	var imageExtensions = ["jpg","jpeg","png","gif","webp"];
	var isImage = function(ext){
		for(var i=0;i<imageExtensions.length;i++){
			if(ext.toLowerCase() == imageExtensions[i]){
				return true;
			}
		}
		return false;
	}
	var lastParam = {currentPage : 1};
	var fileSelectPageQuery = function(page,parent){
		var param = {};
		param.currentPage = page;
		if(parent && parent != "")
			param.parent = parent;
		$("#fileSelectMain").html("<img src='"+basePath+"/static/img/loading.gif' class='img-responsive center-block' />")
		$.get(basePath+'/mgr/file/query',param,function(data){
			if(data.success){
				lastParam = param;
				$("#fileSelectMain").html(getRenderHtml(data.data));
				$('#fileSelectMain [data-toggle="tooltip"]').tooltip();
			} else {
				$("#fileSelectMain").html('<div class="row"><div class="col-md-12"><div class="alert alert-danger">'+data.message+'</div></div></div>');
			}
		})
	}
	
	var getRenderHtml = function(data){
		var html = '';
		var paths = data.paths;
		if(paths.length > 0){
			html += '<div class="row">';
			html += '<div class="col-md-12">';
			html += '<ol class="breadcrumb">';
			html += '<li><a href="###"  data-parent="" data-page="1" >根目录</a></li>'	;
			for(var i=0;i<paths.length;i++){
				var path = paths[i];
				html += '<li><a href="###" data-parent="'+path.id+'" data-page="1" >'+path.path+'</a></li>';
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
				if(data.type == 'DIRECTORY'){
					html += '<a href="###" data-page="1" data-parent="'+data.id+'" "><img src="'+basePath+'/static/fileicon/folder.png" class="img-responsive" style="height:100px"/></a>';
				} else {
					var url =data.cf.url;
					html += '<a href="###" data-extension="'+data.cf.extension+'"  data-url="'+url+'" data-description="'+data.cf.originalFilename+'">'
					if(data.cf.thumbnailUrl){
						html += '<img  src="'+data.cf.thumbnailUrl.small+'" data-middle="'+data.cf.thumbnailUrl.middle+'" class="img-responsive" style="height:100px"/>';
					} else {
						html += '<img src="'+basePath+'/static/fileicon/file.png" class="img-responsive" style="height:100px"/>';
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
			html += '<div>';
			html += '<ul class="pagination">';
			for(var i=page.listbegin;i<=page.listend-1;i++){
				html += '<li>';
				html += '<a href="###" data-page="'+i+'" data-parent="'+(lastParam.parent ? lastParam.parent : "")+'" >'+i+'</a>';
				html += '</li>';
			}
			html += '</ul>';
			html += '</div>';
		}
		html += '</div>';
		html += '</div>';
		return html;
	}
