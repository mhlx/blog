$(document).ready(function() {
		$('[data-toggle="tooltip"]').tooltip({
			placement : "bottom"
		});
		$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
			if($(this).attr("data-status")){
				window.location.href = basePath+"/mgr/article/index?status="+$(this).attr("data-status")
			}else{
				bootbox.prompt({
				    title: "选择编辑器",
				    inputType: 'select',
				    inputOptions: [
				        {
				            text: 'markdown',
				            value: ''
				        },
				        {
				            text: 'html',
				            value: 'HTML'
				        }
				    ],
				    callback: function (result) {
				    	if(result != ''){
					    	window.location.href = basePath + '/mgr/article/write?editor='+result;
				    	}else{
					    	window.location.href = basePath + '/mgr/article/write';
				    	}
				    }
				});
			}
		})
		$("a[data-action]").click(function() {
			var me = $(this);
			var action = me.attr("data-action");
			switch (action) {
			case "link":
				var link = me.attr('data-link');
				bootbox.dialog({
					title : '博客访问链接',
					message : '<a href="'+link+'" target="_blank">'
							+ link
							+ '</a>',
					buttons : {
						success : {
							label : "确定",
							className : "btn-success"
						}
					}
				});
				break;
			case "logicDelete":
				bootbox.confirm("确定要删除吗？可以在回收站中找回",function(result){
					if(result){
						var id = me.attr('data-id');
						$.ajax({
		    				type : "post",
		    				url : basePath+"/mgr/article/logicDelete?id="+id,
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
		    					me.prop("disabled",false);
		    				}
		    			});
					}
				});
				break;
			case "recover":
				var id = me.attr('data-id');
				$.ajax({
    				type : "post",
    				url : basePath+"/mgr/article/recover?id="+id,
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
    					me.prop("disabled",false);
    				}
    			});
				break;
			case "delete":
				bootbox.confirm("确定要删除吗？这将无法被恢复！！！",function(result){
					if(result){
						var id = me.attr('data-id');
						$.ajax({
		    				type : "post",
		    				url : basePath+"/mgr/article/delete?id="+id,
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
		    					me.prop("disabled",false);
		    				}
		    			});
					}
				});
				break;
			case "pub":
				bootbox.confirm("确定要发布吗？",function(result){
					if(result){
						var id = me.attr('data-id');
						$.ajax({
		    				type : "post",
		    				url : basePath+"/mgr/article/pub?id="+id,
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
		    					me.prop("disabled",false);
		    				}
		    			});
					}
				});
				break;
			}
		});
		$("a[data-page]").click(function() {
			var page = $(this).attr("data-page");
			$("#pageForm").find(
					"input[name='currentPage']")
					.val(page);
			$("#pageForm").submit();
		});
		$("#search-btn").click(function(){
			doSearch();
		});
		$("#search-input").keypress(function(e) {
			var me = $(this);
			// 回车键事件  
			if (e.which == 13) {
				doSearch();
			}
		});
	});					
	function doSearch(){
		$("#pageForm").find(
				"input[name='currentPage']")
				.val(1);
		$("#pageForm").find("input[name='query']").val($("#search-input").val());
		$("#pageForm").find("input[name='space.id']").val($("#search-space").val());
		$("#pageForm").submit();
	}	