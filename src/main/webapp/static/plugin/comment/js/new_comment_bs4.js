 var cmt = (function(config) {
	 	$("<style type='text/css'> .media-content{word-break: break-all;} .media-content img {max-width: 100%; height: auto;}  </style>").appendTo("head");
        var commentFunction;
        
        
        var insertAtCursor = function(myField, myValue) {
            if (document.selection) {
                myField.focus();
                sel = document.selection.createRange();
                sel.text = myValue;
            }
            else if (myField.selectionStart || myField.selectionStart == '0') {
                var startPos = myField.selectionStart;
                var endPos = myField.selectionEnd;
                myField.value = myField.value.substring(0, startPos)
                    + myValue
                    + myField.value.substring(endPos, myField.value.length);
            } else {
                myField.value += myValue;
            }
        }
        
        
        $("#captcha-img").click(function(){
        	$(this).attr('src',basePath+'/captcha?time='+$.now());
        });
        
        var confirmModal = '';
        confirmModal += '<div class="modal" tabindex="-1" role="dialog">';
        confirmModal += '<div class="modal-dialog" role="document">';
        confirmModal += '<div class="modal-content">';
        confirmModal += '<div class="modal-header">';
        confirmModal += '<h5 class="modal-title">确认</h5>';
        confirmModal += ' <button type="button" class="close" data-dismiss="modal" aria-label="Close">';
        confirmModal += ' <span aria-hidden="true">&times;</span>';
        confirmModal += '</button>';
        confirmModal += '</div>';
        confirmModal += '<div class="modal-body">';
        confirmModal += '<p>确定要这么做吗？</p>';
        confirmModal += '</div>';
        confirmModal += '<div class="modal-footer">';
        confirmModal += '<button type="button" class="btn btn-primary" data-confirm>确定</button>';
        confirmModal += '<button type="button" class="btn btn-secondary" data-dismiss="modal">取消</button>';
        confirmModal += '</div>';
        confirmModal += '</div>';
        confirmModal += '</div>';
        confirmModal += '</div>';
        
        $(confirmModal).appendTo($('body'));
        
        var $confirmModal = $(confirmModal);
        
        
        var doConfirm =  function(text,cb){
        	$confirmModal.find('.modal-body p').text(text);
        	$confirmModal.find('[data-confirm]').unbind('click').click(function(){
        		try{
        			cb();
        		}finally{
        			$confirmModal.modal('hide');
        		}
        	});
        	$confirmModal.modal('show');
        }
        
        var alertModal = '';
        alertModal += '<div class="modal" tabindex="-1" role="dialog">';
        alertModal += '<div class="modal-dialog" role="document">';
        alertModal += '<div class="modal-content">';
        alertModal += '<div class="modal-header">';
        alertModal += '<h5 class="modal-title">提示</h5>';
        alertModal += ' <button type="button" class="close" data-dismiss="modal" aria-label="Close">';
        alertModal += ' <span aria-hidden="true">&times;</span>';
        alertModal += '</button>';
        alertModal += '</div>';
        alertModal += '<div class="modal-body">';
        alertModal += '<p></p>';
        alertModal += '</div>';
        alertModal += '<div class="modal-footer">';
        alertModal += '<button type="button" class="btn btn-secondary" data-dismiss="modal">关闭</button>';
        alertModal += '</div>';
        alertModal += '</div>';
        alertModal += '</div>';
        alertModal += '</div>';
        
        $(alertModal).appendTo($('body'));
        
        var $alertModal = $(alertModal);
        
        
        var doAlert =  function(text){
        	$alertModal.find('.modal-body p').text(text);
        	$alertModal.modal('show');
        }
        
        var conversationsModal = '<div class="modal " id="conversationsModal" tabindex="-1" role="dialog" >';
        conversationsModal += '<div class="modal-dialog" role="document">';
        conversationsModal += '<div class="modal-content">';
        conversationsModal += '<div class="modal-header">';
        conversationsModal += '<h4 class="modal-title">对话</h4>';
        conversationsModal += '</div>';
        conversationsModal += '<div class="modal-body" id="conversationsBody">';
        conversationsModal += '<div class="tip"></div>';
        conversationsModal += '</div>';
        conversationsModal += '<div class="modal-footer">';
        conversationsModal += '<button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>';
        conversationsModal += '</div>';
        conversationsModal += '</div>';
        conversationsModal += '</div>';
        conversationsModal += '</div>';
        $(conversationsModal).appendTo($('body'));
        var conversationsModal = $("#conversationsModal");
        
        
        var queryConversations = function(id, moduleId,moduleType) {
        	$.ajax({
        		url : actPath + '/api/'+moduleType+'/' + moduleId + '/comment/' + id +
                '/conversation',
                success:function(data){
                	 var html = '';
                     for (var i = 0; i < data.length; i++) {
                         var c = data[i];
                         var p = i == 0 ? null : data[i - 1];
                         html += '<div class="media">';
                         html += '<img class="mr-3"  src="' +
                             getAvatar(c) +
                             '" data-holder-rendered="true" style="width: 32px; height: 32px;">';
                         html += '<div class="media-body"  >';
                         var username = getUsername(c);
                         var user = '<strong>' + username +
                             '</strong>';
                         var p_username = getUsername(p);
                         html += '<h6 class="mr-0">' + user +
                             '</h6>';
                         if (p) {
                             var pnickname = getUsername(p);
                             html += '<small style="margin-right:10px">回复' +
                                 pnickname + ':</small>';
                         }
                         html += '<div class="media-content">'
                         html += c.content;
                         html += '</div>';
                         html += '<p>' +
                             moment(c.commentDate)
                             .format('YYYY-MM-DD HH:mm') +
                             '&nbsp;&nbsp;&nbsp;</p>';
                         html += '</div>';
                         html += '</div>';
                     }
                     $("#conversationsBody").html(html);
                     conversationsModal.modal('show');
                },
                error:function(jqXHR){
                	var message = $.parseJSON(jqXHR.responseText).error;
	             	Swal.fire('获取会话失败',message,'error');
	            }
        	})
        }
        
        var checkComment = function(id,callback){
        	doConfirm("确定要审核通过吗？", function() {
                 $.ajax({
                     type: "patch",
                     url: basePath + "/api/console/comment/" + id+"?status=NORMAL",
                     crossDomain: true,
                     success: function(data) {
                        if(callback){
                        	callback();
                        }
                     },
                     error:function(jqXHR){
                     	var message = $.parseJSON(jqXHR.responseText).error;
                     	Swal.fire('审核失败',message,'error');
                     }
                 });
             });
        }
        
        var removeComment =  function(id, callback) {
            doConfirm(
                    "确定要删除该评论吗？",
                    function() {
                        $.ajax({
                            type: "delete",
                            url: basePath +
                                "/api/console/comment/" +
                                id,
                            success: function(data) {
                               if(callback){
                            	   callback();
                               }
                            },
                            error:function(jqXHR){
                            	var message = $.parseJSON(jqXHR.responseText).error;
                            	Swal.fire('删除失败',message,'error');
                            }
                        });
                    });
            }
        
        var banComment =  function(id, callback) {
        	doConfirm(
                    "确定要禁止该ip评论吗？",
                    function() {
                        $.ajax({
                            type: "post",
                            url: basePath +
                                "/api/console/comment/blacklistItem?id=" +
                                id,
                            success: function(data) {
                               if(callback){
                            	   callback();
                               }
                            },
                            error:function(jqXHR){
                            	var message = $.parseJSON(jqXHR.responseText).error;
                            	Swal.fire('禁止IP失败',message,'error');
                            }
                        });
                    });
            }

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
                return config.gravatarPrefix + c.gravatar;
            }
            return basePath + '/static/img/guest.png';
        }

        var getUsername = function(c) {
            if (c == null || !c) {
                return '';
            }
            var username = '';
            if (c.admin) {
                username = '<span style="display:inline-block;height:10px;width:10px;text-align:center;font-size:0.1em;border-radius:10px;background:red;" title="管理员"></span>&nbsp;' +
                    c.nickname
            } else {
                username = c.nickname
            }
            return username;
        }
        var isLogin = config.isLogin;
        var loadComment = function(config) {
            var pageSize = config.pageSize;
            if (!pageSize) {
                pageSize = 10;
            }
            var page = config.page;
            var c = config.container;
            c.html('<img src="'+basePath+'/static/img/loading.gif" class="img-fluid mx-auto"/>')
            $.ajax({
            	url : actPath + '/api/data/commentPage',
            	data : {
                    moduleType: config.moduleType,
                    moduleId: config.moduleId,
                    currentPage: page,
                    pageSize: pageSize,
                    asc: config.asc
                },
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
                            var time =  moment(data.commentDate)
                            .format('YYYY-MM-DD HH:mm');
                            html += '<h6 class="mt-0">' +
                                getUsername(data) + '</h6>';
                            if (data.parent) {
                                var pnickname = getUsername(data.parent);
                                html += '<small style="margin-right:10px">回复' +
                                    pnickname + ':</small>';
                            }
                            html += '<div class="media-content">';
                            html += data.content;
                            html += '</div>'
                            html += '<div style="padding-top:5px;padding-bottom:5px"><small>' + time +
                                '</small>';
                            if (isLogin) {
                            	if(!data.admin && !data.ban){
                                	html += '<a href="javascript:void(0)" data-ban="'+data.id+'" style="margin-left:10px"><small>禁IP</small></a>';
                            	}
                                html += '<a href="javascript:void(0)" data-del="'+data.id+'"  style="margin-left:10px"><small>删除</small></a>';
                                if(data.admin){
                                	html += '<a href="javascript:void(0)" data-edit="'+data.id+'"  style="margin-left:10px"><small>编辑</small></a>';
                                }
                            }
                            if (data.status == 'CHECK') {
                                html += '<a href="javascript:void(0)" data-check="'+data.id+'"  style="margin-left:10px"><small>审核</small></a>';
                            } else {
                            	if(config.allowComment || isLogin){
                            		html += '<a href="javascript:void(0)" data-moduleId="'+config.moduleId+'" data-moduletype="'+config.moduleType+'" data-reply="'+data.id+'"   style="margin-left:10px"><small>回复</small></a>';
                            	}
                                if (data.parent) {
                                    html += '<a href="javascript:void(0)" data-moduleId="'+config.moduleId+'" data-moduletype="'+config.moduleType+'" data-conversations="'+data.id+'" style="margin-left:10px"><small>查看对话</small></a>';
                                }
                            }
                            html += '</div>';
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
                },error:function(){
                	doAlert('获取评论失败');
                }
            });
        }
        
        loadCSS = function(href) {

        	  var cssLink = $("<link>");
        	  $("head").append(cssLink); 

        	  cssLink.attr({
        	    rel:  "stylesheet",
        	    type: "text/css",
        	    href: href
        	  });

        };
        var commentEditor;
        var renderConfig;
        var cache = [];
        return {
        	
            renderComment: function(config) {
            	renderConfig = config;
            	var parserLoad = false;
            	var commentConfig;
                $.ajax({
                	
                	url : basePath + '/api/commentConfig',
                	async : false,
                	success:function(data){
                		commentConfig = data;
                		var commentEditorHtml = '<div data-comment-editor style="padding:15px">';
            	        if(!isLogin){
            	        	commentEditorHtml += '<div class="form-group row" >';
            	        	commentEditorHtml += '<div class="col-sm-12">';
            	        	commentEditorHtml += ' <input type="text" class="form-control" id="nickname" placeholder="昵称，必填">';
            	        	commentEditorHtml += ' </div>';
            	        	commentEditorHtml += '</div>';
            	        }
            	        commentEditorHtml += '<div class="form-group row" >';
            	    	commentEditorHtml += '<div class="col-sm-12">';
            	    	if(data.editor == 'HTML'){
            	    		if(isLogin){
            	    			commentEditorHtml += '<small>支持所有HTML标签</small>';
            	    		}else{
            	    			commentEditorHtml += '<small>支持b, em, i, strong, u等标签</small>';
            	    		}
            	    	}else{
            	    		if(isLogin){
            	    			commentEditorHtml += '<small>支持所有markdown语法</small>';
            	    		}else{
            	    			commentEditorHtml += '<small>支持**text**,*text*语法</small>';
            	    		}
            	    	}
            	    	commentEditorHtml += '<p><span data-smiley style="cursor:pointer;font-size:20px">😂</span><span data-smiley style="cursor:pointer;font-size:20px">😄</span><span data-smiley style="cursor:pointer;font-size:20px">😭 </span><span data-smiley style="cursor:pointer;font-size:20px">😍</span><span data-smiley style="cursor:pointer;font-size:20px">😘</span><span data-smiley style="cursor:pointer;font-size:20px">😝</span><span data-smiley style="cursor:pointer;font-size:20px">🤬</span><span data-smiley style="cursor:pointer;font-size:20px">😴</span><span data-smiley style="cursor:pointer;font-size:20px">👿</span></p>'
            	    	commentEditorHtml += '<textarea class="form-control" id="content" style="height: 200px" placeholder="评论内容，必填"></textarea>';
            	    	commentEditorHtml += '</div>';
            	    	commentEditorHtml += '</div>';
            	    	if (!isLogin) {
            	    		commentEditorHtml += '<p class="text text-info" style="text-align: right">';
            	    		commentEditorHtml += '<a href="javascript:void(0)" onclick="$(\'#other-info\').toggle()"><small>补充其他信息</small></a>';
            	    		commentEditorHtml += '</p>';
            	    		commentEditorHtml += '<div id="other-info" style="display: none">';
            	    		commentEditorHtml += '<div class="form-group row">';
            	    		commentEditorHtml += '<div class="col-sm-12">';
            	    		commentEditorHtml += '<input type="text" class="form-control" id="email" placeholder="邮箱，非必填，用于显示gravatar头像" maxlength="100">';
            	    		commentEditorHtml += '</div>';
            	    		commentEditorHtml += '</div>';
            	    		commentEditorHtml += '<div class="form-group row">';
            	    		commentEditorHtml += '<div class="col-sm-12">';
            	    		commentEditorHtml += '<input type="text" class="form-control" id="website" placeholder="网址，非必填">';
            	    		commentEditorHtml += '</div>';
            	    		commentEditorHtml += '</div>';
            	    		commentEditorHtml += '</div>';
            	    		
            	    		commentEditorHtml += '<div class="form-group row" style="display:none" id="captchaContainer">';
            	    		commentEditorHtml += '<div class="col-sm-12">';
        	    	        commentEditorHtml += '<img src="'+basePath+'/captcha" class="img-fluid" id="captcha-img"/>'
        	    	        commentEditorHtml += ' <input type="text" class="form-control" id="comment-captcha" placeholder="验证码">';
        	    	        commentEditorHtml += '</div>';
        	    	        commentEditorHtml += '</div>';
            	        }
            	    	commentEditorHtml += '<button class="btn btn-danger" data-comment-close>关闭</button><button class="btn btn-primary" style="float:right" data-comment-btn>提交</button>';
            	        commentEditorHtml += '<div style="clear: both"></div>';
            	    	commentEditorHtml += '</div>';
            	        
            	        commentEditor= $(commentEditorHtml);
                		
                		if(data.editor == 'MD'){
                			parserLoad = typeof md != 'undefined'
                       		 if(!parserLoad){
                       			 var script = document.createElement('script');
                       			 script.onload = function () {
                       				parserLoad = true;
                       			 };
                       			 script.src = basePath + '/static/console/markdowniteditor/md.js';
                       			 document.head.appendChild(script);
                       		 }	
                		} else {
                			parserLoad = true;
                		}
                	}
                	
                });
                
                loadComment(config);
                var c = config.container;
                c.on('click','[data-smiley]',function(){
                	insertAtCursor($("[data-comment-editor]").find('textarea')[0],$(this).text());
                })
                for(var i=0;i<cache.length;i++){
                	if(cache[i].is(c)){
                		return ;
                	}
                }
                c.on("click","[data-page]",function(){
                	config.page = parseInt($(this).data('page'));
                	loadComment(config);
            	}); 
                c.on('click',"[data-conversations]",function(){
                	queryConversations($(this).data('conversations'),$(this).data('moduleid'),$(this).data('moduletype'))
                });
                c.on('click',"[data-del]",function(){
                	removeComment($(this).data('del'),function(){
                		Swal.fire('删除成功','','success');
                		loadComment(config);
                	});
                });
                c.on('click',"[data-check]",function(){
                	checkComment($(this).data('check'),function(){
                		loadComment(config);
                	});
                });
                
                c.on('click','[data-ban]',function(){
                	banComment($(this).data('ban'),function(){
                		Swal.fire('禁止成功','','success');
                		loadComment(config);
                	});
                });
                
                c.on('click','[data-edit]',function(){
                	var me = $(this);
                	var id = me.data('edit');
                	$.ajax({
                		 url : basePath + '/api/console/comment/'+id,
                		 success: function(data) {
                			 if($('[data-comment-editor]').length > 0){
                        		 doConfirm("要放弃正在编辑的内容吗？",function(){
                        			 commentEditor.remove();
                                 	 loadReplyEditor(me);
                                 	 $('[data-comment-editor] textarea').val(data.content);
                                 	 $('[data-comment-btn]').attr('data-comment-edit',id)
                        		 });
                        	}else{
                            	loadReplyEditor(me);
                            	$('[data-comment-editor] textarea').val(data.content);
                            	 $('[data-comment-btn]').attr('data-comment-edit',id)
                        	}
	                     },
	                     error:function(jqXHR){
                        	var error = $.parseJSON(jqXHR.responseText).error;
                        	Swal.fire('获取评论失败',error,'error')
                        }
                		
                	})
                });
                
                c.on('click',"[data-reply]",function(){
                	var me = $(this);
                	if($('[data-comment-editor]').length > 0){
               		 doConfirm("要放弃正在编辑的内容吗？",function(){
               			 commentEditor.remove();
                        	 loadReplyEditor(me);
               		 });
               	}else{
                   	loadReplyEditor(me);
               	}
                });
                
                var loadReplyEditor = function(me){
                	me.parent().after(commentEditor);
                	commentEditor.find('input').val('');
                	commentEditor.find('textarea').val('');
                	var parentId = me.data('reply');
                	var moduleId = me.data('moduleid');
                	var moduleType = me.data('moduletype');
	       			 var btn = commentEditor.find('button').eq(1);
	       			 btn.attr('data-parentid',parentId);
                	 btn.attr('data-moduleid',moduleId);
                	 btn.attr('data-moduletype',moduleType);
                	 $("#captcha-img").attr('src',basePath+'/captcha?time='+$.now());
                	 loadUserInfo();
                	 $('html, body').animate({
                         scrollTop: commentEditor.offset().top-100
                     }, 50);
	                   $.ajax({
	                       url: basePath + '/api/comment/captchaRequirement',
	                       success: function(data) {
	                           if (data) {
	                           	$("#captchaContainer").show();
	                           }else{
	                           	$("#captchaContainer").hide();
	                           }
	                       }
	                   });
	                	 $('[data-comment-btn]').removeAttr('data-comment-edit');
                }
                c.on('click','[data-comment-close]',function(){
                	doConfirm('要放弃正在编辑的内容吗?',function(){
                		commentEditor.remove();
                	});
                });
                c.on('click','[data-comment-btn]',function(){
                	var me = $(this)
                	if(me.data('comment-edit')){
                		$.ajax({
                            type: "PATCH",
                            url: basePath + '/api/console/comment/'+me.data('comment-edit'),
                            data:{"content":$("#content").val()},
                            success: function(data) {
                            	if(config.asc){
	                       			config.page = 0;
	                       		 }
	                       		 loadComment(config);
                            	Swal.fire("编辑成功",'','success');
                            },
                            error:function(jqXHR){
                            	var error = $.parseJSON(jqXHR.responseText).error;
                            	Swal.fire('提交失败',error,'error')
                            }
                        });
                		return ;
                	} 
                    var comment = {};
                    comment.content = $("#content").val();
                    comment.website = $("#website").val();
                    comment.email = $("#email").val();
                    comment.nickname = $("#nickname").val();
                    if (me.data('parentid')) {
                        comment.parent = {
                            id: me.data('parentid')
                        };
                    }
                    $.ajax({
                        type: "post",
                        url: actPath + '/api/'+me.data('moduletype')+'/' + me.data('moduleid') + '/comment?validateCode='+$("#comment-captcha").val(),
                        contentType: "application/json",
                        data: JSON.stringify(comment),
                        success: function(data) {
                        	storeUserInfo(comment.nickname,
                                    comment.email, comment.website);
	                       	 var check = commentConfig.status == 'CHECK';
	                       	 if(!check){
	                       		 if(config.asc){
	                       			config.page = 0;
	                       		 }
	                       		 loadComment(config);
	                       	 }
	                          if (check) {
	                               Swal.fire('评论将会在审核通过后显示','','success');
	                           }else{
	                        	   Swal.fire("评论成功",'','success');
	                               $('html, body').animate({
	                                   scrollTop: c.offset().top
	                               }, 50);
	                           }
	                          if(config.afterComment){
	                        	  config.afterComment(check);
	                          }
                        },
                        error:function(jqXHR){
                        	var error = $.parseJSON(jqXHR.responseText).error;
                        	Swal.fire('提交失败',error,'error')
                        },
                        complete: function() {
                        	$("#captcha-img").attr('src',basePath+'/captcha?time='+$.now());
                            me.prop("disabled", false);
                        }
                    });
                })
                cache.push(c);
            },
            
            
            doComment:function(fun){
            	var loadCommentEditor = function(){
                	c.prepend(commentEditor);
                	commentEditor.find('input').val('');
                	commentEditor.find('textarea').val('');
                	 var btn = commentEditor.find('button').eq(1);
	       			 btn.removeAttr('data-parentid');
	       			 
	       			 btn.attr('data-moduleid',renderConfig.moduleId);
                	 btn.attr('data-moduletype',renderConfig.moduleType);
                	 $('html, body').animate({
                        scrollTop: commentEditor.offset().top-100
                    }, 50);
                	 $.ajax({
	                       url: basePath + '/api/comment/captchaRequirement',
	                       success: function(data) {
	                           if (data) {
	                           	$("#captchaContainer").show();
	                           }else{
	                           	$("#captchaContainer").hide();
	                           }
	                       }
	                   });
                }
            	if($('[data-comment-editor]').length > 0){
	           		 doConfirm("要放弃正在编辑的内容吗？",function(){
	           			 commentEditor.remove();
	           			loadCommentEditor();
	           		 });
	           	}else{
	           		loadCommentEditor();
	           	}
            }
        }
    })(config);