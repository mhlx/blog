		var table = datatable('tagTable',{
			url : function(){
				return root + 'api/console/tags';
			},
			paging:true,
			columns : [{
				bind : 'create',
				render:function(v,d){
					return moment(v).format('YYYY-MM-DD HH:mm')
				}
			},{
				bind : 'id',
				option:['delete','edit']
			}]
		});
		var id;
		$("#tagTable").on('click','[data-delete]',function(){
			id = $(this).data('delete');
			swal({
				  title: '你确定吗？',
				  text : '删除之后将无法恢复!',
				  type: 'warning',
				  showCancelButton: true,
				  confirmButtonColor: '#3085d6',
				  cancelButtonColor: '#d33',
				  confirmButtonText: '加载!',
				  cancelButtonText: '取消'
				}).then((result) => {
			  if (result.value) {
				  $.ajax({
					  type : 'delete',
					  url : root + 'api/console/tag/'+id,
					  success:function(){
						  swal('删除成功','','error');
						  table.reload();
					  },
					  error:function(jqXHR){
						  var error = $.parseJSON(responseText).error;
						  swal('删除失败',error,'error');
					  }
					  
				  })
			  }
			});
		})
		$("#tagTable").on('click','[data-edit]',function(){
			id = $(this).data('edit');
			  $.ajax({
				  type : 'get',
				  url : root + 'api/console/tag/'+id,
				  success:function(data){
					  $("input[name='name']").val(data.name);
					  $("#editTagModal").modal('show');
				  },
				  error:function(jqXHR){
					  var status = jqXHR.status;
					  if(status == 404){
						  swal('获取标签失败','标签不存在','error');
						  return ;
					  }
					  var error = $.parseJSON(jqXHR.responseText).error;
					  swal('获取标签失败',error,'error');
				  }
				  
			  })
			})
			
			$("#editTag").click(function(){
				var me = $(this);
				me.prop("disabled",true);
				var merge = $("#merge").prop("checked");
				$.ajax({
					type : "put",
					url : root+"api/console/tag/"+id+"?merge="+merge,
					data : JSON.stringify({name:$("input[name='name']").val()}),
					dataType : "json",
					contentType : 'application/json',
					success : function(data){
						swal('更新成功','','success');
						table.reload();
						me.prop("disabled",false);
					},
					error:function(jqXHR){
						  var error = $.parseJSON(jqXHR.responseText).error;
						  swal('更新失败',error,'error');
							me.prop("disabled",false);
					}
				});
			});