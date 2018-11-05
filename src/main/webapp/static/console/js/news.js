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
							html += '<p class="font-weight-bold">'+moment(news.write).format('HH:mm')+'</p>';
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
		
		var mode = 'write';
		$(function(){
			 
			 $('#newsEditor').summernote({
					lang: 'zh-CN',
					toolbar:[
						['custom', ['file']],
						['style', ['style']],
			            ['font', ['bold', 'underline', 'clear']],
			            ['fontname', ['fontname']],
			            ['para', ['ul', 'ol']],
			            ['insert', ['link']],
			            ['view', ['fullscreen', 'codeview']]
					]
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
					 $("#newsEditor").summernote('code','');
					 $("#time").val(moment().format("YYYY-MM-DD HH:mm"));
				 }
			 }
			 
			 var closeEditor = function(clear){
				 $("#editorContainer").hide();
				 $("#tableContainer").show();
				 $('.dataTable').show();
				 if(clear){
					 $('#newsEditor').summernote('reset');
					 load();
				 }
			 }
			 
			 $("#saveNews").click(function(){
				var me = $(this);
				me.prop('disabled',true);
				var content = $("#newsEditor").summernote('code');
				var news = {};
				news.content = content;
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
							$("#newsEditor").summernote('code',data.content);
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