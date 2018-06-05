var historyFlag = false;
$(document).ready(
	function() {
		var spaceId = $("#pageFormSpaceId").val();
		if(spaceId != undefined){
			$("#query-space-checkbox").prop("checked",true);
			$("#space").val(spaceId).show();
		}
		var global = $("#pageFormGlobal").val();
		if(global && global == "true"){
			$("#query-global").prop("checked",true);
		}
		
		var callable = $("#pageFormCallable").val();
		if(callable && callable == "true"){
			$("#query-callable").prop("checked",true);
		}
		
		$("#query-space-checkbox").click(function(){
			$("#space").toggle();
		})
		$("#query-btn").click(function(){
			var form = "";
			$("#query-form").remove();
			form += '<form id="query-form" style="display:none" action="'+basePath+'/mgr/template/fragment/index" method="get">';
			var name = $.trim($("#query-name").val());
			if(name != ''){
				form += '<input type="hidden" name="name" value="'+name+'"/>';
			}
			if($("#query-space-checkbox").is(":checked")){
				form += '<input type="hidden" name="space.id" value="'+$("#space").val()+'"/>';
			}
			if($("#query-global").is(":checked")){
				form += '<input type="hidden" name="global" value="true"/>';
			}
			if($("#query-callable").is(":checked")){
				form += '<input type="hidden" name="callable" value="true"/>';
			}
			form += '</form>';
			$("body").append(form);
			$("#query-form").submit();
		})
		$("[data-page]").click(function(){
			var page = $(this).attr("data-page");
			$("#pageForm").find("input[name='currentPage']").val(page);
			$("#pageForm").submit();
		})
		$('[data-toggle="tooltip"]').tooltip();
	$("[data-action='remove']").click(function(){
		var me = $(this);
		bootbox.confirm("确定要删除吗?",function(result){
			if(!result){
				return ;
			}
			$.ajax({
				type : "post",
				url : basePath+"/mgr/template/fragment/delete",
				data : {"id":me.attr("data-id")},
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
	});
	
	$("[data-action='save-history']").click(function(){
		var me = $(this);
		bootbox.prompt({
		    title: "备注",
		    inputType: 'textarea',
		    callback: function (result) {
		    	if(result == null){
		    		return ;
		    	}
		    	var id = me.attr("data-id");
				if(!historyFlag){
					historyFlag = true;
					$.ajax({
    					type : "post",
    					url : basePath+"/mgr/template/fragment/"+id+"/saveHistory",
    					data : {remark:result},
    					success : function(data){
    						if(data.success){
    							bootbox.alert("备份成功");
    						}else{
	    						bootbox.alert(data.message);
    						}
    					},
    					complete:function(){
    						historyFlag = false;
    					}
    				});
				}
		    }
		});
	});
	
	
	$("[data-action='load-history']").click(function(){
		var me = $(this);
		var id = me.attr("data-id");
		
		$.get(basePath + '/mgr/template/fragment/'+id+'/history',{},function(data){
			if(data.success){
				data = data.data;
				if(data.length == 0){
					bootbox.alert("没有历史模板记录");
				} else {
					var html = '<table class="table">';
					html += '<tr><th>备注</th><th>时间</th><th>操作</th></tr>';
					for(var i=0;i<data.length;i++){
						var remark = data[i].remark;
						if(remark.length > 10){
							remark = remark.substring(0,10)+"...";
						}
						html += '<tr><td><a href="###" data-toggle="tooltip" title="'+data[i].remark+'">'+remark+'</a></td><td>'+new Date(data[i].time).format('yyyy-mm-dd HH:MM:ss')+'</td><td><a href="###" data-id="'+data[i].id+'" data-toggle="confirmation"  style="margin-right:10px"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></a><a href="###" onclick="editHistoryTemplate(\''+data[i].id+'\',$(this))"><span class="glyphicon glyphicon-edit" aria-hidden="true"></span></a></td></tr>';
					}
					html += '</table>';
					$("#historyTableContainer").html(html);
					$('#historyTableContainer [data-toggle="tooltip"]').tooltip();
					$("#history-tip").html('');
					$("#historyModal").modal('show');
					
					$('[data-toggle=confirmation]').confirmation({
						 rootSelector: '#historyTableContainer',
						 onConfirm:function(){
							 var me = $(this);
							 var id = me.attr('data-id');
							 $.post(basePath + '/mgr/template/history/delete',{id:id},function(data){
								if(data.success){
									me.parent().parent().remove();
									$("#history-tip").html('<div class="alert alert-info alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>删除成功</div>');
								}else{
									$("#history-tip").html('<div class="alert alert-warning alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+data.message+'</div>');
								}
							});
						 }
					});
				}
			}else{
				bootbox.alert(data.message);
			}
		});
	});
	
	
	});

function editHistoryTemplate(id,o){
	$("#historyTableContainer textarea").parent().parent().remove();
	var tr = o.parent().parent();
	tr.after('<tr><td colspan="3"><textarea class="form-control">'+tr.find('td:first a').attr('data-original-title')+'</textarea><button class="btn btn-primary pull-right" style="margin-top:5px">更新</button></td></tr>');
	var area = $("#historyTableContainer textarea");
	area.next().click(function(){
		$.post(basePath +'/mgr/template/history/update',{id:id,remark:area.val()},function(data){
			if(data.success){
				var template = data.data;
				var remark = template.remark;
				if(remark.length > 10){
					remark = remark.substring(0,10)+"...";
				}
				tr.find('td:first a').text(remark);
				tr.find('td:first a').attr('data-original-title',template.remark);
				area.parent().parent().remove();
				$("#history-tip").html('<div class="alert alert-info alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>更新成功</div>');
			}else{
				$("#history-tip").html('<div class="alert alert-warning alert-dismissible"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+data.message+'</div>');
			}
		});
	})
	
}
