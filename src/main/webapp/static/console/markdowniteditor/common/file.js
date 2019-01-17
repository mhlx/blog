var fileChooser = (function(){
	var basePath = getUrlParam('basePath','');
	if(basePath.endsWith('/')){
		basePath = basePath.substring(0,basePath.length-1);
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
	fileSelectModal.find('[data-choose]').click(function(){
		try{
			if(fileCallback){
				fileCallback(selected);
			} 
		}catch (e) {
			
		}
		fileSelectModal.modal('hide');
	});
	
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