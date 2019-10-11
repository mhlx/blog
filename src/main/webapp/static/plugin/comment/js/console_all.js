 var cmt = (function() {
	 var status = '';
	 $("#onlyCheck").change(function(){
		if($(this).is(":checked")){
			status ='CHECK';
		} else{
			status = '';
		}
		loadComment({page:1});
	 });
	 var config = {};
	 var c = $("#container");
	 	$("<style type='text/css'> .media-content{word-break: break-all;} .media-content img {max-width: 100%; height: auto;}  </style>").appendTo("head");
        var moduleId;
        var parentId;
        var moduleType;
        var commentFunction;
        var modal = '<div class="modal" tabindex="-1" role="dialog" id="comment-modal">';
        modal += '<div class="modal-dialog" role="document">';
        modal += '<div class="modal-content">';
        modal += '<div class="modal-header">';
        modal += '<h5 class="modal-title">评论</h5>';
        modal += '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>';
        modal += '</div>';
        modal += '<div class="modal-body">';
        modal += '<div class="alert alert-danger" style="display: none" id="comment-error-tip"></div>';
        modal += '<form >';
        modal += '<div class="form-group row" >';
        modal += '<label class="col-sm-2 control-label">内容</label>';
        modal += '<div class="col-sm-10">';
        modal += '<p><span data-smiley style="cursor:pointer;font-size:20px">😂</span><span data-smiley style="cursor:pointer;font-size:20px">😄</span><span data-smiley style="cursor:pointer;font-size:20px">😭 </span><span data-smiley style="cursor:pointer;font-size:20px">😍</span><span data-smiley style="cursor:pointer;font-size:20px">😘</span><span data-smiley style="cursor:pointer;font-size:20px">😝</span><span data-smiley style="cursor:pointer;font-size:20px">🤬</span><span data-smiley style="cursor:pointer;font-size:20px">😴</span><span data-smiley style="cursor:pointer;font-size:20px">👿</span></p>'
        modal += '<textarea class="form-control" id="content" style="height: 270px" placeholder="必填"></textarea>';
        modal += '</div>';
        modal += '</div>';
        
        modal += '<div class="form-group row" style="display:none" id="captchaContainer">';
        modal += '<label class="col-sm-2 control-label"></label>';
        modal += '<div class="col-sm-10">';
        modal += '<img src="'+basePath+'captcha" class="img-fluid" id="captcha-img"/>'
        modal += ' <input type="text" class="form-control" id="comment-captcha" placeholder="验证码">';
        modal += '</div>';
        modal += '</div>';

        modal += '</form>';
        modal += '</div>';
        modal += '<div class="modal-footer">';
        modal += '<button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>';
        modal += ' <button type="button" class="btn btn-primary" id="comment-btn">提交</button>';
        modal += '</div>';
        modal += '</div>';
        modal += '</div>';
        modal += '</div>';
        $(modal).appendTo($('body'));
        
      
        var loadUserInfo = function() {
            var name = '';
            var email = '';
            var website = '';
            if (window.localStorage) {
                if (localStorage.commentName) {
                    name = localStorage.commentName;
                }
                if (localStorage.commentEmail) {
                    email = localStorage.commentEmail;
                }
                if (localStorage.commentWebsite) {
                    website = localStorage.commentWebsite;
                }
            }
            $("#nickname").val(name);
            $("#email").val(email);
            $("#website").val(website);
        }
        var storeUserInfo = function(name, email, website) {
            if (window.localStorage) {
                if (name && name != '')
                    localStorage.commentName = name;
                else
                    localStorage.commentName = "";
                if (email && email != '')
                    localStorage.commentEmail = email;
                else
                    localStorage.commentEmail = "";
                if (website && website != '')
                    localStorage.commentWebsite = website;
                else
                    localStorage.commentWebsite = "";
            }
        }
        var getAvatar = function(c) {
            if (c.gravatar) {
                return "https://secure.gravatar.com/avatar/" + c.gravatar;
            }
            return basePath + 'static/img/guest.png';
        }

        var getUsername = function(c) {
            if (c == null || !c) {
                return '';
            }
            var username = '';
            if (c.admin) {
                username = '<i class="fas fa-user" title="管理员" style="color:red"></i></span>&nbsp;' +
                    c.nickname
            } else {
                username = c.nickname
            }
            username += '&nbsp;&nbsp;<small><a href="###" data-clipboard-text="'+c.ip+'">'+c.ip+'</a></small>'
            return username;
        }
        var isLogin = true;
        var loadComment = function(config) {
            var page = config.page;
            if (!page || page < 1) {
                page = 1;
            }
            c.html('<img src="'+basePath+'static/img/loading.gif" class="img-fluid mx-auto"/>')
            $.ajax({
            	url : root + 'api/console/comments',
            	data:{currentPage:page,status:status},
            	success:function(data){
            		var page = data;
                    config.pageSize = page.param.pageSize;
                    config.currentPage = page.param.currentPage;
                    var html = '';
                    if (page.datas.length > 0) {
                        for (var i = 0; i < page.datas.length; i++) {
                            var data = page.datas[i];
                            html += '<div class="media">';
                            if(data.admin){
                            	 html += '<a href="javascript:void(0)"> <img class="mr-3" src="' +
                                 getAvatar(data) +
                                 '" style="width:24px;height:24px"></a>';
                            }else{
                            	var website = data.website;
                                if(website){
                                	 html += '<a href="'+website+'" target="_blank" rel="external nofollow"> <img class="mr-3" src="' +
                                     getAvatar(data) +
                                     '" style="width:24px;height:24px"></a>';
                                }else{
                                	html += '<a href="javascript:void(0)"> <img class="mr-3" src="' +
                                    getAvatar(data) +
                                    '" style="width:24px;height:24px"></a>';
                                }
                            }
                            html += '<div class="media-body">';
                            var time = moment(data.commentDate)
                                .format('YYYY-MM-DD HH:mm');
                            html += '<h6 class="mt-0">' +
                                getUsername(data) + '</h6>';
                            html += '<div class="media-content">';
                            html += data.content;
                            html += '</div>'
                            html += '<p><small>' + time +
                                '</small>';
                            if (isLogin) {
                            	if(!data.admin && !data.ban){
                                	html += '<a href="javascript:void(0)" data-ban="'+data.id+'"   style="margin-left:10px"><i class="fas fa-ban"></i></a>';
                            	}
                                html += '<a href="javascript:void(0)" data-del="'+data.id+'"  style="margin-left:10px"><i class="far fa-trash-alt"></i></a>';
                            }
                            if (data.status == 'CHECK') {
                                html += '<a href="javascript:void(0)" data-check="'+data.id+'"   style="margin-left:10px"><i class="fas fa-check"></i></a>';
                            }
                            html += '<a href="'+root+'comment/link/'+data.commentModule.module+'/'+data.commentModule.id+'"  style="margin-left:10px"><i class="fas fa-location-arrow"></i></a>';
                            html += '</p>';
                            html += '</div>';
                            html += '</div>';
                        }
                    }
                    if (page.totalPage > 1) {
                        html += '<nav >';
                        html += '<ul  class="pagination flex-wrap">';
                        html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1"><span aria-hidden="true">&laquo;</span></a></li>';
                        for (var j = page.listbegin; j < page.listend; j++) {
                            if (j == page.currentPage) {
                                html += '<li class="page-item active"><a class="page-link" href="javascript:void(0)" >' +j + '</a></li>';
                            } else {
                                html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'+j+'">'+j+'</a></li>';
                            }
                        }
                        html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'+page.totalPage+'"><span aria-hidden="true">&raquo;</span></a></a></li>';
                        html += '</ul>';
                        html += '</nav>';
                    }
                    c.html(html);
                    var afterLoad = config.afterLoad;
                    if(afterLoad){
                    	afterLoad(page);
                    }
            	},
            	error : function(jqXHR){
            		Swal.fire('获取评论失败',$.parseJSON(jqXHR.responseText).error,'error');
            	}
            })
        }
        
        var commentConfig;
        
        $.ajax({
        	
        	url : root + 'api/commentConfig',
        	async : false,
        	success:function(data){
        		commentConfig = data;
        	},
        	error : function(jqXHR){
        		Swal.fire('获取评论配置失败',$.parseJSON(jqXHR.responseText).error,'error');
        	}
        	
        });
        
        var editor ;
        
        loadCSS = function(href) {

        	  var cssLink = $("<link>");
        	  $("head").append(cssLink); 

        	  cssLink.attr({
        	    rel:  "stylesheet",
        	    type: "text/css",
        	    href: href
        	  });

        };
        
        editor = {
    			
        		get:function(){
        			return   $("#content").val();
        		}	,
        		clear:function(){
        			$("#content").val('');
        		}
        		
        	}
        
        
        loadComment({});
        c.on("click","[data-page]",function(){
        	config.page = parseInt($(this).attr('data-page'));
        	loadComment(config);
    	}); 
        c.on('click',"[data-del]",function(){
        	var id = $(this).data('del');
        	Swal.fire({
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
						url : root + 'api/console/comment/'+id,
						success:function(data) {
							Swal.fire('删除成功','评论已经被删除','success');
							loadComment(config);
						},
						error:function(jqXHR, textStatus, errorThrown) {
							var data = $.parseJSON(jqXHR.responseText);
							Swal.fire('删除失败',data.error,'error');
						}
					  })
				  }
				});
        });
        c.on('click',"[data-check]",function(){
        	var id = $(this).data('check');
        	Swal.fire({
				  title: '你确定吗？',
				  type: 'warning',
				  showCancelButton: true,
				  confirmButtonColor: '#3085d6',
				  cancelButtonColor: '#d33',
				  confirmButtonText: '确定!',
				  cancelButtonText: '取消'
				}).then((result) => {
				  if (result.value) {
					  $.ajax({
						type : 'PATCH',
						url : root + 'api/console/comment/'+id+"?status=NORMAL",
						success:function(data) {
							Swal.fire('审核通过','','success');
							loadComment(config);
						},
						error:function(jqXHR, textStatus, errorThrown) {
							var data = $.parseJSON(jqXHR.responseText);
							Swal.fire('审核失败',data.error,'error');
						}
					  })
				  }
				});
        });
        
        c.on('click','[data-ban]',function(){
        	var id = $(this).data('ban');
        	Swal.fire({
				  title: '你确定吗？',
				  type: 'warning',
				  showCancelButton: true,
				  confirmButtonColor: '#3085d6',
				  cancelButtonColor: '#d33',
				  confirmButtonText: '禁止!',
				  cancelButtonText: '取消'
				}).then((result) => {
				  if (result.value) {
					  $.ajax({
						type : 'POST',
						url : root + 'api/console/comment/blacklistItem',
						data : {"id":id},
						success:function(data) {
							Swal.fire('禁止成功','该IP已经被禁止评论','success');
							loadComment(config);
						},
						error:function(jqXHR, textStatus, errorThrown) {
							var data = $.parseJSON(jqXHR.responseText);
							Swal.fire('禁止失败',data.error,'error');
						}
					  })
				  }
				});
        });
    })();