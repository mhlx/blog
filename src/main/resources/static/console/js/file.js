	
	var clipboard = new ClipboardJS('[data-copy-url]');

	clipboard.on('success', function(e) {
		toast('拷贝成功')
	});

	clipboard.on('error', function(e) {
		toast('拷贝失败','error')
	});

	var $file = (function() {
		var _param = {};
		var pageSize = 18;
		var container = document.getElementById('files');
		var navContainer = document.getElementById('file-navs');
		var videoExts = ['mp4','mov'];
		navContainer.addEventListener('click',function(e){
			if(e.target.hasAttribute('data-path')){
				var path = e.target.dataset.path;
				query({path : path,currentPage:1})
			}
		})
		
		
		var upload = new Upload(function(){
			return rootPath + 'console/file/upload'
		},{
			'formDataHandler':function(formData){
				formData.append("dirPath", _param.path || '');
			},
			'close' :function(uploads){
				if(uploads.length > 0){
					query(_param);
				}
			}
		});
		
		document.getElementById('upload-file').addEventListener('click',function(){
			upload.open();
		})
		
		document.getElementById('fileStatistics').addEventListener('click',function(){
			loadStatistics('')
		})
		
		document.getElementById('new-file').addEventListener('click',function(){
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
					data:JSON.stringify({'path':(_param.path || '')+'/'+path,'type':dir?'DIR':'FILE'}),
					success:function(data) {
						toast('保存成功');
						query(_param);
					}
				  })
				}
			})()
		});
		
		container.addEventListener('click',function(e){
			var path = getPathNode(e.target,'data-edit');
			if(path != null){
				storeParam();
				window.location.href = rootPath + 'console/file/edit?path='+encodeURIComponent(path.dataset.path);
				return ;
			}
			path = getPathNode(e.target,'data-trash');
			if(path != null){
				Swal.fire({
					  title: '确定删除吗？',
					  text: "文件将会被立即删除，并且无法恢复！！",
					  icon: 'warning',
					  showCancelButton: true,
					  confirmButtonColor: '#3085d6',
					  cancelButtonColor: '#d33'
					}).then((result) => {
					  if (result.value) {
						  	$.ajax({
								type : 'post',
								url : rootPath + 'console/file/delete?path='+encodeURIComponent(path.dataset.path),
								success:function(data) {
									toast('删除成功');
									query(_param);
								}
							  })
					  }
					})
					return ;
			}
			path = getPathNode(e.target,'data-rename');
			if(path != null){
				var fileName = path.dataset.name;
				var ext = path.dataset.ext;
				if(ext){
					fileName = fileName.substring(0,fileName.length - ext.length-1);
				}
				var path = path.dataset.path;
				(async function rename () {
					const { value: name } = await Swal.fire({
						  input: 'text',
						  inputValue: fileName,
						  showCancelButton: true
						})
	
						if (name) {
							$.ajax({
								type : 'post',
								url : rootPath + 'console/file/update?path='+encodeURIComponent(path),
					            contentType: 'application/json',
								data:JSON.stringify({'name':name}),
								success:function(data) {
									toast('保存成功');
									query(_param);
								}
							  })
						}
				})()
				return ;
			}
			path = getPathNode(e.target,'data-copy');
			if(path != null){
				var dirPath = path.dataset.path;
				var index = dirPath.lastIndexOf('/');
				if(index != -1){
					dirPath = dirPath.substring(0,index);
				} else {
					dirPath = '';
				}
				(async function rename () {
					const { value: dir } = await Swal.fire({
						title:'要复制到哪个文件夹内？',
						  input: 'text',
						  inputValue:dirPath,
						  showCancelButton: true
						})
	
						if (dir) {
							$.ajax({
								type : 'post',
								url : rootPath + 'console/file/copy?source='+path.dataset.path+'&dir='+dir,
								success:function(data) {
									query(_param);
									toast('保存成功');
								}
							  })
						}
				})()
				return ;
			}
			path = getPathNode(e.target,'data-move');
			if(path != null){
				var dirPath = path.dataset.path;
				var index = dirPath.lastIndexOf('/');
				if(index != -1){
					dirPath = dirPath.substring(0,index);
				} else {
					dirPath = '';
				}
				(async function rename () {
					const { value: dir } = await Swal.fire({
						title:'要移动到哪个文件夹内？',
						  input: 'text',
						  inputValue:dirPath,
						  showCancelButton: true
						})
	
						if (dir) {
							$.ajax({
								type : 'post',
								url : rootPath + 'console/file/update?path='+encodeURIComponent(path.dataset.path),
					            contentType: 'application/json',
								data:JSON.stringify({'dirPath':dir}),
								success:function(data) {
									query(_param);
									toast('保存成功');
								}
							  })
						}
				})()
				return ;
			}
			path = getPathNode(e.target,'data-info');
			if(path != null){
				loadStatistics(path.dataset.path)
			}
		});
		
		document.getElementById('name').addEventListener('keydown',function(e){
			if(e.key === 'Enter'){
				query({path:_param.path || '',currentPage:1});
			}
		})
		
		var getPathNode = function(target,attr){
			while(target != null){
				if(target.hasAttribute(attr)){
					break;
				}
				target = target.parentElement;
			}
			if(target != null){
				
				var path;
				while(target != null){
					if(target.hasAttribute('data-path')){
						path = target.dataset.path;
						break;
					}
					target = target.parentElement;
				}
				
				return target;
			}
			return null;
		}
		
		var storeParam = function(){
			try{
				sessionStorage.setItem('blog.file.param',JSON.stringify(_param));
			}catch(_e){}
		}
		
		var getStoredParam = function(){
			try{
				var o = JSON.parse(sessionStorage.getItem('blog.file.param'));
				sessionStorage.removeItem('blog.file.param');
				return o;
			}catch(_e){}
			return {};
		}
		
		var writePaging = function(data){
			//remove old paging
			var paging = document.getElementById('paging');
			if(paging != null) paging.remove();
			if (data.totalPage > 1) {
				paging = document.createElement('ul');
				paging.setAttribute('class','pagination pagination-simple  justify-content-center');
				paging.id = 'paging';
				var html = '';
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
				paging.innerHTML = html;
				paging.addEventListener('click',function(e){
					if(e.target.hasAttribute('data-page')){
						var page = e.target.dataset.page;
						_param.currentPage = page;
						query(_param)
					}
				})
				container.after(paging);
			}
		}
		var appendFile = function(file) {
			var template = file.dir ? document.querySelector('#dirTemplate')
					: document.querySelector('#fileTemplate');
			var clone = document.importNode(template.content, true);
			clone.querySelector('[data-name]').innerHTML = file.name;
			var fc = clone.firstElementChild;
			fc.dataset.path = file.path;
			if(!file.dir)
				fc.dataset.ext = file.ext;
			fc.dataset.name = file.name;
			if (file.dir) {
				var link = clone.querySelector('a');
				link.dataset.path = file.path;
				link.addEventListener('click', function() {
					var path = this.dataset.path;
					query({
						path : path,
						currentPage : 1
					})
				});
			} else {
				if(file.smallThumbPath){
					clone.querySelector('img').src = rootPath + file.smallThumbPath;
				} else {
					clone.querySelector('img').src = rootPath
							+ 'static/tabler/dist/assets/images/file.svg'
				}
				clone.querySelector('[data-copy-url]').dataset.clipboardText = rootPath + file.path;
				
				if(file.editable){
					var edit = clone.querySelector('[data-edit]');
					edit.style.display = '';
				}
				if(videoExts.includes(file.ext.toLowerCase())){
					var play = clone.querySelector('[data-play]');
					play.href = rootPath + file.path;
					play.style.display = '';
				}
			}
			var shield = `<i class="fas fa-shield-alt" style="
			    position: absolute;
			    left: 3px;
			    top: 3px;
			"></i>`;
			if(file['private'] || file['protected']){
				var div = document.createElement('div');
				div.innerHTML = shield;
				shield = div.firstChild;
				clone.firstElementChild.firstElementChild.prepend(shield)
			}
			container.appendChild(clone);
		}

		var query = function(param) {
			param = param || {
				path : '',
				currentPage : 1
			};
			_param = param;
			var name = document.getElementById('name').value;
			$.ajax({
				url : rootPath + 'console/files/query',
				data : {
					path : param.path,
					currentPage : param.currentPage,
					pageSize : pageSize,
					name : name,
					querySubDir: name.trim() != ''
				},
				success : function(data) {
					container.innerHTML = '';
					var datas = data.datas;
					for (var i = 0; i < datas.length; i++) {
						var file = datas[i];
						appendFile(file);
					}
					writePaging(data);
					if(data.paths.length == 0){
						navContainer.style.display = 'none';
					} else {
						var html = '';
						html += '<li class="breadcrumb-item"><a href="#" data-path="">根目录</a></li>'
						for(var i=0;i<data.paths.length;i++){
							var path = data.paths[i];
							var clazzAppend = ' ';
							if(i == data.paths.length - 1){
								clazzAppend += 'active';
							}
							html += '<li class="breadcrumb-item'+clazzAppend+'"><a href="#" data-path="'+path.path+'">'+path.name+'</a></li>'
						}
						navContainer.innerHTML = html;
						navContainer.style.display = '';
					}
				}
			})
		}

		query(getStoredParam());
		
		function loadStatistics(path){
			$.ajax({
				type : 'get',
				url : rootPath + 'console/file/get?path='+encodeURIComponent(path),
				success:function(data) {
					
					var html = '<div class="table-responsive"><table class="table">';
					html += '<tr><th  style="word-break:keep-all">路径</th><td>'+data.path+'</td></tr>';
					html += '<tr><th  style="word-break:keep-all">文件名</th><td>'+data.name+'</td></tr>';
					if(data.ext){
						html += '<tr><th  style="word-break:keep-all">文件后缀</th><td>'+data.ext+'</td></tr>';
					}
					html += '<tr><th  style="word-break:keep-all">可编辑</th><td>'+(data.editable?'是':'否')+'</td></tr>';
					if(data.properties){
						html += '<tr><th  style="word-break:keep-all">大小(字节)</th><td>'+data.properties.size+'</td></tr>';
						if(data.properties.dirCount){
							html += '<tr><th  style="word-break:keep-all">子文件夹数目</th><td>'+data.properties.dirCount+'</td></tr>';
						}
						if(data.properties.fileCount){
							html += '<tr><th  style="word-break:keep-all">子文件数目</th><td>'+data.properties.fileCount+'</td></tr>';
						}
						var typeStatistics = data.properties.typeStatistics;
						if(typeStatistics && typeStatistics.length > 0){
							html += '<tr><th>类型统计</th><td>';
							html += '<table class="table">';
							html += '<tr><th style="word-break:keep-all">类型</th><th  style="word-break:keep-all">数目</th><th  style="word-break:keep-all">大小(字节)</th></tr>';
							for(const typeStat of typeStatistics){
								html += '<tr><td>'+typeStat.type+'</td><td>'+typeStat.count+'</td><td>'+typeStat.size+'</td></tr>';
							}
							html +='</table>';
							html +='</td></tr>'
						}
						if(data.properties.width){
							html += '<tr><th  style="word-break:keep-all">宽</th><td>'+data.properties.width+'</td></tr>';
						}
						if(data.properties.height){
							html += '<tr><th  style="word-break:keep-all">高</th><td>'+data.properties.height+'</td></tr>';
						}
						if(data.properties.duration){
							html += '<tr><th  style="word-break:keep-all">视频时长</th><td>'+data.properties.duration+'</td></tr>';
						}
						if(data.properties.type){
							html += '<tr><th  style="word-break:keep-all">图片类型</th><td>'+data.properties.type+'</td></tr>';
						}
					}
					html += '</table></div>';
					$('#proModal .modal-body').html(html);
					$('#proModal').modal('show');
				}
			  })
		}
		
		return {
			reload : function(){
				query(_param);
			}
		}
	})();
	
	

function addSecurityPath(){
	Swal.fire({
		 html : '<input type="text" placeholder="路径" class="swal2-input"/><input type="text" placeholder="密码，不填写则仅限私有访问" class="swal2-input"/>',
		 preConfirm: () => {
			 var body = Swal.getContent();
			 var inputs = body.querySelectorAll('input');
			    return [
			    	inputs[0].value,
			    	inputs[1].value
			    ]
			  }
	}).then((result) => {
		result = result.value;
		$.ajax({
			type : 'post',
			url : rootPath + 'console/securityPath/save?path='+encodeURIComponent(result[0])+"&password="+result[1],
			success:function(data) {
				toast('添加成功');
				$file.reload();
				showSecurityPaths();
			}
		  })
		})
}
function showSecurityPaths(){
	$.get(rootPath+'console/securityPaths',{},function(data){
		
		var html = '<div style="text-align:left;padding:30px">';
		html += '<p><a href="javascript:void(0)" onclick="addSecurityPath()">新增安全路径</a></p>';
		if(!data.PASSWORD && !data.PRIVATE){
			html += '</div>';
			Swal.fire({
				html : html
			})
			return ;
		}
		if(data.PASSWORD){
			html += '<h3 >密码保护</h3>';
			var datas = data.PASSWORD;
			for(const d of datas){
				html += '<p>'+(d == '' ? '/' : d)+'<a href="javascript:void(0)" onclick="deleteSecurityPath(this)"><i class="fas fa-times ml-5" style="cursor:pointer"></i></a></p>'
			}
		}
		if(data.PRIVATE){
			html += '<h3 >私有访问</h3>';
			var datas = data.PRIVATE;
			for(const d of datas){
				html += '<p>'+(d == '' ? '/' : d)+'<a href="javascript:void(0)" onclick="deleteSecurityPath(this)"><i class="fas fa-times ml-5" style="cursor:pointer"></i></a></p>'
			}
		}
		html += '</div>';
		Swal.fire({
			html : html
		})
	});
}

function deleteSecurityPath(o){
	var path = o.previousSibling.nodeValue;
	if(path == '/') path = '';
	Swal.fire({
		  title: '确定删除吗？',
		  icon: 'warning',
		  showCancelButton: true,
		  confirmButtonColor: '#3085d6',
		  cancelButtonColor: '#d33'
		}).then((result) => {
		  if (result.value) {
			  	$.ajax({
					type : 'post',
					url : rootPath + 'console/securityPath/delete?path='+encodeURIComponent(path),
					success:function(data) {
						toast('删除成功');
						$file.reload();
						showSecurityPaths();
					}
				  })
		  }
		})
}