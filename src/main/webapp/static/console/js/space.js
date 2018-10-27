var table = datatable("spaceTable", {
		url : function() {
			return root + 'api/console/spaces';
		},
		columns : [ {
			bind : 'isDefault',
			render : function(v, d) {
				return v ? "是" : "否";
			}
		}, {
			bind : 'isPrivate',
			render : function(v, d) {
				return v ? "是" : "否";
			}
		}, {
			bind : 'lockId',
			render : function(v, d) {
				return v ? "是" : "否";
			}
		}, {
			bind : 'createDate',
			render : function(v, d) {
				return moment(v).format("YYYY-MM-DD HH:mm")
			}
		}, {
			bind : 'id',
			option : [ 'delete', 'edit' ]
		} ]
	});
		var spaceId;
		var update = false;
		$(document).ready(function() {
			$("[data-create]").click(function(){
				update = false;$("#spaceAlias").prop("readonly",false);
				$("#spaceModal").modal('show');
			});
			$("[data-submit]").click(function(){
				var me = $(this);
				me.prop("disabled",true);
				var data = {
						name : $("#spaceName").val(),
						alias : $("#spaceAlias").val(),
						isDefault : $("#isDefault").is(":checked"),
						isPrivate : $("#isPrivate").is(":checked"),
						lockId : $("#lock").val()
					};
				if(update){
					data.id = spaceId;
					$.ajax({
						type : "PUT",
						data : JSON.stringify(data),
						dataType : "json",
						contentType : 'application/json',
						url : root + 'api/console/space/'+spaceId,
						success : function(){
							me.prop("disabled",false);
							$("#spaceModal").modal("hide");
							swal('更新成功','空间已经被更新','success');
							table.reload();
						},
						error : function(jqXHR, textStatus, errorThrown){
							me.prop("disabled",false);
							var data = $.parseJSON(jqXHR.responseText);
							swal('更新失败',data.error,'error');
						}
					});
				}else{
					$.ajax({
						type : "POST",
						data : JSON.stringify(data),
						dataType : "json",
						contentType : 'application/json',
						url : root + 'api/console/space',
						complete : function(jqXHR, textStatus, errorThrown){
							var code = jqXHR.status;
							if(code == 201){
								me.prop("disabled",false);
								$("#spaceModal").modal("hide");
								swal('创建成功','空间已经被创建','success');
								table.reload();
							} else {
								me.prop("disabled",false);
								var data = $.parseJSON(jqXHR.responseText);
								swal('创建失败',data.error,'error');
							}
						}
					});
				}
			});
			$("#spaceTable").on("click","[data-delete]",function(){
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
						url : root + 'api/console/space/'+id,
						success:function(data) {
							swal('删除成功','空间已经被删除','success');
							table.reload();
						},
						error:function(jqXHR, textStatus, errorThrown) {
							var data = $.parseJSON(jqXHR.responseText);
							swal('删除失败',data.error,'error');
						}
					  })
				  }
				});
			});
			
			$("#spaceTable").on("click","[data-edit]",function(){
				spaceId = $(this).data('edit');
				  $.ajax({
					url : root + 'api/console/space/'+spaceId,
					success:function(data) {
						$("#spaceName").val(data.name);
						$("#spaceAlias").val(data.alias).prop("readonly",true);
						if(data.isPrivate){
							$("#isPrivate").prop("checked",true);
						}
						if(data.isDefault){
							$("#isDefault").prop("checked",true);
						}
						$("#spaceModal").modal("show");
						update = true;
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						swal('获取空间详情失败',data.error,'error');
					}
				  })
			});
			
			$("#spaceModal").on("hide.bs.modal", function() {
				$(this).find("form")[0].reset();
			});
			 $.ajax({
				type : 'GET',
				url : root + 'api/console/locks',
				success:function(data) {
					$.each(data, function(index, v) {
						$("#lock").append('<option value="'+v.id+'">'+ v.name + '</option>');
					});
				},
				error:function(jqXHR, textStatus, errorThrown) {
					var data = $.parseJSON(jqXHR.responseText);
					swal('获取访问锁失败',data.error,'error');
				}
			  })
		});