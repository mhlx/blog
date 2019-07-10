(function ($, undefined) {
	var dom_parser = false;

	// based on: https://developer.mozilla.org/en/DOMParser
	// does not work with IE < 9
	// Firefox/Opera/IE throw errors on unsupported types
	try {
		// WebKit returns null on unsupported types
		if ((new DOMParser()).parseFromString("", "text/html")) {
			// text/html parsing is natively supported
			dom_parser = true;
		}
	} catch (ex) {}

	if (dom_parser) {
		$.parseHTML2 = function (html) {
			return new DOMParser().parseFromString(html, "text/html");
		};
	}
	else if (document.implementation && document.implementation.createHTMLDocument) {
		$.parseHTML2 = function (html) {
			var doc = document.implementation.createHTMLDocument("");
			var doc_el = doc.documentElement;

			doc_el.innerHTML = html;

			var els = [], el = doc_el.firstChild;

			while (el) {
				if (el.nodeType === 1) els.push(el);
				el = el.nextSibling;
			}

  			// are we dealing with an entire document or a fragment?
			if (els.length === 1 && els[0].localName.toLowerCase() === "html") {
				doc.removeChild(doc_el);
				el = doc_el.firstChild;
				while (el) {
					var next = el.nextSibling;
					doc.appendChild(el);
					el = next;
				}
			}
			else {
				el = doc_el.firstChild;
				while (el) {
					var next = el.nextSibling;
					if (el.nodeType !== 1 && el.nodeType !== 3) doc.insertBefore(el,doc_el);
					el = next;
				}
			}

			return doc;
		};
	}
})(jQuery);
var queryParam = {
    'currentPage': 1
};

var load = function() {
    $.ajax({
        url: root + 'api/console/newses',
        data: queryParam,
        success: function(data) {
            var datas = data.datas;
            var html = '';
            for (var i = 0; i < datas.length; i++) {
                html += '<div class="col-md-4" style="margin-bottom:10px">'
                html += '<div class="card h-100" >';
                html += '<div class="card-body wrap" >';
                html += '<h5 class="card-title">' + datas[i].ymd+ '</h5>';
                var newses = datas[i].newses;
                for (var j = 0; j < newses.length; j++) {
                    var news = newses[j];
                    html += '<p class="font-weight-bold">' + moment(news.write).format('HH:mm') + '&nbsp;&nbsp;&nbsp;<small><i class="fas fa-fw fa-fire"></i>' + news.hits + '</small>&nbsp;&nbsp;&nbsp;<small><i class="far fa-fw fa-comment"></i>' + news.comments + '</small></p>';
                    var doc = $.parseHTML2(news.content);
                    var text = doc.documentElement.textContent;
                    var hasMedia = doc.getElementsByTagName('img').length > 0 || doc.getElementsByTagName('audio').length > 0 || doc.getElementsByTagName('video').length > 0  ;
                    html += '<p style="font-size:13px">' + text + '</p>';
                    if (hasMedia) {
                        html += '<p style="font-size:13px"><a href="' + root + 'news/' + news.id + '">查看媒体对象</a></p>';
                    }
                    html += '<p style="font-size:12px">';
                    html += '<a href="###" style="margin-right:10px" data-delete="' + news.id + '"><i class="fas fa-trash-alt"></i></a>';
                    html += '<a href="'+root+'console/news/edit/'+news.id+'" style="margin-right:10px" ><i class="fas fa-edit"></i></a>';
                    html += '<a href="' + root + 'news/' + news.id + '"><i class="fas fa-share"></i></a>';
                    html += '</p>';
                }
                html += '</div>';
                html += '</div>';
                html += '</div>';
            }
            $("#news-container").html(html);
            var page = data;
            if (page.totalPage > 1) {
                var html = '';
                html += '<nav >';
                html += '<ul  class="pagination flex-wrap">';
                html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1"><span aria-hidden="true">&laquo;</span></a></li>';
                for (var j = page.listbegin; j < page.listend; j++) {
                    if (j == page.currentPage) {
                        html += '<li class="page-item active"><a class="page-link" href="javascript:void(0)" >' +
                            j + '</a></li>';
                    } else {
                        html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="' +
                            j + '">' + j + '</a></li>';
                    }
                }
                html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="' +
                    page.totalPage +
                    '"><span aria-hidden="true">&raquo;</span></a></a></li>';
                html += '</ul>';
                html += '</nav>';
                $("#news-paging").html(html);
            } else {
                $("#news-paging").html('');
            }
        },
        error: function(jqXHR) {
            swal('查询动态失败', $.parseJSON(jqXHR.responseText).error, 'error');
        }

    });
}
$(function() {
    load();

    $("#news-paging").on('click', '[data-page]', function() {
        var page = $(this).data('page');
        queryParam['currentPage'] = page;
        load();
    });
});

$(function() {

    $("#news-container").on("click", "[data-delete]", function() {
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
                    type: 'DELETE',
                    url: root + 'api/console/news/' + id,
                    success: function(data) {
                        load()
                        swal('删除成功', '动态已经被删除', 'success');
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        var data = $.parseJSON(jqXHR.responseText);
                        swal('删除失败', data.error, 'error');
                    }
                })
            }
        });
    });

    $("#news-container").on("click", "[data-edit]", function() {
        var id = $(this).data('edit');
        $.ajax({
            url: root + 'api/console/news/' + id,
            success: function(data) {
                mode = 'update';

                initEditor();
                $("#time").val(moment(data.write).format("YYYY-MM-DD HH:mm"));
                editor.setValue(data.content);
                $("#id").val(id);
                $("#isPrivate").prop('checked', data.isPrivate);
                $("#lock").val(data.lockId ? data.lockId : '');
                $("#allowComment").prop('checked', data.allowComment);
                toEditor();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                var data = $.parseJSON(jqXHR.responseText);
                swal('获取动态内容失败', data.error, 'error');
            }
        })
    });

    $("#query").click(function() {
        queryParam.currentPage = 1;
        queryParam.content = $("#queryContent").val();
        load();
    });

});

