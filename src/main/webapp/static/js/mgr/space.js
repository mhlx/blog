$(document).ready(function() {
	$("#spaceModal").on("show.bs.modal",function(){
		clearTip();
		$(this).find("form")[0].reset();
	})
	$("#editSpaceModal").on("show.bs.modal",function(event){
		clearTip();
		$(this).find("form")[0].reset();
		var a = $(event.relatedTarget);
		var id = a.attr("data-id");
		var modal = $(this)
		$.get(basePath+'/mgr/space/get/'+id,{},function(data){
			if(data.success){
				data = data.data;
				modal.find('.modal-body input[name="name"]').val(data.name);
				modal.find('.modal-body input[name="isPrivate"]').prop("checked",data.isPrivate);
				modal.find('.modal-body input[name="isDefault"]').prop("checked",data.isDefault);
				modal.find('.modal-body input[name="id"]').val(id);
				if(data.lockId){
					modal.find('.modal-body select[name="lockId"]').val(data.lockId)
				} else {
					modal.find('.modal-body select[name="lockId"]').val('');
				}
			}else{
				bootbox.alert(data.message);
			}
		});
	});
	$.get(basePath + '/mgr/lock/all',{},function(data){
		if(data.success){
			var locks = data.data;
			if(locks.length > 0){
				var html = '';
				html += '<div class="form-group">'
				html += '<label for="lockId" class="control-label">锁:</label> ';
				html += '<select name="lockId" class="form-control">';
				html += '<option value="">无</option>';
				for(var i=0;i<locks.length;i++){
					var lock = locks[i];
					html += '<option value="'+lock.id+'">'+lock.name+'</option>';
				}
				html += '</select>';
				html += '</div>';
				$(".lock_container").html(html);
			}
		}else{
			console.log(data.data);
		}
	});
	
	$('[data-toggle="tooltip"]').tooltip();
	$("#create").click(function() {
		clearTip();
		$("#create").prop("disabled",true);
		var data = $("#spaceModal").find("form").serializeObject();
		data.isPrivate = $("#spaceModal").find('input[name="isPrivate"]').is(":checked");
		data.isDefault = $("#spaceModal").find('input[name="isDefault"]').is(":checked");
		$.ajax({
			type : "post",
			url : basePath+"/mgr/space/add",
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
				$("#create").prop("disabled",false);
			}
		});
	});
	
	$("#update").click(function() {
		clearTip();
		$("#update").prop("disabled",true);
		var data = $("#editSpaceModal").find("form").serializeObject();
		data.isPrivate = $("#editSpaceModal").find('input[name="isPrivate"]').is(":checked");
		data.isDefault = $("#editSpaceModal").find('input[name="isDefault"]').is(":checked");
		$.ajax({
			type : "post",
			url : basePath+"/mgr/space/update",
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				if(data.success){
					success(data.message);
					setTimeout(function(){
						window.location.reload();
					},500);
				} else {
					error(data.message);
				}
			},
			complete:function(){
				$("#update").prop("disabled",false);
			}
		});
	});
	
	
	$("[data-action='delete']").click(function(){
		var me = $(this);
		var html = "是否删除这个空间？您需要注意以下几点：";
		html += '<div>';
		html += '<ul>';
		html += '<li>默认空间无法被删除</li>';
		html += '<li>空间下所有的文章将会转移到默认空间下，如果没有设置默认空间将会删除失败</li>';
		html += '<li><b>同时会删除该空间下所有的模板片断、页面以及页面的评论</b></li>';
		html += '<li><b>该操作无法被恢复</b></li>';
		html += '</ul>';
		html += '</div>';
		bootbox.confirm(html, function(result){ 
			if(result){
				var id = me.attr("data-id");
				$.ajax({
					type : "post",
					url : basePath+"/mgr/space/delete/"+id,
					data : {},
					dataType : "json",
					contentType : 'application/json',
					success : function(data){
						if(data.success){
							bootbox.alert("删除成功");
							setTimeout(function(){
								window.location.reload();
							},500);
						} else {
							bootbox.alert(data.message);
						}
					},
					complete:function(){
					}
				});
			}
		});
	});
});
