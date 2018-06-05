var sfq = (function() {
	var params = {currentPage:1};
	$('body').append('<div class="modal fade" id="sfqModal" tabindex="-1" role="dialog"><div class="modal-dialog modal-lg" role="document"><div class="modal-content"><div class="modal-header"><button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button><h4 class="modal-title">静态资源选择</h4></div><div class="modal-body"><div class="container-fluid" id="sfqMain"><div class="row"><div class="col-md-12"><form class="form-inline" style="margin-bottom:10px"><div class="form-group" style="margin-right:5px"><input type="text" class="form-control" placeholder="文件名"></div><div class="checkbox" style="margin-right:5px"><label><input type="checkbox">查询子文件夹</label></div><button type="button" class="btn btn-default">查询</button></form></div></div><div class="row"></div></div></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">关闭</button></div></div></div></div>');
	var modal = $("#sfqModal");
	var statusFunctions = [];
	var data = [];
	var fileClickFunction;
	var fileWriteModal = {
		
		id:'commonCopyModal',
		isInit : false,
		clipboard:undefined,
		show : function(data){
			var clipboard = this.clipboard;
			var _modal = $("#"+this.id);
			if(!this.isInit){
				$('body').append('<div class="modal fade" tabindex="-1" role="dialog" id="'+this.id+'"><div class="modal-dialog" role="document"><div class="modal-content"><div class="modal-header"><button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button></div><div class="modal-body"><div class="tip"></div><form><div class="form-group"><div class="input-group"><input type="text" class="form-control" placeholder="" readonly="readonly"><div class="input-group-addon"><a href="###" data-clipboard-text><span class="glyphicon glyphicon-copy" aria-hidden="true"></span></a></div></div></div></form></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">关闭</button></div></div></div></div>');
				_modal = $("#"+this.id);
				this.isInit = true;
				_modal.on('hidden.bs.modal',function(){
					showModal(false);
				});
				_modal.on('shown.bs.modal',function(){
					if(clipboard){
						clipboard.destroy();
					}
					clipboard = new Clipboard('[data-clipboard-text]', {
					    container: document.getElementById(this.id)
					});
					clipboard.on('success',function(){
						_modal.find('.tip').html('<p class="text text-info">拷贝成功</p>')
					});
					clipboard.on('error',function(){
						_modal.find('.tip').html('<p class="text text-danger">拷贝失败</p>')
					})
				})
			}
			_modal.find('.tip').html('')
			statusFunctions.push({
				data : data,
				modal :_modal,
				id:'fileWriter',
				onHidden:function(){
					this.modal.find('input[type="text"]').val(basePath+'/static/'+this.data.path);
					this.modal.find('[data-clipboard-text]').attr('data-clipboard-text',basePath+'/static/'+this.data.path);
					this.modal.modal('show');
				}
			});
			
			modal.modal('hide');
		}
	};
	
	
	modal.on('shown.bs.modal',function(){
		statusFunctions = [];
	})
	modal.on('hidden.bs.modal',function(){
		for(var i=0;i<statusFunctions.length;i++){
			statusFunctions[i].onHidden();
		}
	});
	
	modal.on("click","a[data-page]",function(){
		var page = parseInt($(this).attr('data-page'));
		toPage(page);
	});
	
	modal.on("click","a[data-path]",function(){
		var path = $(this).attr('data-path');
		params = {currentPage:1};
		clearQuery();
		params.path = path;
		toPage(1);
	});
	
	
	modal.find('form input[type="text"]').eq(0).on('keydown',function(event){
		if(event.keyCode==13){ 
			query();
		} 
	});
	
	modal.find('form button').click(function(){
		query();
	});
	
	modal.on('click',"a[data-file]",function(){
		var path = $(this).attr('data-file-path');
		if(fileClickFunction){
			if(fileClickFunction(path)){
				modal.modal('hide');
			}
		} else {
			var data;
			for(var i=0;i<datas.length;i++){
				if(datas[i].path == path){
					data = datas[i];
					break;
				}
			}
			if(data){
				delStatusFunction("fileWriter");
				fileWriteModal.show(data);
			}
		}
	});
	
	var getFileWriteModal = function(data){
		for(var i=0;i<fileWriteModals.length;i++){
			if(fileWriteModals[i].id  == 'commonCopyModal'){
				return fileWriteModals[i];
			}
		}
	}
	
	var delStatusFunction = function(id){
		for(var i=0;i<statusFunctions.length;i++){
			if(statusFunctions[i].id == id){
				statusFunctions.splice(i,1);
				break;
			}
		}
	}
	
	var clearQuery = function(){
		modal.find('form input[type="text"]').eq(0).val("");
		 modal.find('form input[type="checkbox"]').eq(0).prop('checked',false);
	}
	
	var query = function(){
		var name = modal.find('form input[type="text"]').eq(0).val();
		var querySub = modal.find('form input[type="checkbox"]').eq(0).is(":checked");
		params.name = name;
		params.querySubDir = querySub;
		toPage(1);
	}
	
	var toPage = function(page){
		params.currentPage = page;
		var rows = modal.find('.row');
		var row = rows.eq(rows.length-1);
		row.html("<img src='"+basePath+"/static/img/loading.gif' class='img-responsive center-block' />");
		if(row.next()){
			row.next().remove();
		}
		$.get(basePath+'/mgr/static/query',params,function(data){
			if(data.success){
				var result = data.data;
				render(result);
			}else{
				bootbox.alert(data.message);
			}
		});
	};
	
	var render = function(result){
		var page = result.page;
		var paths = result.paths;
		var rows = modal.find('.row');
		if(paths.length == 0){
			if(rows.length > 2){
				rows.eq(1).remove();
			}
		} else {
			var nav = '<ol class="breadcrumb">';
			nav += '<li><a href="###" data-path="">根目录</a></li>';
			for(var i=0;i<paths.length;i++){
				var path = paths[i];
				nav += '<li><a href="###" data-path="'+path.path+'">'+path.name+'</a></li>';
			}
			nav += '</ol>';
			if(rows.length > 2){
				rows.eq(1).html('<div class="col-md-12">'+nav+'</div>');
			} else {
				rows.eq(1).before('<div class="row"><div class="col-md-12">'+nav+'</div></div>')
			}
		}
		rows = modal.find('.row');
		var row = paths.length > 0 ? rows.eq(2) : rows.eq(1);
		datas = page.datas;
		if(datas.length > 0){
			var html = '';
			for(var i=0;i<datas.length;i++){
				var data = datas[i];
				if(data.dir){
					html += ('<div class="col-xs-6 col-md-4"><div class="thumbnail text-center"><a href="###" data-path="'+data.path+'"><img src="'+basePath+'/static/fileicon/folder.png" class="img-responsive" style="height:100px"></a><div class="caption" style="height:35px">'+data.name+'</div></div></div>');
				} else {
					html += ('<div class="col-xs-6 col-md-4"><div class="thumbnail text-center"><a href="###" data-file="true" data-file-path="'+data.path+'"><img src="'+basePath+'/static/fileicon/file.png" class="img-responsive" style="height:100px"></a><div class="caption" style="height:35px">'+data.name+'</div></div></div>');
				}
			}
			row.html(html);
		} else {
			row.html('<div class="col-md-12"><div class="alert alert-warning">当前没有任何静态资源文件</div></div>')
		}
		var dom = row.next();
		if(dom){
			row.next().remove();
		}
		if(page.totalPage > 1){
			var html = '<div>';
			html += '<ul class="pagination">';
			for(var i=page.listbegin;i<=page.listend-1;i++){
				if(i == page.currentPage){
					html += '<li class="active"><a href="###" >'+i+'</a></li>';
				}else{
					html += '<li><a href="###" data-page="'+i+'" >'+i+'</a></li>';
				}
			}
			html += '</ul>';
			html += '</div>';
			row.after(html);
		} 
	}
	
	
	var showModal = function(reQuery){
		if(reQuery){
			params = {currentPage:1};
			clearQuery();
			toPage(1);
		}
		modal.modal('show');
	}
	return {
		show:function(){
			showModal(true);
		},
		setFileClickFunction:function(fun){
			fileClickFunction = fun;
		}
	};
})();
