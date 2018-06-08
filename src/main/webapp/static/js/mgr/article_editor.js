var editor = (function() {
	var autoParse = true;
	var scrollMap;

	var detectSize = function() {
		var wwidth = $(window).width();
		if (wwidth <= 768) {
			autoParse = false;
			toEditor();
			$("#mobile-style").remove();
			$("head")
					.append(
							"<style type='text/css' id='mobile-style'>.icon {font-size: 30px} #toolbar img{width:32px !important;height:32px !important} .CodeMirror-scroll{margin-top:10px}</style>");
		} else {
			autoParse = true;
			$("#in").css({
				width : '50%',
				top : '30px'
			}).show();
			$("#out").css({
				left : '50%',
				top : 0
			}).show();
			$("#editor-icon").remove();
			$("#preview-icon").remove();
			$("#mobile-style").remove();

			document.addEventListener('drop', function(e) {
				e.preventDefault();
				e.stopPropagation();
				var reader = new FileReader();
				var file = e.dataTransfer.files[0];
				var ext = file.name.split(".").pop().toLowerCase();
				if (ext == "md") {
					reader.onload = function(e) {
						editor.setValue(e.target.result);
					};
					reader.readAsText(file);
				} else if (ext == "html" || ext == 'htm') {
					reader.onload = function(e) {
						editor.setValue(turndownService
								.turndown(e.target.result));
					};
					reader.readAsText(file);
				} else {
					base64Upload(file);
				}
			}, false);

		}
	}

	var toEditor = function() {
		$("#out").hide();
		$("#toolbar").css({
			width : '100%',
			height : '45px'
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
						'<span data-to-preview class="glyphicon glyphicon-eye-open icon" id="preview-icon" title="预览"></span>');
	}

	var toPreview = function() {
		render.md(0);
		$("#in").hide();
		$("#out").css({
			left : '0%',
			top : '45px'
		}).show();
		$("#editor-icon").remove();
		$("#preview-icon").remove();
		$(".icon").hide();
		$("#toolbar img").hide();
		$("#toolbar")
				.append(
						'<span data-to-editor class="glyphicon glyphicon-eye-close icon" id="editor-icon" title="取消预览"></span>');
	}
	
	detectSize();
	
	$(document).on('click','[data-to-preview]',function(){
		toPreview();
	});
	$(document).on('click','[data-to-editor]',function(){
		toEditor();
	});
	var editor = (function(){
		var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
			mode : 'gfm',
			lineNumbers : false,
			matchBrackets : true,
			lineWrapping : true,
		      scrollbarStyle: null,
			theme : 'base16-light',
			dragDrop:false,
		    extraKeys: {"Enter": "newlineAndIndentContinueMarkdownList","Alt-F": "findPersistent","Ctrl-A":"selectAll","Ctrl-S":function(){
		    	showBase();	
		    }}
		});
		
		editor.on('paste',function(editor,evt){
			var clipboardData, pastedData;
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
//		    setTimeout(function(){
//		    	sync.doSync();
//		    },300)
		})
		
		CodeMirror.keyMap.default["Shift-Tab"] = "indentLess";
		CodeMirror.keyMap.default["Tab"] = "indentMore";
		
		
		 editor.on('change', function(e) {
				if (autoParse) {
					render.md(300);
				}
				 if($(window).width() > 768){
					 sync.doSyncAtLine(editor.getCursor('start').line);
					 stat.doStat(1000);
				  }
			});
		return editor;
	})();
	
	var sync = (function() {
	      var oldVForScroll;
	      var building = false;
	      var syncResultScrollAtLine = function(lineNo){
	    	  if (!scrollMap) {
		          buildScrollMap(function(){
					  posTo = scrollMap[lineNo];
						$('#out').stop(true).animate({
					  scrollTop: posTo
					}, 100, 'linear');
					  
				  });
		        } else {
					
					posTo = scrollMap[lineNo];
					$('#out').stop(true).animate({
					  scrollTop: posTo
					}, 100, 'linear');
				}
	      }
	      var syncResultScroll = function() {
	    	  	var lineHeight = parseFloat($('.CodeMirror').css('line-height')),
	            lineNo, posTo;
	        	lineNo = editor.lineAtHeight(editor.getScrollInfo().top, 'local');
	        	syncResultScrollAtLine(lineNo);
	        
	      };



	      var buildScrollMap = function(cb) {
	        if (building) {
	          return;
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
	          for (i = 0; i < linesCount; i++) {
	            _scrollMap.push(-1);
	          }

	          nonEmptyList.push(0);
	          _scrollMap[0] = 0;

	          $('.line').each(function(n, el) {
	            var $el = $(el),
	                t = $el.data('line');
	            if (t === '') {
	              return;
	            }
	            if (t !== 0) {
	              nonEmptyList.push(t);
	            }
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
	          if (editor.getValue() == oldVForScroll) {
	            scrollMap = _scrollMap;
				if(cb){
					cb();
				}
	          }
	        })
	      }

	 	 if($(window).width() > 768){
	 		  editor.on('scroll', function () {
	 		    $('#out').off('scroll');
	 		    editor.on('scroll',syncResultScroll);
	 		  });
	 	  }
		  return {
			  doSync : syncResultScroll,
			  doSyncAtLine : function(line){
				  syncResultScrollAtLine(line)
			  }
		  }
	    })();
	
	var turndownService = (function(){
		var turndownService = new window.TurndownService()
		  turndownService.use(window.turndownPluginGfm.gfm);
		return turndownService;
	})();

	var md = (function() {
		var md = window.markdownit({
			html : true,
			linkify : true,
			typographer : true
		}).use(window.markdownitFootnote);

		var injectLineNumbers = function(tokens, idx, options, env, slf) {
			var line;
			if (tokens[idx].map && tokens[idx].level === 0) {
				line = tokens[idx].map[0];
				tokens[idx].attrJoin('class', 'line');
				tokens[idx].attrSet('data-line', String(line));
			}
			return slf.renderToken(tokens, idx, options, env, slf);
		}
		md.renderer.rules.paragraph_open = md.renderer.rules.heading_open = injectLineNumbers;
		return md;
	})();
	
	var base64Upload = function(f){
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
	
	
	 var render = (function(){
			var t;
			
			return {
				md : function(ms){
					var v = editor.getValue();
					if(t){
						clearTimeout(t);
					}
					t = setTimeout(function(){
						v = md.render(v);
						$("#out").html(v);
						renderCodeBlock();
						scrollMap = null;
					},ms);
				}
			}
		})();
	 
	 var renderCodeBlock = function(){
			var p = false;
			$("#out pre").each(function(){
				var me = $(this);
				if(me.hasClass('prettyprint prettyprinted'))
					return true;
				if(me.find('code').length == 0)
					 return true;
				else{
					p = true;
					me.addClass("prettyprint");
				}
			});
			if(p){
				prettyPrint();
			}
			
		}
	 
	 var stat = (function(){
			var t;
			
			return {
				doStat : function(ms){
					var v = editor.getValue().length;
					$("#stat").text("当前字数："+v+"/200000").show();
					if(t){
						clearTimeout(t);
					}
					t = setTimeout(function(){
						$("#stat").hide();
					},ms);
				}
			}
		})();
	render.md(0);
	return editor;
})();