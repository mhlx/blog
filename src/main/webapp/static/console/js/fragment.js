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
			bind : 'enable',
			render:function(v,d){
				return v ? "是" : '否' ;
			}
		},{
			bind:'createDate',
			render:function(v,d){
				return moment(v).format("YYYY-MM-DD HH:mm");
			}
		},{
			bind : 'id',
			option:['delete','edit',function(v,d){
				return '<a href="###" title="历史模板" data-history="'+d.id+'"  style="margin-right:10px"><i class="fas fa-archive"></i></a>';
			}]
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
	
	
	
	var historyTable;
	var _Fragment ;
	$("#fragmentTable").on("click","[data-history]",function(){
		_Fragment = $(this).data('history');
		$("#historyModal").modal('show');
	});
	
	$("#historyModal").on('shown.bs.modal',function(){
		if(!historyTable){
			historyTable = datatable('historyTable',{
				url : function(){
					return root+'api/console/template/fragment/'+_Fragment+'/histories'
				},
				columns:[{
					bind : 'time',
					render:function(v){
						return moment(v).format('YYYY-MM-DD HH:mm');
					}
				},{
					bind : 'id',
					option:['delete','edit']
				}]
			});
		}
		else{
			historyTable.reload();
		}
	});
	$("#saveHistory").click(function(){
		$("#historyModal").modal('hide');
		(async function getRemark () {
			const {value: text} = await swal({
			  input: 'textarea',
			  inputPlaceholder: '备注',
			  showCancelButton: true,
			  confirmButtonText: '确定',
			  cancelButtonText: '取消'
			})
			
			if (text) {
				 $.ajax({
					type : 'POST',
					url : root + 'api/console/template/fragment/'+_Fragment+'/history?remark='+text,
					success:function(data) {
						swal('保存成功','','success');
						$("#historyModal").modal('show');
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						swal('保存失败',data.error,'error');
					}
				  })
			}else{
				$("#historyModal").modal('show');
			}
		})()
	});
	
	$("#historyTable").on('click','[data-edit]',function(){
		var id = $(this).data('edit');
		$.ajax({
			type : 'GET',
			url : root + 'api/console/template/history/'+id,
			success:function(data) {
				$("#historyModal").modal('hide');
				(async function getRemark () {
					const {value: text} = await swal({
					  input: 'textarea',
					  inputValue : data.remark,
					  inputPlaceholder: '备注',
					  showCancelButton: true,
					  confirmButtonText: '确定',
					  cancelButtonText: '取消'
					})
					
					if (text) {
						 $.ajax({
							type : 'PUT',
							url : root + 'api/console/template/history/'+data.id+"?remark="+text,
							success:function(data) {
								swal('更新成功','','success');
								$("#historyModal").modal('show');
							},
							error:function(jqXHR, textStatus, errorThrown) {
								var data = $.parseJSON(jqXHR.responseText);
								swal('更新失败',data.error,'error');
							}
						  })
					}else{
						$("#historyModal").modal('show');
					}
				})()
			},
			error:function(jqXHR, textStatus, errorThrown) {
				if(jqXHR.status == 404){
					swal('模板不存在','','error');return;
				}
				var data = $.parseJSON(jqXHR.responseText);
				swal('保存失败',data.error,'error');
			}
		  })
	});
	
	
	$("#historyTable").on('click','[data-delete]',function(){
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
					url : root + 'api/console/template/history/'+id,
					success:function(data) {
						swal('删除成功','','success');
						if(historyTable){
							historyTable.reload();
						}
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						swal('删除失败',data.error,'error');
					}
				  })
			  }
			});
	});