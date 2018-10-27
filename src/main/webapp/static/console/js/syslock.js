var table = datatable("syslockTable", {
		url : function() {
			return root + 'api/console/syslocks';
		},
		columns : [{
			bind : 'type',
			render:function(v,d){
				if(v == 'QA'){
					return '问答锁';
				}
				if(v == 'PASSWORD'){
					return '密码锁';
				}
			}
		},{
			bind : 'createDate',
			render:function(v,d){
				return moment(v).format("YYYY-MM-DD HH:mm:ss")
			}
		},{
			bind : 'id',
			option:['delete','edit']
		}]
	});

$("[data-create]").click(function(){
	mode = "write";
	$("#lockModal").modal('show')
})
$("#lockModal").on("hide.bs.modal", function(){
				$(this).find("form")[0].reset();
			});

$("#syslockTable").on('click','[data-delete]',function(){
	var me = $(this);
	var id = me.data('delete');
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
			type : "DELETE",
			url : root + 'api/console/syslock/'+id,
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				table.reload();
				swal('删除成功','','success');
			},
			error : function(jqXHR){
				var error = $.parseJSON(jqXHR.responseText).error;
				swal('删除失败',error,'error');
			}
		});
	  }
	});
})
var mode = "write";
var updateId;
$("#syslockTable").on('click','[data-edit]',function(){
	var me = $(this);
	var id = me.data('edit');
	updateId = id;
	mode = "update";
	$.ajax({
		url : root + 'api/console/syslock/'+id,
		success:function(data){
			var v = data.type;
			$('div[data-for]').hide();	
			$('div[data-for="'+v+'"]').show();
			$("#typeSelector").val(v);
			$("#typeSelector").prop('disabled',true);
			if(v == 'QA'){
				$("#question").val(data.question);
				$("#answers").val(data.answers);
			}
			$("#name").val(data.name);
			$("#lockModal").modal('show');
		},
		error:function(jqXHR){
			var error = $.parseJSON(jqXHR.responseText).error;
			swal('保存失败',error,'error');
		}
	})
})

$("#lockModal").on("show.bs.modal",function(){
	$("#lockModal").find("form").find("input[name='password']").val("");
})
$("#typeSelector").change(function(){
				var v = $(this).val();
				$('div[data-for]').hide();	
				$('div[data-for="'+v+'"]').show();
			});
$("#create").click(function() {
	$("#create").prop("disabled",true);
	var data = $("#lockModal").find("form").serializeObject();
	if(mode == "update"){
		data.id = updateId;
	}
	if(mode == 'update'){
		var url = $("#typeSelector").val() == "QA" ? root + 'api/console/syslock/qa/'+data.id : root + 'api/console/syslock/password/'+data.id;
		$.ajax({
			type : "put",
			url : url,
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				table.reload();
				$("#lockModal").modal('hide')
				swal('更新成功','','success');
			},
			error : function(jqXHR){
				var error = $.parseJSON(jqXHR.responseText).error;
				swal('更新失败',error,'error');
			},
			complete:function(){
				$("#create").prop("disabled",false);
			}
		});
	}else{
		var url = data.type == 'QA' ? root + 'api/console/syslock/qa' : root + 'api/console/syslock/password';
		$.ajax({
			type : "post",
			url : url,
			data : JSON.stringify(data),
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				table.reload();
				$("#lockModal").modal('hide')
				swal('保存成功','','success');
			},
			error : function(jqXHR){
				var error = $.parseJSON(jqXHR.responseText).error;
				swal('保存失败',error,'error');
			},
			complete:function(){
				$("#create").prop("disabled",false);
			}
		});
	}
});


