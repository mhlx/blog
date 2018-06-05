(function($){
	$('<div class="modal" id="historyModal" tabindex="-1" role="dialog" aria-labelledby="queryModalLabel"><div class="modal-dialog" role="document"><div class="modal-content"><div class="modal-header"><h4 class="modal-title">历史模板</h4></div><div class="modal-body"><div class="container-fluid"><div id="history-tip"></div><div class="table-responsive" id="historyTableContainer"></div></div></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">关闭</button></div></div></div></div>').appendTo($('body'));
	var modal = $('#historyModal');
	var editor;
	var id;
	
	modal.on('shown.bs.modal',function(){
	});
	
	
	return {
		
		loadHistory:function(id,editor){
			$.get(basePath + '/mgr/template/page/'+id+'/history',{},function(data){
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
		}
		
	}
	
})(jQuery);