var table = datatable("pageTable", {
	url : function() {
		return root + 'api/console/template/pages';
	},
	paging : true,
	columns:[{
		bind : 'xx1',
		render:function(v,d){
			return d.id;
		}
	},{
		bind : 'alias',
		render:function(v,d){
			return v == '' ? '/' : v;
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
			var url = d.space ? fullRoot + '/space/'+d.space.alias+'/'+d.alias : fullRoot + '/' + d.alias;
			var html = '<a href="###" title="拷贝地址" data-copy="'+d.id+'" data-clipboard-text="'+url+'" style="margin-right:10px"><i class="fas fa-copy"></i></a>';
			html +=  '&nbsp;<a href="###" title="历史模板" data-history="'+d.id+'"  style="margin-right:10px"><i class="fas fa-archive"></i></a>';
			return html ;
		}]
	}]
});

var clipboard=new Clipboard('[data-clipboard-text]');
clipboard.on('success',function(){
	swal('拷贝成功','','success');
});
clipboard.on('error',function(){
	swal('拷贝失败','','error');
});


$(function(){
	$("#pageTable").on("click","[data-delete]",function(){
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
				url : root + 'api/console/template/page/'+id,
				success:function(data) {
					swal('删除成功','页面已经被删除','success');
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
	
	$("#query-btn").click(function(){
		table.reload({query:$("#query").val(),currentPage:1});
	});
	
	$("#pageTable").on("click","[data-edit]",function(){
		var id = $(this).data('edit');
		window.location.href = root + 'console/template/page/edit/'+id;
	});
	
	var historyTable;
	var _Page ;
	$("#pageTable").on("click","[data-history]",function(){
		_Page = $(this).data('history');
		$("#historyModal").modal('show');
	});
	
	$("#historyModal").on('shown.bs.modal',function(){
		if(!historyTable){
			historyTable = datatable('historyTable',{
				url : function(){
					return root+'api/console/template/page/'+_Page+'/histories'
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
					url : root + 'api/console/template/page/'+_Page+'/history?remark='+text,
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
	
});