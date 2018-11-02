var table = datatable("fragmentTable", {
		url : function() {
			return root + 'api/console/template/fragments';
		},
		paging : true,
		columns:[{
			bind : 'xx1',
			render:function(v,d){
				return d.id;
			}
		},{
			bind : 'space',
			render:function(v,d){
				return v ? v.name : '默认' ;
			}
		},{
			bind:'createDate',
			render:function(v,d){
				return moment(v).format("YYYY-MM-DD HH:mm");
			}
		},{
			bind : 'id',
			option:['delete','edit']
		}]
	});
	$("#fragmentTable").on("click","[data-delete]",function(){
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
				url : root + 'api/console/template/fragment/'+id,
				success:function(data) {
					swal('删除成功','模板片段已经被删除','success');
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
	
	$("#fragmentTable").on("click","[data-edit]",function(){
		var id = $(this).data('edit');
		window.location.href = root + 'console/template/fragment/edit/'+id;
	});
	
	$("#query-btn").click(function(){
		table.reload({name:$("#query").val(),currentPage:1});
	});