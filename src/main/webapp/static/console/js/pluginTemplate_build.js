var preEditorContent = "";
	var saveFlag = false;
	var defaultPluginTemplates;
	
	function getPlugins(){
		 var plugins = [];
         for(var i=0;i<defaultPluginTemplates.length;i++){
             var pt = defaultPluginTemplates[i];
         	if($.inArray(pt.pluginName, plugins) == -1){
         		plugins.push(pt.pluginName);
         	} 
         }
         return plugins;
	}
	
	function getNames(plugin){
		 var names = [];
         for(var i=0;i<defaultPluginTemplates.length;i++){
             var pt = defaultPluginTemplates[i];
             if(pt.pluginName == plugin)
            	 names.push(pt.name);
         }
         return names;
	}
	var editor = createEditor('editor', [{
	    key: 'Ctrl-S',
	    fun: function() {save(true)}
	}, {
	    key: 'Ctrl-P',
	    fun: function() {preview()}
	}]);
	$(function() {
		

		editor.setOption("fullScreen", true);
		editor.getWrapperElement().style.top = 60+'px'
	    
	    $.ajax({
			type : 'GET',
			url : root + 'api/console/locks',
			success:function(data) {
				var html = '<div class="table-responsive">';
				html += '<table class="table">';
				html += '<tr><th>名称</th><th></th></tr>'
				$.each(data, function(index, v) {
					html += '<tr><td>'+v.name+'</td><td><a href="###" data-lock="'+v.id+'" >加载</a></td></tr>';
				});
				html += '</table>';
				html += '<div>';
				$("#lockBody").html(html);
			},
			error:function(jqXHR, textStatus, errorThrown) {
				var data = $.parseJSON(jqXHR.responseText);
				Swal.fire('获取访问锁失败',data.error,'error');
			}
		  })
		  
        $.ajax({
                url: root + 'api/console/template/defaultPluginTemplates',
                success: function(data) {
                    defaultPluginTemplates = data;
                    var oldPluginName = $("#pluginName").data('value');
                    if(oldPluginName && oldPluginName != '' && oldPluginName != 'null'){
                    	$("#pluginName").append('<option value="'+oldPluginName+'" checked>'+oldPluginName+'</option>');
                    	$("#pluginName").attr('readonly',true)
                    	var oldName = $("#name").data('value');
                    	$("#name").append('<option value="'+oldName+'" checked>'+oldName+'</option>');
                    	$("#name").attr('readonly',true)
                    	return ;
                    }
                    var plugins = getPlugins();
                    for(var i=0;i<plugins.length;i++){
                    	var plugin = plugins[i];
                		if(i == 0)
                			$("#pluginName").append('<option value="'+plugin+'" checked>'+plugin+'</option>');
                		else
                			$("#pluginName").append('<option value="'+plugin+'">'+plugin+'</option>');
                    }
                   
                    if(plugins.length > 0){
                    	var names = getNames(plugins[0]);
                    	for(var i=0;i<names.length;i++){
                        	var name = names[i];
                    		if(i == 0)
                    			$("#name").append('<option value="'+name+'" checked>'+name+'</option>');
                    		else
                    			$("#name").append('<option value="'+name+'">'+name+'</option>');
                        }
                    }
                },
                error: function(jqXHR) {
                    var message = $.parseJSON(jqXHR.responseText).error;
                    Swal.fire("获取默认页面失败", "", "error")
                }
        });
	    
	    $("#pluginName").change(function(){
	    	$("#name").html('');
	    	var pn = $(this).val();
	    	var names = getNames(pn);
        	for(var i=0;i<names.length;i++){
            	var name = names[i];
        		if(i == 0)
        			$("#name").append('<option value="'+name+'" checked>'+name+'</option>');
        		else
        			$("#name").append('<option value="'+name+'">'+name+'</option>');
            }
	    })
		$("#doc").click(function(){
			$("#docModal").modal('show');
		});
	    
	    $("#file").click(
	        function() {
	            fileChooser
	                .choose(function(datas) {
	                    for (var i = 0; i < datas.length; i++) {
	                        var data = datas[i];
	                        var cf = data.cf;
	                        var ext = cf.extension
	                            .toLowerCase();
	                        if ($.inArray(ext, ['jpeg',
	                                'jpg', 'png', 'gif'
	                            ]) == -1) {
	                            editor
	                                .replaceSelection('<a href="' + cf.url + '" target="_blank" title="' + cf.originalFilename + '">' +
	                                    cf.url +
	                                    '</a>')
	                        } else {
	                            var thumb = cf.thumbnailUrl;
	                            if (thumb) {
	                                editor
	                                    .replaceSelection('<a href="' + thumb.large + '" target="_blank" title="' + cf.originalFilename + '"><img src="' + thumb.middle + '" alt="' + cf.originalFilename + '"/></a>')
	                            } else {
	                                editor
	                                    .replaceSelection('<img src="' + cf.url + '"  alt="' + cf.originalFilename + '"/>')
	                            }
	                        }
	                    }
	                })
	        });


	    $("#clear").click(function() {
	        Swal.fire({
	            title: '你确定要清空吗？',
	            type: 'warning',
	            showCancelButton: true,
	            confirmButtonColor: '#3085d6',
	            cancelButtonColor: '#d33',
	            confirmButtonText: '清空!',
	            cancelButtonText: '取消'
	        }).then((result) => {
	            if (result.value) {

	                editor.clear();
	            }
	        });
	    });
	    
	    $("#save").click(function() {
	        $("#saveModal").modal('show')
	    });
	    
	    $("#back").click(function(){
	    	 Swal.fire({
		            title: '你确定要返回吗？',
		            type: 'warning',
		            showCancelButton: true,
		            confirmButtonColor: '#3085d6',
		            cancelButtonColor: '#d33',
		            confirmButtonText: '确定!',
		            cancelButtonText: '取消'
		        }).then((result) => {
		            if (result.value) {
		            	window.location.href = root + 'console/template/pluginTemplate'
		            }
		        });
	    });
	    

	    $("#lock").click(function() {
	        $("#lockModal").modal('show')
	    });
	    $("#beautiful").click(function(){
	    	editor.format();
	    })
	    $("#pluginTemplate").click(function() {
	         renderDefaultTemplates();
	    });


	    var renderDefaultTemplates = function() {
	        var html = '<div class="table-responsive">';
	        html += '<table class="table">';
	        html += '<tr><th>插件名</th><th>模板名</th><th></th></tr>';
	        for (var i = 0; i < defaultPluginTemplates.length; i++) {
	            var p = defaultPluginTemplates[i];
	            html += '<tr><td>' + p.pluginName + '</td><td>' + p.name + '</td><td><a href="###" onclick="loadTemplate(\'' + i + '\')">加载</a></td></tr>';
	        }
	        html += '</table>';
	        html += '</div>';
	        $("#templateModalBody").html(html);
	        $("#templateModal").modal('show');
	    }

 		$("#lockModal").on('click','[data-lock]',function(){
	    	var id = $(this).data('lock');
	    	editor.replaceSelection('<lock id="'+id+'"></lock>');
	    	$("#lockModal").modal('hide');
	    })

	    $("#query").click(function(){
	    	$("#lookupModal").modal('show');
	    });
	});


	function loadTemplate(i) {
	    var template = defaultPluginTemplates[i];
	    editor.setValue(template.template);
	    $("#templateModal").modal('hide');
	}
	
	function save() {
		var pluginTemplate = {"template":editor.getValue()};
		var url = root + 'api/console/template/pluginTemplate';
		var id = $("#pluginTemplateId").val();
		var update = id != null && $.trim(id) != ''
		if(update){
			pluginTemplate.id = id;
			url = root + 'api/console/template/pluginTemplate/'+id;
		}
		pluginTemplate.name=$("#name").val();
		pluginTemplate.pluginName=$("#pluginName").val();
		saveFlag = true;
		var method = update ? 'PUT' : 'POST'
		$.ajax({
			type : method,
			data : JSON.stringify(pluginTemplate),
			url:url,
			dataType : "json",
			contentType : 'application/json',
			success : function(data){
				if(data){
					$("#pluginTemplateId").val(data.id);
				}
				Swal.fire("保存成功",'','success');
			},error:function(jqXHR){
				saveFlag = false;
				var error = $.parseJSON(jqXHR.responseText).error;
				Swal.fire('保存失败',error,'error')
			}
		});
	}
	
	var getFileExtension = function (url) {
	    "use strict";
	    if (url === null) {
	        return "";
	    }
	    var index = url.lastIndexOf("/");
	    if (index !== -1) {
	        url = url.substring(index + 1); // Keep path without its segments
	    }
	    index = url.indexOf("?");
	    if (index !== -1) {
	        url = url.substring(0, index); // Remove query
	    }
	    index = url.indexOf("#");
	    if (index !== -1) {
	        url = url.substring(0, index); // Remove fragment
	    }
	    index = url.lastIndexOf(".");
	    return index !== -1
	        ? url.substring(index + 1) // Only keep file extension
	        : ""; // No extension found
	};
	
	var clipboard=new Clipboard('[data-clipboard-target]');
	clipboard.on('success',function(e){
		 e.clearSelection();
		Swal.fire('拷贝成功','','success');
	});
	clipboard.on('error',function(){
		Swal.fire('拷贝失败','','error');
	});
	
	
	var dataTags;
	
	var dataTable = datatable('dataTable',{
		url :function(){
			return  root+'api/console/template/datas';
		},
		dataConverter:function(data){
			dataTags = data;
			return data;
		},
		columns:[{
			bind:'xx',
			render:function(v,d){
				return '<a href="###" onclick="addDataTag(\''+d.dataName+'\')">加载</a>';
			}
		}]
	});
	
	var fragmentTable = datatable('fragmentTable',{
		url :function(){
			return  root+'api/console/template/fragments';
		},
		paging:true,
		columns:[{
			bind:'xx',
			render:function(v,d){
				return '<a href="###" onclick="addFragment(\''+d.name+'\')">加载</a>';
			}
		}]
	});
	
	function addDataTag(dataName){
		for(var i=0;i<dataTags.length;i++){
			var tag =dataTags[i];
			if(tag.dataName == dataName){
				
				var html = '<data name="'+dataName+'"';
				if(tag.attrs.length > 0){
					for(var j=0;j<tag.attrs.length;j++){
						html += " "+tag.attrs[j]+"=\"\"";
					}
				}
				html += '/>';
				
				editor.replaceSelection(html);
				break;
			}
		}
		$("#lookupModal").modal('hide');
	}
	
	function addFragment(name){
		editor.replaceSelection('<fragment name="'+name+'"/>');
		$("#lookupModal").modal('hide')
	}
	
	var timeout;
	$("#doc-search").on(
			'paste keyup',
			function() {
				if($(this).val() != ''){
					$("#doc-search-clear").show();
				}else{
					$("#doc-search-clear").hide();
				}
				var text = $.trim($(this).val());
				if (timeout) {
					clearTimeout(timeout);
				}
				timeout = setTimeout(function() {
					if (text == '') {
						$("#search-results").html('').hide();
					} else {
						querier.search(text, function(result) {
							$("#search-results").html('').hide();
							if (result.length > 0) {
								$.each(result, function(i, d) {
									$("#search-results").append(
											'<div style="padding:3px"><a href="###" onclick="loadFile(\''
													+ d + '\')">' + d
													+ '</a></div>');
								})
								$("#search-results").show();
							}
						})
					}
				}, 100)
			});

	function loadFile(d) {
		$("#doc-search").hide();
		$("#search-results").hide();
		$("#doc-search-clear").hide();
		querier.getFile(d,function(data){
			$("#doc-title").html(d);
			$("#doc-content").html(data);
			if($(window).width() <= 1024){
				$("#doc-card").css({"max-width":($(window).width()-100)+'px'})
			}else{
				$("#doc-card").css({"max-width":($(window).width()/2)+'px'})
			}
			$("#doc-card").show();
		})
	}
	
	$("#close-doc-card").click(function(){
		$("#doc-card").hide();
		$("#doc-search").show();
		if($("#doc-search").val() != ''){
			$("#doc-search-clear").show();
		}
		$("#search-results").show();
	});
	
	$("#doc-search-clear").click(function(){
		$("#search-results").html('').hide();
		$("#doc-search").val('');
		$("#doc-search-clear").hide();
	});

