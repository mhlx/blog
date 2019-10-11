var table = datatable(
		'dataTable',
		{
			url : function() {
				return root + 'api/console/template/datas'
			},
			columns : [
					{
						bind : 'dataName',
						render : function(v, d) {
							if (d.callable) {
								return '<a href="' + root + 'api/data/' + v
										+ '">' + v + '</a>'
							}
							return v;
						}
					},
					{
						bind : 'callable',
						render : function(v, d) {
							return v ? '是' : '否';
						}
					},
					{
						bind : 'xx',
						classes : 'nowrap',
						render : function(v, d) {
							var checked = d.callable ? 'checked=checked' : '';
							var unchecked = !d.callable ? 'checked=checked'
									: '';
							return '<input type="radio" '
									+ checked
									+ ' data-name="'
									+ d.name
									+ '" value="true" name="'
									+ d.dataName
									+ '_callable"/>是&nbsp;&nbsp;&nbsp;&nbsp; <input data-name="'
									+ d.name
									+ '" '
									+ unchecked
									+ ' name="'
									+ d.dataName
									+ '_callable" type="radio" value="false"/>否';
						}
					} ]
		});

$("#dataTable").on(
		'click',
		'input[type="radio"]',
		function() {
			var me = $(this);
			var callable = me.val();
			var name = me.data('name');
			$.ajax({
				type : 'patch',
				url : root + 'api/console/template/data/' + encodeURIComponent(name) + "?callable="
						+ callable,
				success : function() {
					Swal.fire('设置是否可访问成功', '', 'success');
					table.reload();
				},
				error : function(jqXHR) {
					var data = $.parseJSON(jqXHR.responseText);
					Swal.fire('设置是否可访问失败', data.error, 'error');
				}
			});
		});