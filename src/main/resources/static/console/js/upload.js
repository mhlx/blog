var Upload = (function(){
	
	function Upload(url,config){
		this.url = url;
		this.config = config || {};
		this.modal = appendUploadModal();
		this._file = this.modal.querySelector('input[type="file"]');
		this._cancel = this.modal.querySelector('.btn-warning');
		this._files = this.modal.querySelector('.files');
		this.uploadingItems = [];
		this.successfulUploads = [];
		registerEventListener(this);;
	}
	
	Upload.prototype.open = function(){
		$(this.modal).modal({backdrop: 'static', keyboard: false});
		this.successfulUploads = [];
	}
	
	function registerEventListener(upload){
		upload._file.addEventListener('change',function(){
			if(this.files.length > 0){
				for(const file of this.files){
					processOneFile(file,upload);
				}
			}
		});
		var cancelAll = function(){
			for(const uploadingItem of upload.uploadingItems){
				uploadingItem.cancel();
			}
			upload.uploadingItems = [];
			upload._files.innerHTML = '';
			upload._file.value = null;
		}
		upload._cancel.addEventListener('click',function(){
			cancelAll();
		})
		$(upload.modal).on('hide.bs.modal', function () {
			cancelAll();
		});
		$(upload.modal).on('hidden.bs.modal', function () {
			if(upload.config.close){
				upload.config.close(upload.successfulUploads);
			}
		});
		
		upload.modal.addEventListener("drop", function(event) {
			event.preventDefault();
			var files = event.dataTransfer.files;
			if(files.length > 0){
				for(const file of files){
					processOneFile(file,upload);
				}
			}
			return false;
		}, false);
		
		upload.modal.addEventListener("dragover", function(event) {
			event.preventDefault();
		}, false);
		
		upload.modal.addEventListener("paste", function(event) {
			event.preventDefault();
			var items = event.clipboardData.items;
			if(items && items.length > 0){
				for (var i = 0; i < items.length; i++) {
			        if (items[i].type.indexOf("image") == -1) continue;
			        var blob = items[i].getAsFile();
			        blob.from = 'paste';
			        processOneFile(blob,upload);
			    }
			}
			return false;
		}, false);
	}
	
	function processOneFile(file,upload){
		var container = document.createElement('div');
		container.setAttribute('style','margin-bottom:10px');
		container.setAttribute('class','row');
		container.innerHTML = '<div class="col-9"><small class="file-name">'+file.name+'</small><div class="progress"><div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" ></div></div></div>';
		upload._files.appendChild(container);
		startUpload(file,container,upload);
	}
	
	function startUpload(file,container,upload){
		var formdata = new FormData();
		if(file.from === 'paste')
			formdata.append("file", file,new Date().getTime() + '.'+file.name.split('.').pop());
		else
			formdata.append("file", file);
		if(upload.config.formDataHandler){
			upload.config.formDataHandler(formdata);	
		}
		var progressBar = container.querySelector('.progress-bar');
		var ajax = new XMLHttpRequest();

		
		var opContainer = document.createElement('div');
		opContainer.setAttribute('class','col-3 align-self-center')
		opContainer.setAttribute('style','cursor:pointer;text-align:center');
		container.append(opContainer);
		
		var cancelButton ;
		ajax.upload.addEventListener("progress", function(event){
			if(!cancelButton){
				cancelButton = document.createElement('i');
				cancelButton.setAttribute('class','fas fa-times');
				cancelButton.setAttribute('style','cursor:pointer;font-size:1.5rem');
				cancelButton.addEventListener('click',function(){
					ajax.abort();
				});
				opContainer.appendChild(cancelButton);
			}
			var percent = (event.loaded / event.total) * 100;
			progressBar.style.width = percent+'%';
		}, false);
		
		var processResponse = function(response){
			progressBar.parentElement.remove();
			if(response.errors){
				var errorDiv = document.createElement('div');
				errorDiv.setAttribute('class','text text-danger');
				var errors = [];
				for(const error of response.errors){
					errors.push(error.message);
				}
				errorDiv.innerHTML = errors.join('<br>');
				container.firstChild.appendChild(errorDiv);
			} else {
				upload.successfulUploads.push(response);
				// successful upload
				// display thumbPath
				if(response.thumbPath){
					var thumbDiv = document.createElement('div');
					thumbDiv.innerHTML = '<img src="'+rootPath+response.thumbPath+'" style="max-height:64px" class="img-fluid rounded"/>';
					container.firstChild.appendChild(thumbDiv);
				}
			}
			opContainer.innerHTML = '';
			var confirmButton = document.createElement('i');
			confirmButton.setAttribute('class','fas fa-check');
			confirmButton.setAttribute('style','cursor:pointer;font-size:1.5rem');
			confirmButton.addEventListener('click',function(){
				container.remove();
				//remove from file list ? 
			});
			opContainer.appendChild(confirmButton);
		}
		
		ajax.addEventListener("load", function(event){
			var response = JSON.parse(event.target.responseText);
			processResponse(response);
		}, false);
		ajax.addEventListener("error", function(event){
			processResponse({'errors':[{'message':'连接到服务器失败'}]})
		}, false);
		ajax.addEventListener("abort", function(){
			processResponse({'errors':[{'message':'上传取消'}]});
		}, false);
		ajax.open("POST", upload.url());
		ajax.send(formdata);
		
		upload.uploadingItems.push({
			cancel : function(){
				ajax.abort();
			}
		})
	}
	
	
	function appendUploadModal(){
		var modal = document.createElement('div');
		modal.setAttribute('class','modal fade ');
		modal.setAttribute('tabindex','-1');
		modal.innerHTML = '<div class="modal-dialog modal-lg modal-dialog-centered"><div class="modal-content"><div class="modal-header"><h5 class="modal-title">文件上传</h5><button type="button" class="close" data-dismiss="modal" aria-label="Close"></button></div><div class="modal-body"><label class="btn btn-warning" style="margin-right:10px">取消上传</label><label class="btn btn-primary">选择文件<input type="file" multiple hidden></label><div class="files"></div></div></div></div>';
		document.body.appendChild(modal);
		return modal;
	}
	
	return Upload;
})();