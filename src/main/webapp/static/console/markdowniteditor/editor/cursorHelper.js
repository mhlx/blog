var cursorHelper = (function(editor){
	'use strict';
	var cursor_panel = '<div id="cursorHelper" style="position:absolute;bottom:5px;width:150px;left:calc(50% - 75px);display:none;z-index:9999" class="alpha30" >'		
	cursor_panel += '<div style="height:26.66%;padding:5px;cursor:pointer">';
	cursor_panel += '<i class="fas fa-times" data-close style="font-size:35px" title="关闭"></i>';		
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
	
	function CursorHelper(){
		var from;
		var to;
		var mark;
		var t;
		var movedByMouseOrTouch;
		var div = document.createElement('div');
		div.innerHTML = cursor_panel;
		this.panel = div.firstChild;
		document.getElementById("in").appendChild(this.panel);
		this.$panel = $(this.panel);
		var helper = this;
		this.$panel.on('click','[data-arrow]',function(){
			var action = $(this).data('arrow');
			helper.move(action);
		});
		this.$panel.on('click','[data-close]',function(){
			helper.close();	
			helper.setSelection();
		});
		editor.on("mousedown", function () {
			helper.movedByMouseOrTouch = true;
		});
		editor.on("touchstart", function () {
			helper.movedByMouseOrTouch = true;
		});
		editor.on("cursorActivity", function () {
			if (helper.movedByMouseOrTouch) {
			  if(helper.mark){
				helper.mark.clear();
			  }
				helper.movedByMouseOrTouch = false;
				helper.from = editor.getCursor('from');
				helper.to = editor.getCursor('to');
			}
		});
	}
	
	CursorHelper.prototype.bind = function(name,handle){
		this.panel.addEventListener(name,handle);
	}
	
	CursorHelper.prototype.unbind = function(name,handle){
		this.panel.removeEventListener(name,handle);
	}
	
	CursorHelper.prototype.move = function(action){
		editor.setCursor(this.to);
		editor.execCommand(action);
		this.to = editor.getCursor('from');
		if(this.mark){
			this.mark.clear();
		}
		if(this.from.line > this.to.line || (this.from.line == this.to.line && this.from.ch > this.to.ch)){
			this.mark = editor.markText(this.to,this.from, {className: "styled-background"});
		}else{
			this.mark = editor.markText(this.from,this.to, {className: "styled-background"});
		}
		if(this.t){
			clearTimeout(this.t);
		}
		this.t = setTimeout(function(){
			editor.focus();
		},500);
	}
		
	CursorHelper.prototype.open = function(){
		this.from = editor.getCursor('from');
		this.to = editor.getCursor('to');
		this.$panel.show();
		editor.setOption('readOnly',true);
		var  openEvent = new CustomEvent('open', {
		  bubbles: true
		});
		this.$panel[0].dispatchEvent(openEvent);
	}	
	
	CursorHelper.prototype.close = function(){
		this.$panel.hide();
		editor.setOption('readOnly',false);

		if(this.mark){
			this.mark.clear();
		}
		
		editor.setCursor(this.to);
		editor.focus();
		var  closeEvent = new CustomEvent('close', {
		  bubbles: true
		});
		this.$panel[0].dispatchEvent(closeEvent);
	}	
		
	CursorHelper.prototype.setSelection = function(){
	  editor.focus();  
	  if(this.from.line > this.to.line || (this.from.line == this.to.line && this.from.ch > this.to.ch)){
		editor.setSelection(this.to,this.from);
	  }else{
		editor.setSelection(this.from,this.to);
	  }
	}
	return new CursorHelper();
})(editor);