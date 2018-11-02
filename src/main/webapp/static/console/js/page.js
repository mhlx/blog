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
		bind:'createDate',
		render:function(v,d){
			return moment(v).format("YYYY-MM-DD HH:mm");
		}
	},{
		bind : 'id',
		option:['delete','edit',function(v,d){
			var url = d.space ? fullRoot + '/space/'+d.space.alias+'/'+d.alias : fullRoot + '/' + d.alias;
			return '<a href="###" data-copy="'+d.id+'" data-clipboard-text="'+url+'" style="margin-right:10px"><i class="fas fa-copy"></i></a>'
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
});