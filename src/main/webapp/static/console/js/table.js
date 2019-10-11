function datatable(id, config) {
	var ths = $("#" + id).find('th');
	var binds = [];
	$.each(ths, function(i, v) {
		v = $(v);
		var bind = v.data('bind');
		binds.push({
			index : i,
			bind : bind
		});
	});

	var columns = config.columns;
	var paging = config.paging;
	var _params = {};

	var load = function() {
		var tbody = $("#" + id).find('tbody');
		tbody.html('<tr ><td class="text-center" colspan="'
				+ binds.length + '">正在加载...</td></tr>');
		$
				.ajax({
					type : 'GET',
					data : _params,
					url : config.url.call(),
					success : function(data) {
						if (config.dataConverter) {
							data = config.dataConverter.call(this, data);
						}
						var page;
						if (paging) {
							page = data;
							data = data.datas;
						}

						var body_template = "";
						for (var i = 0; i < data.length; i++) {
							body_template += "<tr>";
							var d = data[i];
							for (var j = 0; j < binds.length; j++) {
								var bind = binds[j].bind;
								var v = d[bind];
								var clazz = "";
								var option = {
									enable : false,
									options : []
								};
								for (var k = 0; k < config.columns.length; k++) {
									var column = config.columns[k];
									if (column.bind == bind) {
										if (column.render) {
											v = column.render.call(this, v, d);
										}
										if (column.classes) {
											clazz = column.classes;
										}
										if (column.option
												&& column.option.length > 0) {
											option.enable = true;
											option.options = option.options
													.concat(column.option);
										}
										break;
									}
								}
								if (option.enable) {
									var html = '';
									for (var s = 0; s < option.options.length; s++) {
										var op = option.options[s];
										if (op == 'delete') {
											html += '<a href="###" data-delete="'
													+ v
													+ '" style="margin-right:10px"><i class="fas fa-trash-alt"></i></a>';
										} else if (op == 'edit') {
											html += '<a href="###" data-edit="'
													+ v
													+ '" style="margin-right:10px"><i class="fas fa-edit"></i></a>';
										} else {
											html += op.call(this,v,d);
										}
									}
									v = html;
									clazz += " nowrap";
								}

								if (clazz != "") {
									body_template += '<td class="' + clazz
											+ '">' + v + '</td>';
								} else {
									body_template += '<td>' + v + '</td>';
								}
							}
							body_template += "</tr>";
						}
						tbody.html(body_template);

						if (paging) {
							var p = $("#" + id + "_paging");
							var html = '';
							if (page.totalPage > 1) {
								html += '<nav >';
								html += '<ul  class="pagination flex-wrap">';
								html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1"><span aria-hidden="true">&laquo;</span></a></li>';
								for (var j = page.listbegin; j < page.listend; j++) {
									if (j == page.currentPage) {
										html += '<li class="page-item active"><a class="page-link" href="javascript:void(0)" >'
												+ j + '</a></li>';
									} else {
										html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
												+ j + '">' + j + '</a></li>';
									}
								}
								html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="'
										+ page.totalPage
										+ '"><span aria-hidden="true">&raquo;</span></a></a></li>';
								html += '</ul>';
								html += '</nav>';
							}
							p.html(html);
						}

					},
					error : function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						Swal.fire('查询失败', data.error, 'error');
					}
				});
	};

	load();

	$("#" + id + "_paging").on('click', '[data-page]', function() {
		var page = $(this).data('page');
		_params['currentPage'] = page;
		load();
	});

	return {
		reload : function(param) {
			if (param) {
				Object.keys(param).forEach(function(key, index) {
					_params[key] = param[key];
				});
			}
			load();
		},
		params : function() {
			return _params;
		}
	}

}