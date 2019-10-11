$(document).ready(function() {
		$("#import-btn").click(function() {
			readFile(function(pages){
				$.ajax({
		            type: "post",
		            url: root + 'api/console/template/imports',
		            data: JSON.stringify(pages),
		            dataType: "json",
		            contentType: 'application/json',
		            success: function(data) {
	                	$("#messages").hide();
	    				var records = data;
	    				var html = '';
	    				for (var i = 0; i < records.length; i++) {
	    					var record = records[i];
	    					var clazz = record.success ? 'text-success' : 'text-danger';
	    					html += "<tr class='"+clazz+"'><td>" + record.message
	    							+ "</td></tr>";
	    				}
	    				$("#messageBody").html(html);
	    				$("#messages").show();
	    				Swal.fire('导入成功','','success')
		            },
		            fail: function(jqXHR) {
		            	$("#messages").hide();
		            	var error = $.parseJSON(jqXHR.responseText).error;
		            	Swal.fire('导入失败',error,'error');
		            }
		        });
			})
		});
		
		$("#preview-btn").click(function() {
			readFile(function(pages){
				$.ajax({
		            type: "post",
		            url: root + 'api/console/template/imports/preview',
		            data: JSON.stringify(pages),
		            dataType: "json",
		            contentType: 'application/json',
		            success: function(data) {
	                	$("#messages").hide();
	    				var pages = data.pages;
	    				var html = '';
	    				if(pages.length > 0){
	    					html += '<tr><th>页面名称</th><th>路径</th><th>空间全局</th></tr>';
	    					for (var i = 0; i < pages.length; i++) {
		    					var page = pages[i];
		    					html += "<tr ><td>" + page.name
		    							+ "</td><td>"+page.alias+"</td><td>"+(page.spaceGlobal ? '是': '否')+"</td></tr>";
		    				}
	    				}
	    				var fragments = data.fragments;
	    				if(fragments.length > 0){
	    					html += '<tr><th colspan=2>模板片段</th><th>全局</th></tr>';
	    					for (var i = 0; i < fragments.length; i++) {
		    					var fragment = fragments[i];
		    					html += "<tr ><td colspan=2>" + fragment.name
		    							+ "</td><td>"+(fragment.global ? '是': '否')+"</td></tr>";
		    				}
	    				}
	    				$("#messageBody").html(html);
	    				$("#messages").show();
	    				Swal.fire('预览成功','','success')
		            },
		            fail: function(jqXHR) {
		            	$("#messages").hide();
		            	var error = $.parseJSON(jqXHR.responseText).error;
		            	Swal.fire('预览失败',error,'error');
		            }
		        });
			})
		})
	});
	function readFile(callback){
		var file = document.getElementById('file').files[0];
		if(!file){
			Swal.fire("请选择要导入的文件",'','error');
			return ;
		}
		var fileName = document.getElementById('file').files[0].name;
		if (fileName.indexOf('.json') == -1) {
			Swal.fire("请选择json格式的文件",'','error');
			return;
		}
		var reader = new FileReader();
		reader.readAsText(file, 'UTF-8');
		reader.onload = function(event){
			var text = event.target.result;
			var pages = {};
			var spaceId = $("#importSpaceId").val();
			if(spaceId != ''){
				pages.spaceId = spaceId;
			}
			pages.pages = $.parseJSON(text);
			callback(pages);
		};
	}
	
	$(document).ready(function(){
		$("#clearMgrCache").click(function(){
			Swal.fire({
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
					url : root + 'api/console/template/caches',
					success:function(data) {
						Swal.fire('清除缓存成功','','success');
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						Swal.fire('清除缓存失败',data.error,'error');
					}
				  })
			  }
			});
		});
		
		$("#clearPreview").click(function(){
			Swal.fire({
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
					url : root + 'api/console/template/previews',
					success:function(data) {
						Swal.fire('清除预览页面成功','','success');
					},
					error:function(jqXHR, textStatus, errorThrown) {
						var data = $.parseJSON(jqXHR.responseText);
						Swal.fire('清除预览页面失败',data.error,'error');
					}
				  })
			  }
			});
		});
	});