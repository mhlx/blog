var FileSelector = (function(){
	
	function FileSelector(callback){
		this.modal = appendSelectorModal();
		var buttons = this.modal.querySelectorAll('button');
		this.newFileButton = buttons[1];
		this.uploadButton = buttons[2];
		this.selectButton = buttons[3];
		this.selects = [];
		this.callback = callback;
		this.query = new Query(this);
		var me = this;
		this.upload = new Upload(function(){
			return rootPath + 'console/file/upload'
		},{
			'formDataHandler':function(formData){
				formData.append("dirPath", me.query.param.path || '');
			},
			'close' :function(uploads){
				me.open();
			}
		});
		registerEvents(this);
	}
	
	FileSelector.prototype.getPath = function(){
		return this.query.param.path || "";
	}
	
	FileSelector.prototype.open = function(){
		$(this.modal).modal();
	}	
	FileSelector.prototype.close = function(){
		$(this.modal).modal('hide');
	}		
	FileSelector.prototype.isSelected = function(file){
		if(file.dir) return false;
		for(const selected of this.selects){
			if(selected.path === file.path)
				return true;
		}
		return false;
	}
	
	FileSelector.prototype.unSelect = function(file){
		for(var i=this.selects.length-1;i>=0;i--){
			if(this.selects[i].path === file.path){
				this.selects.splice(i,1);
				break;
			}
		}
	}	
	
	function registerEvents(selector){
		$(selector.modal).on('show.bs.modal',function(){
			selector.selects = [];
			selector.query.query(selector.queryParam||{});
		});
		$(selector.modal).on('hidden.bs.modal',function(event){
			if(selector.afterClose){
				selector.afterClose();
				selector.afterClose = undefined;
			}
		});
		selector.uploadButton.addEventListener('click',function(){
			selector.afterClose = function(){
				selector.upload.open();
			}
			selector.close();
		});
		selector.selectButton.addEventListener('click',function(){
			selector.close();
			if(selector.callback){
				selector.callback(selector.selects);
			}
		});
		selector.newFileButton.addEventListener('click',function(){
			selector.afterClose = function(){
				(async function getPath () {
					const { value: formValues } = await Swal.fire({
						  title: '文件信息',
						  html:
						    '<input type="text"  class="swal2-input">' +
						    '<label class="swal2-checkbox"><input type="checkbox" >文件夹</label>',
						  focusConfirm: false,
						  preConfirm: () => {
							  var content = Swal.getContent();
						    return [
						    	content.querySelector('input[type="text"]').value,content.querySelector('input[type="checkbox"]').checked
						    ]
						  },
						  onAfterClose:function(){
							  selector.open();
						  }
						})
				    if (formValues) {
					  var path = formValues[0];
					  var dir = formValues[1];
					  $.ajax({
						type : 'POST',
						url : rootPath + 'console/file/create',
						dataType: "json",
			            contentType: 'application/json',
						data:JSON.stringify({'path':(selector.query.param.path || '')+'/'+path,'type':dir?'DIR':'FILE'}),
						success:function(data) {
							toast('保存成功');
						},
						error:function(e) {
							var html = '';
							var errors = e.responseJSON.errors;
							for(const error of errors){
								html += error.message+'<br>';
							}
							toast(html,'error');
						},
						complete:function(){
							selector.open();
						}
					  })
					}
				})()
			}
			selector.close();
		});
	}
	
	
	var Query = (function(){
		function Query(selector){
			this.files = selector.modal.querySelector('.files');
			this.paging = selector.modal.querySelector('.paging');
			this.paths = selector.modal.querySelector('.paths');
			this.input = selector.modal.querySelector('input');
			this.param = {currentPage:1,pageSize:12};
			this.selector = selector;
			this.datas = [];
			registerEvent(this);
		}
		
		function registerEvent(query){
			query.paging.addEventListener('click',function(e){
				if(e.target.hasAttribute('data-page')){
					query.query({currentPage:parseInt(e.target.dataset.page)});
				}
			});
			query.files.addEventListener('click',function(e){
				var target = e.target;
				while(target != null){
					if(target.hasAttribute('data-dir')){
						break;
					}
					target = target.parentElement;
				}
				if(target != null){
					var path = target.dataset.path;
					if(target.dataset.dir === 'true'){
						query.query({path : path,currentPage:1});
					} else {
						for(const data of query.datas){
							if(data.path === path){
								if(query.selector.isSelected(data)){
									query.selector.unSelect(data);
									target.firstElementChild.removeAttribute('style');
								} else {
									query.selector.selects.push(data);
									target.firstElementChild.setAttribute('style','border:2px solid red');
								}
								break;
							}
						}
					}
				}
			})
			query.paths.addEventListener('click',function(e){
				if(e.target.hasAttribute('data-path')){
					var path = e.target.dataset.path;
					query.query({path : path,currentPage:1});
				}
			})
			query.input.addEventListener('keypress',function(e){
				if(e.key == 'Enter'){
					query.query({name : this.value,currentPage:1});
				}
			})
		}
		
		Query.prototype.query = function(param){
			var me = this;
			me.param = Object.assign(me.param, param);
			$.ajax({
				url : rootPath + 'console/files/query',
				data : me.param,
				error : function(data) {
					
				},
				success : function(data) {
					var datas = data.datas;
					var html = '';
					for (var i = 0; i < datas.length; i++) {
						html += processOneFile(datas[i],me.selector);
					}
					me.files.innerHTML = html;
					var pagingHtml = processPaging(data);
					me.paging.innerHTML = pagingHtml;
					me.paths.innerHTML = processPaths(data);
					me.datas = datas;
				}
			})
		}
		
		function processPaging(data){
			//remove old paging
			if(data.totalPage <= 1) return "";
			var html = '';
			html += '<ul class="pagination pagination-simple  justify-content-center">';
			var p = calcPaging(5,data);
			for (var j = p.first; j <= p.last; j++) {
				if (j == data.currentPage) {
					html += '<li class="page-item active"><a class="page-link" href="javascript:void(0)" >'
							+ j + '</a></li>';
				} else {
					html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
							+ j + '">' + j + '</a></li>';
				}
			}
			html += '</ul>';
			return html;
		}
		
		function processOneFile(file,selector){
			var html = '<div class="col-sm-4 col-lg-3 col-6" data-path="'+file.path+'" data-dir="'+file.dir+'" style="cursor:pointer">';
			if(selector.isSelected(file)){
				html += '<div class="card p-3 h-100" style="border:2px solid red">';
			} else {
				html += '<div class="card p-3 h-100">';
			}
			html += '<div style="margin-bottom: 5px">';
			if(file.ext){
				var ext = file.ext.toLowerCase();
				if(ext == 'mp4' || ext == 'mov'){
					html += '<i class="fas fa-video" style="position: relative;left: calc(50% - 10px);top: calc(50%);  color: yellowgreen;"></i>'
				}
			}
			var path = file.dir ? rootPath+'static/tabler/dist/assets/images/folder.svg' : file.smallThumbPath ? rootPath+file.smallThumbPath : rootPath+'static/tabler/dist/assets/images/file.svg';
			html += '<img src="'+path+'" class="rounded img-fluid"/>'
			html += '</div>';
			html += '<div style="text-overflow: ellipsis; overflow: hidden; white-space: nowrap;"><small>'+file.name+'</small></div>';
			html += '</div>';
			html += '</div>';
			return html;
		}
		
		
		function processPaths(data){
			if(data.paths.length == 0){
				return "";
			} else {
				var html = '<nav><ol class="breadcrumb">';
				html += '<li class="breadcrumb-item"><a href="#" data-path="">根目录</a></li>'
				for(var i=0;i<data.paths.length;i++){
					var path = data.paths[i];
					var clazzAppend = ' ';
					if(i == data.paths.length - 1){
						clazzAppend += 'active';
					}
					html += '<li class="breadcrumb-item'+clazzAppend+'"><a href="#" data-path="'+path.path+'">'+path.name+'</a></li>'
				}
				html += '</ol></nav>';
				return html;
			}
		}
		return Query;
	})();
	
	
	function appendSelectorModal(){
		var modal = document.createElement('div');
		modal.setAttribute('class','modal fade ');
		modal.setAttribute('tabindex','-1');
		modal.innerHTML = '<div class="modal-dialog modal-lg modal-dialog-centered"><div class="modal-content"><div class="modal-header"><h5 class="modal-title">文件选择</h5><button type="button" class="close" data-dismiss="modal" aria-label="Close"></button></div><div class="modal-body"><div class="page-header"><div class="page-options d-flex"><div class="input-icon ml-2"><span class="input-icon-addon"> <i class="fe fe-search"></i></span> <input type="text" class="form-control w-10" placeholder="文件名"></div><button class="btn btn-primary"  style="margin-left: 20px"><i class="far fa-plus-square"></i></button><button class="btn btn-primary"  style="margin-left: 20px"><i class="fas fa-upload"></i></button><button class="btn btn-primary"  style="margin-left: 20px"><i class="fas fa-check"></i></button></div></div><div class="paths"></div><div class="files row row-cards"></div><div class="paging"></div></div></div></div>';
		document.body.appendChild(modal);
		return modal;
	}
	
	return FileSelector;
})();