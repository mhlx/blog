var autoParse = true;
		var publishing = false;
		var tags = [];
		function detect() {
			var wwidth = $(window).width();
			if (wwidth <= 768) {
				autoParse = false;
				toEditor();
				$("#mobile-style").remove();
				$("head").append("<style type='text/css' id='mobile-style'>.icon {font-size: 30px} #toolbar img{width:32px !important;height:32px !important} .CodeMirror-scroll{margin-top:10px}</style>");
			} else {
				autoParse = true;
				$("#in").css({
					width : '50%',
					top : '30px'
				}).show();
				$("#out").css({
					left : '50%',
					top: 0
				}).show();
				$("#editor-icon").remove();
				$("#preview-icon").remove();
				$("#mobile-style").remove();
				if (editor)
					render();
				
				document.addEventListener('drop', function(e){
				      e.preventDefault();
				      e.stopPropagation(); var reader = new FileReader();
 						var file = e.dataTransfer.files[0];
				      if(file.name.indexOf('.md') == -1){
				    	  base64Upload(file);
				      } else {
					      reader.onload = function(e){
					        editor.setValue(e.target.result);
					      };
					      reader.readAsText(file);
				      }
				 }, false);
				
			}
		}
		detect();
		var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
			mode : 'gfm',
			lineNumbers : false,
			matchBrackets : true,
			lineWrapping : true,
			scrollbarStyle:null,
			theme : 'base16-light',
			dragDrop:false,
		    extraKeys: {"Enter": "newlineAndIndentContinueMarkdownList","Alt-F": "findPersistent","Ctrl-A":"selectAll","Ctrl-S":function(){
		    	showBase();	
		    }}
		});
		
		
		var summaryEditor = CodeMirror.fromTextArea(document.getElementById('summary'), {
			mode : 'gfm',
			lineNumbers : false,
			matchBrackets : true,
			lineWrapping : true,
			theme : 'base16-light',
			dragDrop:false,
		    extraKeys: {"Enter": "newlineAndIndentContinueMarkdownList","Alt-F": "findPersistent","Ctrl-A":"selectAll"}
		});
		
		$("#baseModal .CodeMirror").css({"height":"300px"});
		
		editor.on('paste',function(editor,evt){
			var clipboardData, pastedData;
// evt.stopPropagation();
// evt.preventDefault();
		    clipboardData = evt.clipboardData || window.clipboardData;
		    var files = clipboardData.files;
		    if(files.length > 0){
		    	var f = files[0];// 上传第一张
		    	var type = f.type;
		    	if(type.indexOf('image/') == -1){
		    		bootbox.alert("只能上传图片文件");
		    		return ;
		    	}
		    	
		    	base64Upload(f);
		    	
		    }
		})
		
		CodeMirror.keyMap.default["Shift-Tab"] = "indentLess";
		CodeMirror.keyMap.default["Tab"] = "indentMore";
		
		emoji.setClick(function(emoji){
			editor.replaceSelection(emoji);
			return true;
		});
		

// $(window).resize(function() {
// detect();
// });

		function toEditor() {
			$("#out").hide();
			$("#toolbar").css({
				width : '100%',
				height:'45px'
			});
			$("#in").css({
				width : '100%',
				top : '45px'
			}).show();
			$("#editor-icon").remove();
			$("#preview-icon").remove();
			$(".icon").show();
			$("#toolbar")
					.append(
							'<span onclick="toPreview()" class="glyphicon glyphicon-eye-open icon" id="preview-icon" title="预览"></span>');
		}

		function toPreview() {
			render();
			$("#in").hide();
			$("#out").css({
				left : '0%',
				top:'45px'
			}).show();
			$("#editor-icon").remove();
			$("#preview-icon").remove();
			$(".icon").hide();
			$("#toolbar img").hide();
			$("#toolbar")
					.append(
							'<span onclick="toEditor()" class="glyphicon glyphicon-eye-close icon" id="editor-icon" title="取消预览"></span>');
		}
		var md = window.markdownit({
			 html: true,
			  linkify: true,
			  typographer: true
		}).use(window.markdownitFootnote);

		md.renderer.rules.paragraph_open = md.renderer.rules.heading_open = injectLineNumbers;
		var render = function() {
			var html = md.render(editor.getValue());
			$("#out").html(html);
			scrollMap = null;
		};
		

		render();

		editor.on('change', function(e) {
			if (autoParse) {
				render();
			}
		});

		function showBase() {
			$("#baseModal").modal('show');
		}

		function openFile() {
			fileSelectPageQuery(1, '');
			$("#fileSelectModal").modal("show");
		}
		
		function getStyle(){
			return {"csses":$("#css-link-text").val(),"styles":$("#css-style-text").val(),"preview":$.trim($("#css-preview-text").val())};
		}
		
		$(function(){
			
			$.get(basePath + '/mgr/lock/all',{},function(data){
				var oldLock = $("#oldLock").val();
				if(data.success){
					var locks = data.data;
					if(locks.length > 0){
						var html = '';
						html += '<div class="form-group">'
						html += '<label for="lockId" class="control-label">锁:</label> ';
						html += '<select id="lockId" class="form-control">';
						html += '<option value="">无</option>';
						for(var i=0;i<locks.length;i++){
							var lock = locks[i];
							if(lock.id == oldLock){
								html += '<option value="'+lock.id+'" selected="selected">'+lock.name+'</option>';
							}else{
								html += '<option value="'+lock.id+'">'+lock.name+'</option>';
							}
						}
						html += '</select>';
						html += '</div>';
						$("#lock_container").html(html);
					}
				}else{
					console.log(data.data);
				}
			});
			
			
			$("#status").change(function() {
				if ($(this).val() == 'SCHEDULED') {
					$("#scheduleContainer").show();
				} else {
					$("#scheduleContainer").hide();
				}
			});
			
			var oldTags = $("#oldTags").val();
			if(oldTags != ''){
				var oldTagArray = oldTags.split(",");
				for(var i=0;i<oldTagArray.length;i++){
					var tag = oldTagArray[i];
					if(tag != ''){
						addTag(tag);
						renderTag();
					}
				}
			}
			var oldSpace = $("#oldSpace").val();
			if(oldSpace != ""){
				$("#space").val(oldSpace);
			}

			$("#tags-input").on('input',function(e) {
				var me = $(this);
				var tag = me.val();
				$("#add-tag-sign").remove();
				if($.trim(tag).length > 0){
					$("#tags-input").after('<a onclick="_addTag()" class="glyphicon glyphicon-ok-sign form-control-feedback form-control-clear" id="add-tag-sign" style="pointer-events: auto; text-decoration: none;cursor: pointer;"></a>');
				}
			});
			
			setInterval(function(){
				save();
			},10000);
			
			$("#submit-art").click(function(){
				publishing = true;
				var me = $(this);
				var article = getArticle();
				me.prop("disabled",true);
				var url = "";
				if(article.id && article.id != null){
					url = basePath+"/mgr/article/update";
				}else{
					url = basePath+"/mgr/article/write";
				}
				$.ajax({
					type : "post",
					url : url,
		            contentType:"application/json",
					data : JSON.stringify(article),
					success : function(data){
						if(data.success){
							bootbox.alert("保存成功");
							setTimeout(function(){
								window.location.href = basePath+'/mgr/article/index';
							},500)
						} else {
							$("#error-tip").html(data.message).show();
							publishing = false;
						}
					},
					complete:function(){
						me.prop("disabled",false);
					}
				});
			});
			
			$("#baseModal").on("show.bs.modal",function(){
				$("#error-tip").html('').hide();
			});
			
			$("#baseModal").on("shown.bs.modal",function(){
				summaryEditor.refresh();
			});
			
			$("#stackeditModal").on('hidden.bs.modal',function(){
				showStackedit();
			});
			
		});
		
		function _addTag(){
			var me =$("#tags-input");
			addTag($.trim(me.val()));
			renderTag();
			me.val("");
			$("#add-tag-sign").remove();
		}
		
		function getArticle(){
			var article = {};
			article.title = $("#title").val();
			if($.trim(article.title) == ""){
				article.title = "No title";
			}
			article.content = editor.getValue();
			article.from = $("#from").val();
			article.status = $("#status").val();
			if($("#level").val() != ''){
				article.level = $("#level").val();
			}
			if(article.status == 'SCHEDULED'){
				article.pubDate = $("#scheduleDate").val()
			};
			article.isPrivate = $("#private").prop("checked");
			article.allowComment = $("#allowComment").prop("checked");
			article.tags = tags;
			article.featureImage = $("#featureImage").val();
			article.summary = summaryEditor.getValue();
			article.space = {"id":$("#space").val()};
			article.editor = 'MD';
			if($("#lockId").val() != ""){
				article.lockId = $("#lockId").val();
			}
			article.alias = $("#alias").val();
			if($("#id").val() != ""){
				article.id = $("#id").val();
			}
			return article;
		}
		
		function save(){
			if(publishing){
				return ;
			}
			var article = getArticle();
			article.status = 'DRAFT';
			if(article.content == ''){
				return ;
			}
			var url = "";
			if(article.id && article.id != null){
				url = basePath+"/mgr/article/update";
			}else{
				url = basePath+"/mgr/article/write";
			}
			publishing = true;
			$.ajax({
				type : "post",
				url : url,
				async:false,
	            contentType:"application/json",
				data : JSON.stringify(article),
				success : function(data){
					if(data.success){
						$("#id").val(data.data.id);
					}
				},
				complete:function(){
					publishing = false;
				}
			});
		}
		
		function showTagError(error) {
			if ($("#tag-tip").length == 0)
				$("#tags-input").before(error);
			setTimeout(function() {
				$("#tag-tip").remove();
			}, 1000);
		}

		function addTag(tag) {
			var tag = $.trim(tag);
			if (tags.length >= 10) {
				showTagError('<span id="tag-tip" class="text text-danger">最多只能有10标签</span>')
			} else if (tag == "" || tag.length > 20) {
				showTagError('<span id="tag-tip" class="text text-danger">标签名在1~20个字符之间</span>')
			} else {
				for (var i = 0; i < tags.length; i++) {
					var _tag = tags[i];
					if (_tag.name == tag) {
						showTagError('<span id="tag-tip" class="text text-danger">已经存在该标签</span>')
						$("#tags-input").val("");
						return;
					}
				}
				tags.push({
					"name" : $.trim(tag)
				});
			}
		}

		function renderTag() {
			if(tags.length == 0){
				$("#tags-container").html('');
				return ;
			}
			var html = '<div class="table-responsive"><table class="table table-borderless">';
			if (tags.length > 5) {
				html += '<tr>';
				for (var i = 0; i < 5; i++) {
					html += getLabel_html(tags[i].name);
				}
				html += '</tr>';
				html += '<tr>';
				for (var i = 5; i < tags.length; i++) {
					html += getLabel_html(tags[i].name);
				}
				html += '</tr>';
			} else {
				html += '<tr>';
				for (var i = 0; i < tags.length; i++) {
					html += getLabel_html(tags[i].name);
				}
				html += '</tr>';
			}
			html += '<table></div>';
			$("#tags-container").html(html);
		}
		
		function save(){
			if(publishing){
				return ;
			}
			var article = getArticle();
			article.status = 'DRAFT';
			if(article.content == ''){
				return ;
			}
			var url = "";
			if(article.id && article.id != null){
				url = basePath+"/mgr/article/update";
			}else{
				url = basePath+"/mgr/article/write";
			}
			publishing = true;
			$.ajax({
				type : "post",
				url : url,
				async:false,
	            contentType:"application/json",
				data : JSON.stringify(article),
				success : function(data){
					if(data.success){
						$("#id").val(data.data.id);
						// showAutoSaveTip(new Date());
					}
				},
				complete:function(){
					publishing = false;
				}
			});
		}

		function getLabel_html(tag) {
			return '<td><span class="label label-success">'
					+ tag
					+ '<a href="###" onclick="removeTag($(this))" style="margin-left:5px"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></a></span></td>';
		}

		function removeTag(o) {
			var tag = o.parent().text();
			for (var i = 0; i < tags.length; i++) {
				if (tags[i].name == tag) {
					tags.splice(i, 1);
					renderTag();
					return;
				}
			}
		}
		
		
		function showAutoSaveTip(time){
			$("#auto-save-tip").remove();
			var hour = time.getHours();
			var minute = time.getMinutes();
			var second = time.getSeconds();
			if(hour < 10){
				hour = "0" + hour;
			}
			if(minute < 10){
				minute = "0" + minute;
			}
			if(second < 10){
				second = "0"+second;
			}
			$('body').append('<div id="auto-save-tip"  style="position:fixed;top:0;text-align:center;width:100%"><div class="alert alert-info" style="width:200px;margin:0 auto;margin-top:5px;z-index:9999999;">'+(hour + ':' + minute+ ':'+second)+'自动保存成功</div></div>');
			setTimeout(function(){
				$("#auto-save-tip").remove();
			},1500);
		}
		
		function base64Upload(f){
			// show pick up
			if(f.size == 0){
				bootbox.alert('不能上传空文件或者文件夹');
				return;
			}
	    	var reader = new FileReader();
	    	
	    	dirChooser.choose(function(dir,store){
	    		var left = $(window).width()/2 - 64;
	    		var top = $(window).height()/2 - 64;
	    		$("#upload-loading").remove();
	    		$('body').append("<div id='upload-loading'><div class='modal-backdrop in' style='z-index: 1040;'></div><img src='"+basePath+"/static/img/loading.gif' style='position:absolute;top:"+top+"px;left:"+left+"px' /></div>");
	    		reader.onload = (function(theFile) {
		            return function(e) {
		            	var base64 = e.target.result;
		            	$.post(basePath+'/mgr/file/uploadWithBase64',{parent:dir.id,store:store,name:f.name,base64:base64},function(data){
				    		$("#upload-loading").remove();
		            		if(data.success){
		            			var result = data.data[0];
		            			if(result.error){
		            				bootbox.alert(result.error);
		            			} else {
		            				var name = result.name;
		            				var ext = name.split('.').pop().toLowerCase();
		            				if(ext == 'jpg' || ext == 'jpeg' || ext == 'png' || ext == 'gif'){
		            					var middle = result.thumbnailUrl ? result.thumbnailUrl.middle : result.url;
			            				var large = result.thumbnailUrl ? result.thumbnailUrl.large : result.url;
			            				var md = '[!['+result.name+']('+middle+' "'+result.name+'")]('+large+' "'+result.name+'")';
			            				editor.replaceSelection(md);
		            				} else {
		            					var md = '['+result.name+']('+result.url+')';
		            					editor.replaceSelection(md);
		            				}
		            			}
		            		}else{
		            			bootbox.alert(data.message);
		            		}
		            	})
		            };
		          })(f);
		         reader.readAsDataURL(f);
	    	});
		}
		
		
		var oldVForScroll;
		var building = false;
		
		var scrollMap;
		var syncResultScroll = function () {
			  var lineHeight = parseFloat($('.CodeMirror').css('line-height')),
			      lineNo, posTo;
			  lineNo = editor.lineAtHeight(editor.getScrollInfo().top, 'local');
			  if (!scrollMap) { buildScrollMap(); }
			  if(!scrollMap){
				  return;
			  }
			  posTo = scrollMap[lineNo];
			  $('#out').stop(true).animate({
			    scrollTop: posTo
			  }, 100, 'linear');
			};
			
			function buildScrollMap() {
				if(building){
					return ;
				}
				building = true;
				oldVForScroll = editor.getValue();
				$('#out').waitForImages(function() {
					 var i, offset, nonEmptyList, pos, a, b, lineHeightMap, linesCount,
				      acc, textarea = $('.CodeMirror'),
				      _scrollMap;

				  offset = $('#out').scrollTop() - $('#out').offset().top;
				  _scrollMap = [];
				  nonEmptyList = [];
				  
				  linesCount = editor.lineCount();
				  for (i = 0; i < linesCount; i++) { _scrollMap.push(-1); }

				  nonEmptyList.push(0);
				  _scrollMap[0] = 0;

				  $('.line').each(function (n, el) {
				    var $el = $(el), t = $el.data('line');
				    if (t === '') { return; }
				    if (t !== 0) { nonEmptyList.push(t); }
				    _scrollMap[t] = Math.round($el.offset().top + offset);
				  });

				  nonEmptyList.push(linesCount);
				  _scrollMap[linesCount] = $('#out')[0].scrollHeight;

				  pos = 0;
				  for (i = 1; i < linesCount; i++) {
				    if (_scrollMap[i] !== -1) {
				      pos++;
				      continue;
				    }

				    a = nonEmptyList[pos];
				    b = nonEmptyList[pos + 1];
				    _scrollMap[i] = Math.round((_scrollMap[b] * (i - a) + _scrollMap[a] * (b - i)) / (b - a));
				  }

				  building = false;
				  if(editor.getValue() == oldVForScroll){
					  scrollMap = _scrollMap;
				  }
				})
				
			}
			  
			  function injectLineNumbers(tokens, idx, options, env, slf) {
				    var line;
				    if (tokens[idx].map && tokens[idx].level === 0) {
				      line = tokens[idx].map[0];
				      tokens[idx].attrJoin('class', 'line');
				      tokens[idx].attrSet('data-line', String(line));
				    }
				    return slf.renderToken(tokens, idx, options, env, slf);
				  }
			  
			  if($(window).width() > 768){
				  editor.on('scroll', function () {
					    $('#out').off('scroll');
					    editor.on('scroll', syncResultScroll);
					  });
			  }
			  
			  function previewSummary(o){
				  var content = summaryEditor.getValue();
				  var html = md.render(content);
				  o.removeClass("glyphicon-eye-open").addClass("glyphicon-eye-close").attr("onclick","inputSummry($(this))");
				  $("#summary-content").hide();
				  $("#summary-rendered").html(html).show();
			  }
			  
			  function inputSummry(o){
				  o.removeClass("glyphicon-eye-close").addClass("glyphicon-eye-open").attr("onclick","previewSummary($(this))");
				  $("#summary-rendered").html('').hide();
				  $("#summary-content").show();
			  }
			  
			  
			  function bold(){
				  var text = editor.getSelection();
				  if(text == ''){
					  editor.replaceRange("****", editor.getCursor());
					  editor.focus();
					  var str="**";
					  var mynum=str.length;
					  var start_cursor = editor.getCursor();
					  var cursorLine = start_cursor.line;
					  var cursorCh = start_cursor.ch;
					  editor.setCursor({line: cursorLine , ch : cursorCh -mynum });
				  } else {
					  editor.replaceSelection("**" + text + "**");
				  }
			  }
			  
			  function italic(){
				  var text = editor.getSelection();
				  if(text == ''){
					  editor.replaceRange("**", editor.getCursor());
					  editor.focus();
					  var str="*";
					  var mynum=str.length;
					  var start_cursor = editor.getCursor();
					  var cursorLine = start_cursor.line;
					  var cursorCh = start_cursor.ch;
					  editor.setCursor({line: cursorLine , ch : cursorCh -mynum });
				  } else {
					  editor.replaceSelection("*" + text + "*");
				  }
			  }
			  
			  function blockQuote(){
				  var text = editor.getSelection();
				  if(text == ''){
					  editor.replaceRange("> ", editor.getCursor());
					  editor.focus();
					  var start_cursor = editor.getCursor();
					  var cursorLine = start_cursor.line;
					  var cursorCh = start_cursor.ch;
					  editor.setCursor({line: cursorLine , ch : cursorCh });
				  } else {
					  editor.replaceSelection("> " + text);
				  }
			  }