var cp = (function(editor){
			var from;	
			var to;
			var movedByMouseOrTouch;
			editor.on("mousedown", function () {
				movedByMouseOrTouch = true;
			});
			editor.on("touchstart", function () {
				movedByMouseOrTouch = true;
			});
			editor.on("cursorActivity", function () {
				if (movedByMouseOrTouch) {
                  if(mark){
                    mark.clear();
                  }
					movedByMouseOrTouch = false;
					from = editor.getCursor('from');
					to = editor.getCursor('to');
				}
			});
			var cursor_panel = '<div style="position:absolute;bottom:5px;width:150px;left:calc(50% - 75px);display:none;z-index:9999" class="alpha30" id="_cursor_panel">'
			
			cursor_panel += '<div style="height:26.66%;padding:5px;cursor:pointer">';
			cursor_panel += '<i class="fas fa-copy" data-exec="copy" style="font-size:35px;" title="复制"></i>';
			cursor_panel += '<i class="fas fa-cut" data-exec="cut" style="font-size:35px;margin-left:15px" title="剪切"></i>';
			cursor_panel += '<i class="fas fa-times" data-close style="font-size:35px;float:right" title="关闭"></i>';
			
			cursor_panel += '<div style="clear:both"></div>';
			cursor_panel += '</div>';
			cursor_panel += '<div style="height:26.66%;text-align:center">';
			cursor_panel += '<i class="fas fa-arrow-up" data-arrow="goLineUp" style="font-size:50px;cursor:pointer"></i>'	
			cursor_panel += '</div>';
			cursor_panel += '<div style="height:26.66%">'
			cursor_panel += '<i class="fas fa-arrow-left" data-arrow="goCharLeft" style="font-size:50px;float:left;cursor:pointer;margin-right:20px"></i>';
			cursor_panel += '<i class="fas fa-arrow-right" data-arrow="goCharRight" style="font-size:50px;float:right;cursor:pointer"></i>';
			cursor_panel += '<div style="clear:both"></div>';
			cursor_panel += '</div>';
			cursor_panel += '<div style="height:26.66%;text-align:center">';
			cursor_panel += '<i class="fas fa-arrow-down" data-arrow="goLineDown" style="font-size:50px;cursor:pointer"></i>';
			cursor_panel += '</div>';
			cursor_panel += '</div>';		
			
			$(cursor_panel).appendTo($('#in'));
			
			
			var t ;
			
			
			var cs;
			var cs_t;
			

			$("#_cursor_panel").on('click','[data-arrow]',function(){
				var min = 300;
				var action = $(this).data('arrow');
				move(action);
			});
			
			//var _move = function(min,action){
			//	cs = setInterval(function(){
			//		move(action);
			//		if(min > 50){
			//			clearInterval(cs);
			//			_move(min-25,action);
			//		}
			//	},min)
			//}
			var mark;
			var move = function(action){
				editor.setCursor(to);
				editor.execCommand(action);
				to = editor.getCursor('from');
              if(mark){
              	mark.clear();
              }
				if(from.line > to.line || (from.line == to.line && from.ch > to.ch)){
				mark = editor.markText(to,from, {className: "styled-background"});
			  }else{
				mark = editor.markText(from,to, {className: "styled-background"});
			  }
				if(t){
					clearTimeout(t);
				}
				t = setTimeout(function(){
					editor.focus();
				},500);

			}
			
			//$("#_cursor_panel").on('mouseup touchend','[data-arrow]',function(){
			//	if(cs_t)
			//		clearTimeout(cs_t);
			//	if(cs)
			//		clearInterval(cs);
			//
			//});
			
			$("#_cursor_panel").on('click','[data-exec]',function(){
              	if(from.line == to.line && from.ch == to.ch){
                	hide();
                    return ;
                }
              
			  editor.focus();
              
              if(from.line > to.line || (from.line == to.line && from.ch > to.ch)){
				editor.setSelection(to,from,{
					scroll:true
				});
			  }else{
				editor.setSelection(from,to,{
					scroll:true
				});
			  }
				
				if(document.execCommand('copy')){
					if($(this).data('exec') == 'cut'){
						editor.replaceRange("", from,to);
					}
				}else{
					alert("拷贝失败");
				}
				hide();
			});
			
			$("#_cursor_panel").on('click','[data-close]',function(){
				
				hide();
			});
			
			var _toolbar = config.toolbar;
			var hide = function(){
				$("#_cursor_panel").hide();
				editor.setOption('readOnly',false);
				if(_toolbar){
					config.toolbar = true;
				}
              if(mark){
              	mark.clear();
              }
              editor.setCursor(to);
              editor.focus();
			}
			
			var show = function(){
				from = editor.getCursor('from');
				to = editor.getCursor('to');
				$("#_cursor_panel").show();
				editor.setOption('readOnly',true);
				config.toolbar = false;
					if(inner_bar){
						inner_bar.remove();
					}
			}

			return {
				
				show : function(){
					show();
				},
				
				hide : function(){
					hide();
				}
			}
})(editor);