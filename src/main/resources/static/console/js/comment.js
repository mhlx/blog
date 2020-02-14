var Comment = (function(){
	
	$(document).ajaxError(function(e,r) {
		if(r.handled === true)
			return ;
		var errors = r.responseJSON && r.responseJSON.errors;
		if(errors)
			toastErrors(errors);
	});
	
	function Comment(config){
		var container = document.getElementById(config.container);
		container.innerHTML = getTabsHtml();
		this.navs = container.querySelectorAll('.nav-link');
		this.tabs = container.querySelectorAll('.tab-panel');
		var inputs = this.tabs[1].querySelectorAll('input');
		inputs[0].value='Administrator';//just for approve validate
		for(const input of inputs)
			input.parentElement.style.display = 'none';
		this.commentTabs = this.tabs[0];
		this.container = container;
		var pageSize = config.pageSize;
		if(typeof pageSize === 'undefined'){
			pageSize = 10;
		}
		this.pageSize = pageSize;
		this.load(1);
		bindEvent(this);
	}
	
	function calcPaging(step,data){
		var offset = step%2 == 0 ? step/2 : (step-1)/2;
		var first,last;
		if(data.currentPage - offset < 1){
			first = 1;
			last = step;
		} else {
			first = data.currentPage - offset;
			last = data.currentPage + (step%2 == 0?offset-1:offset);
		}
		last = Math.min(last,data.totalPage);
		if(last - first + 1 < step){
			if(data.totalPage > step){
				first = last - step+1;
			} else {
				first = 1
			}
		}
		return {first:first,last:last};
	}
	
	function processPaging(data,comment){
		var paging = comment.tabs[0].querySelector('.pagination');
		if(paging != null) paging.remove();
		if (data.totalPage > 1) {
			paging = document.createElement('ul');
			paging.setAttribute('class','pagination pagination-simple  justify-content-center');
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
					var page = parseInt(e.target.dataset.page);
					comment.load(page);
				}
			})
			comment.tabs[0].appendChild(paging);
		}
	}
	function bindEvent(comment){
		comment.tabs[0].addEventListener('click',function(e){
			var target = e.target;
			while(target != null){
				if(target.hasAttribute('data-action')){
					break;
				}
				target = target.parentElement;
			}
			if(target != null){
				var id = target.dataset.target;
				var action = target.dataset.action;
				for(const data of comment.cache){
					if(data.id == id){
						if(action == 'conversation'){
							$.get(rootPath+'comments/'+id+'/conversation',function(datas){
								var html = '';
								for(const data of datas){
									html += processOne(data,comment,true);
								}
								Swal.fire({
									title:'',
									html:html,
									onOpen:function(dom){
										dom.querySelector('.swal2-content').style['text-align'] = 'left'
									}
								})
							})
						}
						if(action == 'edit'){
							$.get(rootPath+'comments/'+id,function(data){
								toCommentBox(null,comment);
								var inputs = comment.tabs[1].querySelectorAll('input');
								inputs[2].value = id;
								if(comment.editor instanceof Element){
									comment.editor.value = data.content;
								} else {
									comment.editor.setValue(data.content);
								}
							})
						}
						if(action == 'reply'){
							toCommentBox(data,comment,target.dataset.modulename,target.dataset.moduleid);
						}
						if(action == 'check')
							$.ajax({
								type : 'post',
								url : rootPath + 'console/comments/'+id+'/check',
								success:function(data) {
									toast('审核成功');
									comment.reload();
								}
							  })
						if(action == 'delete'){
							Swal.fire({
								  title: '确定删除吗？',
								  text: "评论以及回复将会被一并删除",
								  icon: 'warning',
								  showCancelButton: true,
								  confirmButtonColor: '#3085d6',
								  cancelButtonColor: '#d33'
								}).then((result) => {
								  if (result.value) {
									  	$.ajax({
											type : 'post',
											url : rootPath + 'console/comments/'+id+'/delete',
											success:function(data) {
												toast('删除成功');
												comment.reload();
											}
										  })
								  }
								})
						}
						if(action == 'forbidden'){
							Swal.fire({
								  title: '确定封禁该IP吗？',
								  text: "该IP将无法使用任何服务",
								  icon: 'warning',
								  showCancelButton: true,
								  confirmButtonColor: '#3085d6',
								  cancelButtonColor: '#d33'
								}).then((result) => {
								  if (result.value) {
									  $.ajax({
											type : 'post',
											url : rootPath + 'console/blackip/save?ip='+data.ip,
											success:function(data) {
												toast('操作成功');
												comment.reload();
											}
										  })
								  }
								})
						}
						break;
					}
				}
			}
		})
		var addComment = function(captcha){
			var data = {};
			if(comment.target){
				data.parent = {id : comment.target.id};
			}
			var inputs = comment.tabs[1].querySelectorAll('input');
			data.nickname =  'admin';
			data.email = inputs[1].value;
			data.content = comment.editor.getValue();
			var url = inputs[2].value == '' ? rootPath + 'commentModule/'+comment.name+'/'+comment.id+'/comment/add' : rootPath + 'console/comments/'+inputs[2].value+'/update';
			if(captcha){
				url += "?captcha="+captcha;
			}
			$.ajax({
				type : 'post',
				url : url,
	            contentType: 'application/json',
				data:JSON.stringify(data),
				success:function(data) {
					toast('保存成功');
					comment.editor.setValue('');
					toComment(comment);
					clearFormError(comment);
				},
				error:function(r){
					r.handled = true;
					clearFormError(comment);
		        	var errors = r.responseJSON && r.responseJSON.errors;
		    		if(errors){
    					if(errors[0].field){
		    				displayFormError(errors,comment);
		    			} else {
		    				if(errors[0].code == 'captcha.invalid'){
		    					Swal.fire({
		    						title:'请输入验证码',
		    						html : '<img src="'+rootPath+'captcha?time='+new Date().getTime()+'"/><input type="text" class="swal2-input"/>',
		    						onClose:function(dom){
		    							var value = dom.querySelector('input').value;
		    							if(value.trim() == ''){
		    								return ;
		    							}
		    							addComment(value);
		    						}
		    					})
		    				} else {
		    					toastErrors(errors);
		    				}
		    			}
		    		}
		        }
			  })
		}
		comment.tabs[1].querySelectorAll('.sbt')[0].addEventListener('click',function(){
			addComment();
		});
		comment.tabs[1].querySelectorAll('.sbt')[1].addEventListener('click',function(){
			comment.tabs[1].style.display='none'
			comment.tabs[0].style.display=''
		});
		document.querySelector("#checkingBox").addEventListener('change',function(){
			comment.load(1);
		});
	}
	
	function displayFormError(errors,comment){
		for(const error of errors){
			var field = error.field;
			var target = comment.tabs[1].querySelector('[data-invalid-target="'+field+'"]');
			if(target != null){
				target.previousElementSibling.classList.add('is-invalid');
				target.innerHTML = error.message;
				target.style.display = 'block'//for content error display 
			}
		}
	}
	
	function clearFormError(comment){
		var targets = comment.tabs[1].querySelectorAll('[data-invalid-target]');
		for(const target of targets){
			target.previousElementSibling.classList.remove('is-invalid');
			target.innerHTML = '';
			target.style.display = ''
		}
	}
	
	function toComment(comment){
		comment.tabs[1].style.display = 'none';
		comment.tabs[0].style.display = '';
		comment.load(1);
	}
	
	function toCommentBox(data,comment,name,id){
		comment.tabs[1].querySelectorAll('input')[2].value = '';
		clearFormError(comment);
		comment.target = data;
		comment.id = id;
		comment.name = name;
		comment.tabs[0].style.display = 'none';
		var p = comment.tabs[1];
		p.style.display = ''
		if(!comment.editor){
			comment.editor = p.querySelector('textarea');
			LazyHeather.load(function(Heather){
				Heather.lazyRes.mermaid_js = rootPath + 'static/heather/js/mermaid.min.js';
				Heather.lazyRes.katex_css = rootPath + 'static/heather/katex/katex.min.css';
				Heather.lazyRes.katex_js = rootPath + 'static/heather/katex/katex.min.js';
				var textarea = comment.editor;
				comment.editor = new Heather(textarea,{
					editor:{
						lineNumbers:false
					},
					disablePreviewCloseBtn:true
				});
				var eye = document.createElement('i');
				eye.setAttribute('class','far fa-eye');
				eye.setAttribute('style','margin-left:5px');
				eye.addEventListener('click',function(){
					var preview = eye.classList.contains('fa-eye');
					comment.editor.setPreview(preview);
					if(preview){
						eye.classList.remove('fa-eye');
						eye.classList.add('fa-eye-slash');
					} else {
						eye.classList.remove('fa-eye-slash');
						eye.classList.add('fa-eye');
					}
				});
				textarea.parentElement.firstElementChild.append(eye)
			})
		}
		var rd = p.querySelector('.reply-detail');
		if(data != null){
			rd.innerHTML = '<fieldset class="form-fieldset" style="padding-bottom:0rem"><legend style="font-size:12px">回复</legend>'+processOne(data,comment,true)+'</fieldset>';
			rd.style.display = '';
		} else {
			rd.innerHTML = '';rd.style.display = 'none';
		}
		try{
			comment.editor.setValue('');
		}catch(_e){}
	}
	
	function toastErrors(errors){
		var html = '';
		if(errors.length > 1){
			html += '<ol>';
			for(const error of errors){
				html += '<li style="text-align:left">'+error.message+'</li>';
			}
			html += '</ol>';
		} else {
			html += errors[0].message;
		}
		toast(html,'error');
	}
	
	
	function getActiveTab(comment){
		for(var i=0;i<comment.tabs.length;i++){
			var tab = comment.tabs[i];
			if(tab.classList.contains('active')){
				return i;
			}
		}
		return -1;
	}
	
	Comment.prototype.reload = function(){
		var pageItem = this.tabs[0].querySelector('.page-item.active');
		if(pageItem == null){
			this.load(1);
		} else {
			this.load(parseInt(pageItem.innerText));
		}
	}
	
	Comment.prototype.loadTarget = function(id){
		var pageSize = this.pageSize;
		var url = rootPath + 'commentModule/'+this.name+'/'+this.id+'/comments';
		var me = this;
		$.ajax({
			type: "get",
	        url: url,
	        data:{pageSize:pageSize,contain:id},
	        success:function(data){
	        	processData(data,me);
	        },
	        error:function(r){
	        	var errors = r.responseJSON && r.responseJSON.errors;
	    		if(errors)
	    			toastErrors(errors);
	        }
		})
	}
	
	Comment.prototype.load = function(page){
		var pageSize = this.pageSize;
		var url = rootPath + 'console/comments?checking='+document.querySelector("#checkingBox").checked;
		var me = this;
		$.ajax({
			type: "get",
	        url: url,
	        data:{currentPage:page,pageSize:pageSize},
	        success:function(data){
	        	processData(data,me);
	        },
	        error:function(r){
	        	var errors = r.responseJSON && r.responseJSON.errors;
	    		if(errors)
	    			toastErrors(errors);
	        }
		})
	}
	
	function getTabsHtml(){
		var html = '<div class="card">';
		html += '<div class="card-body">';
		
		html += '<div class="tab-panel active">';
		html += ' </div>';
		
		html += '<div class="tab-panel" style="display:none">';
		html += '<div class="mb-3 reply-detail" style="display:none"></div>'
		html += '<div class="mb-3">';
		html += '<label class="form-label">昵称<span class="form-required">*</span></label>';
		html += '<input type="text" class="form-control " autocomplete="off">';
		html += '<div class="invalid-feedback" data-invalid-target="nickname"></div>';
        html += '</div>';
        html += '<div class="mb-3">';
		html += '<label class="form-label">内容<span class="form-required">*</span></label>';
		html += '<textarea class="form-control"></textarea>';
		html += '<div class="invalid-feedback" data-invalid-target="content"></div>';
        html += '</div>';
        html += '<div class="mb-3">';
		html += '<label class="form-label">邮箱<small>(用于显示头像)</small></label>';
		html += '<input type="text" class="form-control" autocomplete="off">';
		html += '<div class="invalid-feedback" data-invalid-target="email"></div>';
        html += '</div>';
        html += '<input type="hidden" autocomplete="off">';
        html += '<button type="button" class="btn btn-primary ml-auto float-right sbt">提交</button>';
        html += '<button type="button" class="btn btn-danger ml-auto float-right sbt" style="margin-right:30px">返回</button>';
		html += ' </div>';
		
		html += '</div>';
		html += '</div>';
		return html;
	}
	
	function processData(data,comment){
		var html = '';
		var datas = data.datas;
		comment.cache = datas;
		for(const data of datas){
			html += processOne(data,comment);
		}
		if(html == ''){
			html += '<div class="card-body">没有评论</div>'
		}
		comment.commentTabs.innerHTML = html;
		processPaging(data,comment);
	}
	
	
	function processOne(data,comment,removeBar){
		var avatar = data.gravatar ? 'https://secure.gravatar.com/avatar/'+data.gravatar : rootPath + 'static/img/guest.png';
		var html = '<div class="d-flex mb-5">';
		html += '<div class="mr-4">';
		html += '<span class="avatar" style="background-image: url('+avatar+')">';
		html += '<span class="badge bg-green"></span>';
		html += '</span>';
		html += '</div>';
		html += '<div class="flex-fill">';
		var nickname = data.admin ? '<i class="far fa-star"></i>&nbsp;' + data.nickname : data.nickname;
		html += '<div class="d-flex mt-n1"><h5 class="m-0">'+nickname+'</h5><div class="ml-auto small text-muted">'+getTime(data.createTime)+'</div></div>';
		if(data.parent){
			var pnickname = data.parent.admin ? '<i class="far fa-star"></i>&nbsp;' + data.parent.nickname : data.parent.nickname;
			html += '<div style="font-size:10px;padding:5px"><i class="fas fa-share"></i>&nbsp;&nbsp;'+pnickname+'</div>';
		}
		html += '<div class="mb-2 markdown-body"  style="word-break:break-all">'+data.content+'</div>';
		if(removeBar !== true){
			html += '<div class="small">';
			html += '<a href="javascript:void(0)" class="text-muted" data-action="delete" data-target="'+data.id+'"><i class="fas fa-trash-alt"></i></a><span class="mr-1"></span> ';
			if(data.blackIp === false && !data.admin)
				html += '<a href="javascript:void(0)" class="text-muted" data-action="forbidden" data-target="'+data.id+'"><i class="fas fa-user-slash"></i></a><span class="mr-1"></span> ';
			if(data.checking)
				html += '<a href="javascript:void(0)" class="text-muted" data-action="check" data-target="'+data.id+'"><i class="fas fa-check"></i></a><span class="mr-1"></span> ';
			html += '<a href="javascript:void(0)" class="text-muted" data-action="reply" data-moduleid="'+data.module.id+'" data-modulename="'+data.module.name+'" data-target="'+data.id+'"><i class="fas fa-reply"></i></a><span class="mr-1"></span> ';
			if(data.parent){
				html += '<span class="mr-1"></span> <a href="javascript:void(0)" data-action="conversation" data-target="'+data.id+'" class="text-muted"><i class="fas fa-comment"></i></a><span class="mr-1"></span>';
			}
			
			if(data.admin)
				html += '<span class="mr-1"></span> <a href="javascript:void(0)" data-action="edit" data-target="'+data.id+'" class="text-muted"><i class="fas fa-edit"></i></a>';
			
			html += '</div>';
		}
		html += '</div>';
		html += '</div>';
		return html;
	}
	
	function getTime(create){
		var time = create[0]
					+'-'
					+(create[1]+"").padStart(2,'0')
					+'-'
					+(create[2]+"").padStart(2,'0')
					+' '
					+(create[3]+"").padStart(2,'0')
					+':'
					+(create[4]+"").padStart(2,'0');
		return time;
	}
	
	
	return Comment;
	
})();