var table = datatable("pluginTemplateTable", {
		url : function() {
			return root + 'api/console/template/pluginTemplates';
		},
		paging : false,
		columns:[{
			bind : 'pluginName'
		},{
			bind : 'name'
		},{
			bind : 'id',
			option:['delete','edit']
		}]
	});
	$("#pluginTemplateTable").on("click","[data-delete]",function(){
		var id = $(this).data('delete');
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
				url : root + 'api/console/template/pluginTemplate/'+id,
				success:function(data) {
					Swal.fire('删除成功','插件模板已经被删除','success');
					table.reload();
				},
				error:function(jqXHR, textStatus, errorThrown) {
					var data = $.parseJSON(jqXHR.responseText);
					Swal.fire('删除失败',data.error,'error');
				}
			  })
		  }
		});
	});
	
	$("#pluginTemplateTable").on("click","[data-edit]",function(){
		var id = $(this).data('edit');
		window.location.href = root + 'console/template/pluginTemplate/edit/'+id;
	});
	
