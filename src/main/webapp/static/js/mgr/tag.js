$(document).ready(function() {
			$('[data-toggle="tooltip"]').tooltip();
			$("[data-page]").click(function() {
				var page = $(this).attr("data-page");
				$("#pageForm").find("input[name='currentPage']").val(page);
				$("#pageForm").submit();
			});
			$("#search-input").keypress(function(e) {
				var me = $(this);
				// 回车键事件  
				if (e.which == 13) {
					$("#pageForm").find("input[name='currentPage']").val(1);
					$("#pageForm").submit();
				}
			});
			
			$("#editTag").click(function(){
				var me = $(this);
				var data = $("#editTagModal").find("form").serializeObject();
				me.prop("disabled",true);
				var merge = $("#merge").prop("checked");
				$.ajax({
					type : "post",
					url : basePath+"/mgr/tag/update?merge="+merge,
					data : JSON.stringify(data),
					dataType : "json",
					contentType : 'application/json',
					success : function(data){
						if(data.success){
							success(data.message);
							setTimeout(function(){
								window.location.reload();
							},500)
						} else {
							error(data.message);
						}
					},
					complete:function(){
						me.prop("disabled",false);
					}
				});
			});
			
			$("#editTagModal").on("hidden.bs.modal",function(){
				$(this).find("form")[0].reset();
			})
			
			$("a[data-action]").click(function(){
				var action = $(this).attr("data-action");
				switch(action){
				case "remove":
					var id = $(this).attr('data-id');
					bootbox.confirm("确定要删除吗?",function(result){
						if(!result){
							return ;
						}
						$.ajax({
	        				type : "post",
	        				url : basePath+"/mgr/tag/delete?id="+id,
	        	            contentType:"application/json",
	        				data : {},
	        				success : function(data){
	        					if(data.success){
	        						success(data.message);
	        						setTimeout(function(){
	        							window.location.reload();
	        						},500)
	        					} else {
	        						error(data.message);
	        					}
	        				},
	        				complete:function(){
	        				}
	        			});
					});
					break;
				case "edit":
					var id = $(this).attr('data-id');
					var name = $(this).attr("data-tag");
					$("#editTagModal input[name='name']").val(name);
					$("#editTagModal input[name='id']").val(id);
					$("#editTagModal").modal("show");
					break;
				}
			})
		});