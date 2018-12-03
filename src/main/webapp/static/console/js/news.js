var imageExtensions = ["jpg","jpeg","png","gif"];
      var videoExtensions = ["mp4","mov"];
      var isImage = function(ext){
    	  return $.inArray(ext.toLowerCase(),imageExtensions) != -1;
      }
      var isVideo = function(ext){
    	  return $.inArray(ext.toLowerCase(),videoExtensions) != -1;
      }
		
var queryParam = {'currentPage':1};
	
		var load = function(){
			$.ajax({
				url : root + 'api/console/newses',
				data : queryParam,
				success : function(data){
					var datas = data.datas;
					var html = '';
					for(var i=0;i<datas.length;i++){
						html += '<div class="col-md-4" style="margin-bottom:10px">'
						html += '<div class="card h-100" >';
						html += '<div class="card-body wrap" >';
						html += '<h5 class="card-title">'+datas[i].ymd;
						
						html += '<h5>';
						var newses = datas[i].newses;
						for(var j=0;j<newses.length;j++){
							var news = newses[j];
							html += '<p class="font-weight-bold">'+moment(news.write).format('HH:mm')+'&nbsp;&nbsp;&nbsp;<small><i class="fas fa-fw fa-fire"></i>'+news.hits+'</small>&nbsp;&nbsp;&nbsp;<small><i class="far fa-fw fa-comment"></i>'+news.comments+'</small></p>';
							var ele = $('<div>'+news.content+'</div>');
							var text = ele.text();
							if(text.length > 50){text = text.substring(0,50)+'...'}
							var hasMedia = ele.find('img').length > 0 || ele.find('video').length > 0;
							html += '<p style="font-size:13px">'+text+'</p>';
							if(hasMedia){
								html += '<p style="font-size:13px"><a href="'+root+'news/'+news.id+'">查看媒体对象</a></p>';
							}
							html += '<p style="font-size:12px">';
							html += '<a href="###" style="margin-right:10px" data-delete="'+news.id+'"><i class="fas fa-trash-alt"></i></a>';
							html += '<a href="###" style="margin-right:10px" data-edit="'+news.id+'"><i class="fas fa-edit"></i></a>';
							html += '<a href="'+root+'news/'+news.id+'"><i class="fas fa-share"></i></a>';
							html += '</p>';
						}
						html += '</div>';
						html += '</div>';
						html += '</div>';
					}
					$("#news-container").html(html);
					var page = data;
					if (page.totalPage > 1) {
						var html = '';
						html += '<nav >';
						html += '<ul  class="pagination flex-wrap">';
						html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1"><span aria-hidden="true">&laquo;</span></a></li>';
						for (var j = page.listbegin; j < page.listend; j++) {
							if (j == page.currentPage) {
								html += '<li class="page-item active"><a class="page-link" href="javascript:void(0)" >'
										+ j + '</a></li>';
							} else {
								html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
										+ j + '">' + j + '</a></li>';
							}
						}
						html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
								+ page.totalPage
								+ '"><span aria-hidden="true">&raquo;</span></a></a></li>';
						html += '</ul>';
						html += '</nav>';
						$("#news-paging").html(html);
					} else {
						$("#news-paging").html('');
					}
				},
				error : function(jqXHR){
					swal('查询动态失败',$.parseJSON(jqXHR.responseText).error,'error');
				}
				
			});
		}
		$(function(){
			load();
			
			$("#news-paging").on('click','[data-page]',function(){
				var page = $(this).data('page');
				queryParam['currentPage'] = page;
				load();
			});
		});
		
		var editorContent = '';
		var editor;
		var mode = 'write';
		$(function(){
			editor = editor = window.pell.init({
		        element: document.getElementById('newsEditor'),
		        defaultParagraphSeparator: 'p',
		        styleWithCSS: false,
		        onChange: function (html) {
		        	editorContent = html;
		        },
		        actions:[
		        	'bold',
		        	'italic',
		        	'underline',
		        	'strikethrough',
		        	'heading1',
		        	'heading2',
		        	'paragraph',
		        	'quote',
		        	'olist',
		        	'ulist',
		        	'code',
		        	'line',
		        	'link',
		        	{
		        	      name: 'image',
		        	      result: function(){
		        	    	  fileChooser.choose(function(data){
		                		  if(data.length > 0){
        								$(".pell-content").focus();
		                			  for(var i=0;i<data.length;i++){
		                				  var f = data[i];
		                				  var cf = f.cf;
		                				  if(isImage(cf.extension)){
		                					  document.execCommand('insertHTML', false, '<p><img src="'+cf.url+'"/></p>');
		                				  }
		                				  if(isVideo(cf.extension)){
		                					  var d = document;
		                					  (async function getPath () {
		            							const {value: path} = await swal({
		            							  title: '插入视频',
		            							  input: 'text',
		            							  inputValue: cf.url,
		            							  inputPlaceholder:'请输入视频封面地址',
		            							  showCancelButton: true,
		            							  confirmButtonText:'确定',
		            							  cancelButtonText:'取消'
		            							})

		        								$(".pell-content").focus();
		            							if (path) {
		            								 document.execCommand('insertHTML',false, '<p>1</p><button data-src="'+cf.url+'" data-poster="'+path+'" style="display:block;width:100%" readony="readonly" type="button">视频</button><p>1</p>');
		            								 //document.execCommand('insertHTML',false, '<p ><video controls poster="'+path+'"  src="'+cf.url+'"></video></p>');
		            							} else {
		            								 document.execCommand('insertHTML',false, '<p>1</p><button data-src="'+cf.url+'" style="display:block;width:100%" readony="readonly" type="button">视频</button><p>1</p>');
		            							}

		            						})()
		                				  }
		                			  }
		                		  }
		                	  })
		        	      }
		        	    }
		        	
		        ]
		      });
				
			$("#newsEditor .pell-content").keyup(function(event){
				var e = getSelectionStart();
				if(e.tagName == 'BUTTON' && event.keyCode == 8 && $(e).text() != '视频'){
					$(e).remove();
				}
			});
			
			 $("#createNews").click(function(){
				mode = 'write';
				toEditor();
			 });
			 $("#closeEditor").click(function(){
				 closeEditor(false);
			 });
			 
			 var toEditor = function(){
				 $("#tableContainer").hide();
				 $('.dataTable').hide();
				 $("#editorContainer").show();
				 if(mode == 'write'){
					 $("#editForm")[0].reset();
						$(".pell-content").focus();
					 $("#time").val(moment().format("YYYY-MM-DD HH:mm"));
				 }
			 }
			 
			 var closeEditor = function(clear){
				 $("#editorContainer").hide();
				 $("#tableContainer").show();
				 $('.dataTable').show();
				 if(clear){
					 load();
				 }
			 }
			 
			 $("#saveNews").click(function(){
				var me = $(this);
				me.prop('disabled',true);
				var news = {};
				$("#newsEditor .pell-content button").each(function(){
					var me = $(this);
					var poster = me.data('poster');
					var src = me.data('src');
					me.after('<p><video src="'+src+'" poster="'+poster+'" controls></video></p>');
					me.remove();
				});
				news.content = $("#newsEditor .pell-content").html();
				var lockId = $("#lock").val();
				if(lockId != '')
					news.lockId = lockId;
				news.write = $("#time").val();
				news.allowComment = $("#allowComment").is(":checked");
				news.isPrivate = $("#isPrivate").is(":checked");
				if(mode == 'update'){
					news.id = $("#id").val();
				}
				if(mode == 'write'){
					$.ajax({
						type : "post",
						url : root+"api/console/news",
			            contentType:"application/json",
						data : JSON.stringify(news),
						complete : function(jqXHR, textStatus, errorThrown){
							var code = jqXHR.status;
							if(code == 201){
								me.prop("disabled",false);
								swal('动态添加成功','','success');
								closeEditor(true);
							} else {
								me.prop("disabled",false);
								var data = $.parseJSON(jqXHR.responseText);
								swal('添加失败',data.error,'error');
							}
						}
					});
				} else {
					$.ajax({
						type : "PUT",
						url : root+"api/console/news/"+news.id,
			            contentType:"application/json",
						data : JSON.stringify(news),
						success:function(data){
							me.prop("disabled",false);
							swal('动态更新成功','','success');closeEditor(true);
						},
						error : function(jqXHR, textStatus, errorThrown){
							me.prop("disabled",false);
							var data = $.parseJSON(jqXHR.responseText);
							swal('更新失败',data.error,'error');
						}
					});
				}
			});
			 
			 $("#news-container").on("click","[data-delete]",function(){
					var id = $(this).data('delete');
					swal({
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
							url : root + 'api/console/news/'+id,
							success:function(data) {
								load()
								swal('删除成功','动态已经被删除','success');
							},
							error:function(jqXHR, textStatus, errorThrown) {
								var data = $.parseJSON(jqXHR.responseText);
								swal('删除失败',data.error,'error');
							}
						  })
					  }
					});
				});
				 
				 $("#news-container").on("click","[data-edit]",function(){
					var id = $(this).data('edit');
				 	$.ajax({
						url : root + 'api/console/news/'+id,
						success:function(data) {
							mode = 'update';
							$("#time").val(moment(data.write).format("YYYY-MM-DD HH:mm"));
							editor.content.innerHTML = data.content;
							$("#newsEditor .pell-content video").each(function(){
								var me = $(this);
								var poster = me.attr('poster');
								var src = me.attr('src');
								me.parent().after('<button data-src="'+src+'" data-poster="'+poster+'" style="display:block;width:100%" readony="readonly" type="button">视频</button>');
								me.parent().remove();
							});
							$(".pell-content").focus();
							$("#id").val(id);
							$("#isPrivate").prop('checked',data.isPrivate);
							$("#lock").val(data.lockId?data.lockId : '');
							$("#allowComment").prop('checked',data.allowComment);
							toEditor();
						},
						error:function(jqXHR, textStatus, errorThrown) {
							var data = $.parseJSON(jqXHR.responseText);
							swal('获取动态内容失败',data.error,'error');
						}
					  })
				});
			 
			 $("#query").click(function(){
				 queryParam.currentPage = 1;
				 queryParam.content = $("#queryContent").val();
				 load();
			 });
			 
			 
			 $.ajax({
					url : root + 'api/console/locks',
					success:function(data){
						var locks = data;
						if(locks.length > 0){
							var html = '';
							for(var i=0;i<locks.length;i++){
								var lock = locks[i];
								html += '<option value="'+lock.id+'">'+lock.name+'</option>';
							}
							$("#lock").append(html);
						}
					},
					error : function(jqXHR){
						swal('获取锁失败',$.parseJSON(jqXHR.responseText).error,'error');
					}
				})
		});
		
		
		function getSelectionStart() {
			   var node = document.getSelection().anchorNode;
			   return (node.nodeType == 3 ? node.parentNode : node);
			}