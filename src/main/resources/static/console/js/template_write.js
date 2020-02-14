(function() {
	var editor = CodeMirror.fromTextArea(document.getElementById("editor"), {
		lineNumbers : true,
		matchBrackets : true,
		autoCloseBrackets : true,
		styleActiveLine : true,
		autoCloseTags : true,
		mode:'htmlmixed',
		inputStyle : 'textarea',
		extraKeys:{
			'Ctrl-S':save,
			'Ctrl-P':preview
		}
	});
	
	var defaultTemplates = [];
	
	$.get(rootPath+'console/defaultTemplates',function(data){
		defaultTemplates = data;
		var html = '<div class="modal fade" id="defaultTemplatesModal" tabindex="-1" role="dialog" aria-hidden="true">';
		html += '<div class="modal-dialog" role="document">';
		html += '<div class="modal-content">';
		html += '<div class="modal-header">';
		html += '<h5 class="modal-title"></h5>';
		html += '<button type="button" class="close" data-dismiss="modal"';
		html += ' aria-label="Close">';
		html += '</button>';
		html += '</div>';
		html += '<div class="modal-body table-responsive ">';
		html += '<table class="table">';
		html += '<tr><th>路径</th><th>名称</th><th></th></tr>'
		for(const t of data){
			html += '<tr><td>'+(t.pattern||'')+'</td><td>'+(t.name||"")+'</td><td><a href="javascript:void(0)" data-p="'+(t.pattern||'')+'" data-n="'+(t.name||'')+'"><i class="fas fa-check"></i></a></td></tr>'
		}
		html += '</table>'
		html += '</div>';
		html += '<div class="modal-footer">';
		html += '<button type="button" class="btn btn-secondary"';
		html += ' data-dismiss="modal">关闭</button>';
		html += '</div>';
		html += '</div>'
		html += '</div>';
		html += '</div>';
		var div = document.createElement('div');
		div.innerHTML = html;
		document.body.appendChild(div);
		for(const ele of div.querySelectorAll('[data-p]')){
			ele.addEventListener('click',function(){
				var p = this.dataset.p;
				var n = this.dataset.n;
				for(const t of defaultTemplates){
					if((p != '' && t.pattern == p) || (n != '' && t.name == n)){
						Swal.fire({
							  title: '',
							  text: "确定替换内容？",
							  icon: 'warning',
							  showCancelButton: true,
							  confirmButtonColor: '#3085d6',
							  cancelButtonColor: '#d33'
							}).then((result) => {
							  if (result.value) {
								  $('#defaultTemplatesModal').modal('hide');
							   editor.setValue(t.content);
							  }
							})
						break;
					}
				}
			})
		}
	});

	var headers = document.querySelectorAll('.header');
	var h = 0;
	for(const header of headers){
		h += header.clientHeight;
	}
	editor.setSize(null,window.screen.height-h);
	
	var fs = new FileSelector(function(datas) {
		if (datas.length > 0) {
			var html = ''
			for(const data of datas){
				var ext = data.ext.toLowerCase();
				if(["png","jpeg","jpg","gif"].includes(ext)){
					html += '<img src="'+rootPath+data.path+'"/>';
				}
			}
			editor.replaceSelection(html);
		}
	});
	var toolbar = document.querySelector('#toolbar');
	if(fileEnable){
		toolbar.querySelector('[data-file]').addEventListener('click',function(){
			fs.open();
		});
	}
	toolbar.querySelector('[data-default-templates]').addEventListener('click',function(){
		$("#defaultTemplatesModal").modal('show')
	});
	toolbar.querySelector('[data-beautify]').addEventListener('click',function(){
		editor.setValue(html_beautify(editor.getValue()));
	})
	
	var back = document.getElementById('back-btn');
	back.addEventListener('click',function(){
		Swal.fire({
			  title: '确定要返回吗？',
			  text: "你会丢失没有保存的内容",
			  icon: 'warning',
			  showCancelButton: true,
			  confirmButtonColor: '#3085d6',
			  cancelButtonColor: '#d33'
			}).then((result) => {
			  if (result.value) {
			   	window.location.href = rootPath + 'console/templates?pageSize=10';
			  }
			})
	});
	
	
	var templateId;
	
	function save(){
		var data = {};
		data.content = editor.getValue();
		data.pattern = document.getElementById('pattern').value;
		data.name = document.getElementById('name').value;
		data.enable = document.getElementById('enable').checked;
		data.allowComment = document.getElementById('allowComment').checked;
		data.description = document.getElementById('description').value;
		var url = templateId ? rootPath + 'console/templates/'+templateId+'/update' : rootPath + 'console/template/save';
		$.ajax({
			type : 'post',
			url : url,
            contentType: 'application/json',
			data:JSON.stringify(data),
			success:function(data) {
				if(!templateId)
					templateId = data;
				toast('保存成功');
				clearFormError(document.body);
			},
			error:function(r){
				r.handled = true;
				clearFormError(document.body);
	        	var errors = r.responseJSON && r.responseJSON.errors;
	    		if(errors){
					if(errors[0].field){
	    				displayFormError(errors,document.body);
	    			} else {
	    				toastErrors(errors);
	    			}
	    		}
	        }
		  })
	}
	function preview(){
		var data = {};
		data.content = editor.getValue();
		data.pattern = document.getElementById('pattern').value;
		data.name = document.getElementById('name').value;
		data.enable = true;
		$.ajax({
			type : 'post',
			url : rootPath + 'console/template/preview',
            contentType: 'application/json',
			data:JSON.stringify(data),
			success:function(data) {
				if(data.pattern){
					if(data.definitely){
						Swal.fire({
							'title':'点击访问地址',
							'html':'<a target="_blank" href="'+data.pattern+'">'+data.pattern+'</a>'
						})
					} else {
						Swal.fire({
							'title':'模板地址不确定，请手动访问',
							'html':data.pattern
						})
					}
				} else {
					toast('请访问调用该片段的模板');
				}
				clearFormError(document.body);
			},
			error:function(r){
				r.handled = true;
				clearFormError(document.body);
	        	var errors = r.responseJSON && r.responseJSON.errors;
	    		if(errors){
					if(errors[0].field){
	    				displayFormError(errors,document.body);
	    			} else {
	    				toastErrors(errors);
	    			}
	    		}
	        }
		  })
	}
	document.getElementById('submit-btn').addEventListener('click',function(){
		save();
	});
	
	
	
	
	
	$.get(rootPath+'static/console/doc/index.html',function(data){
		document.querySelector('#doc-content').innerHTML = data;
		var refHeadings = [];
		for(const heading of document.querySelectorAll('h4')){
			if(heading.textContent == '引用')
				refHeadings.push(heading);
		}
		var parser = new DOMParser();
		var queryIndex = [];
		for(const refHeading of refHeadings){
			var tagStr = refHeading.nextElementSibling.textContent.trim();
			var node = parser.parseFromString(tagStr, "text/html").body.firstElementChild;
			if(node != null){
				var name = node.tagName;
				if(name === 'DATA'){
					queryIndex.push({
						name : node.getAttribute('name'),
						element : refHeading
					})
				} else {
					queryIndex.push({
						name : name.toLowerCase(),
						element : refHeading
					})
				}
			}
		}
		queryIndex.push({
			name : 'times',
			element : document.getElementById('java8%E6%97%B6%E9%97%B4%E8%BE%85%E5%8A%A9')
		})
		queryIndex.push({
			name : 'session',
			element : document.getElementById('%E5%88%A4%E6%96%AD%E7%94%A8%E6%88%B7%E6%98%AF%E5%90%A6%E7%99%BB%E5%BD%95')
		})
		queryIndex.push({
			name : 'messager',
			element : document.getElementById('%E8%8E%B7%E5%8F%96%E4%BF%A1%E6%81%AFcode%E5%AF%B9%E5%BA%94%E7%9A%84%E5%86%85%E5%AE%B9')
		})
		queryIndex.push({
			name : 'pagings',
			element : document.getElementById('%E8%BE%85%E5%8A%A9%E5%88%86%E9%A1%B5')
		})
		queryIndex.push({
			name : 'urls',
			element : document.getElementById('%E9%93%BE%E6%8E%A5%E8%BE%85%E5%8A%A9')
		})
		queryIndex.push({
			name : 'jsoups',
			element : document.getElementById('jsoup%E8%BE%85%E5%8A%A9')
		})
		queryIndex.push({
			name : 'formats',
			element : document.getElementById('%E6%A0%BC%E5%BC%8F%E5%8C%96%E8%BE%85%E5%8A%A9')
		})
		
		var container = document.querySelector('#search-result-container');
		var input = document.querySelector('#search').parentElement.previousElementSibling;
		
		function showQueryResult(){
			var query = input.value.trim().toLowerCase();
			var array = query.split('');
			if(array.length==0){
				container.style.display = 'none';
				return ;
			}
			var results = [];
			out:for(const index of queryIndex){
				for(const ch of array){
					if(!index.name.toLowerCase().includes(ch)){
						continue out;		
					}
				}
				results.push(index);
			}
			if(results.length == 0){
				container.style.display = 'none';
				return ;
			}
			var ul = container.querySelector('ul');
			ul.innerHTML = '';
			for(const result of results){
				var li = document.createElement('li');
				li.innerHTML = result.name;
				li.addEventListener('click',function(){
					var ele = result.element;
					ele.scrollIntoView(true);
					ul.innerHTML = '';
					container.style.display = 'none';
				})
				ul.appendChild(li);
			}
			container.style.display = '';
		}
		
		input.addEventListener('input',showQueryResult);
		input.addEventListener('focus',showQueryResult);
	})
})();