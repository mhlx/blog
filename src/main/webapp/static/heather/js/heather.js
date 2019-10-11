var Heather = (function(){
	
	var lazyRes = {
        mermaid_js: "js/mermaid.min.js",
        katex_css: "katex/katex.min.css",
        katex_js: "katex/katex.min.js"
    }
	
	var commands = {};
	
	String.prototype.replaceAll = function(search, replacement) {
		var target = this;
		return target.split(search).join(replacement);
	}
	
	function Editor(textarea,config){
		config = config || {};
		this.eventHandlers = [];
		this.editor = createEditor(textarea,config);
		this.markdownParser = new MarkdownParser(config.markdownParser);
		this.toolbar = new Toolbar(this,config);
		this.commandBar = new CommandBar(this,config);
		this.tooltip = new Tooltip(this.editor,config);
		this.rendered = true;
		this.partPreview = new PartPreview(this,config);
		this.config = config;
		initKeyMap(this);
		handleDefaultEditorEvent(this);
	}
	
	Editor.prototype.getValue = function(){
		return this.editor.getValue();
	}
	
	Editor.prototype.setValue = function(value){
		this.editor.setValue(value);
	}	
	/*
	是否启用了文件上传
	*/
	Editor.prototype.isFileUploadEnable = function(){
		var config = this.config.upload;
		return !Util.isUndefined(config) && !Util.isUndefined(config.url) && !Util.isUndefined(config.uploadFinish);
	}
	
	/*
	根据markdown行号获取对应的html节点
	一个行号可能对应多个节点，例如：
	0. - [ ] todo list
	1.     <div></div>
	那么行号1对应的节点应该为 [ul,li,div]
	*/
	Editor.prototype.getNodesByLine = function(line){
		if(!this.node) return [];
		var nodes = [];
		var addNode = function(node){
			for(const dataLine of node.querySelectorAll('[data-line]')){
				var _startLine = parseInt(dataLine.dataset.line);
				var _endLine = parseInt(dataLine.dataset.endLine);
				if (_startLine <= line && _endLine > line) {
					var clone = dataLine.cloneNode(true);
					clone.startLine = _startLine;
					clone.endLine = _endLine;
					nodes.push(clone);
					addNode(clone);
					break;
				}
			}
		}
		addNode(this.node);
		return nodes;
	}
	
	/*
	当前是否处于预览模式
	*/
	Editor.prototype.isPreview = function(){
		return !Util.isUndefined(this.previewState);
	}
	
	Editor.prototype.execCommand = function(name){
		var handler = commands[name];
		if(!Util.isUndefined(handler))
			handler(this);
	}
	
	/*
	设置是否预览
	*/
	Editor.prototype.setPreview = function(preview){
		if(preview === true && this.previewState) return;
		if(preview !== true && !this.previewState) return;
		if(preview === true){
			
			var elem = this.editor.getWrapperElement();
			this.editor.setOption('readOnly',true);
			var div = document.createElement('div');
			div.classList.add('markdown-body');
			div.classList.add('heather_preview');
			div.innerHTML = this.node ? this.node.innerHTML : '';
			
			var close = document.createElement('div');
			close.classList.add('heather_preview_close');
			close.innerHTML = '<i class="fas fa-eye-slash heather_icon"></i>';
			div.appendChild(close);
			
			div.addEventListener('scroll',function(){
				close.style.top = (this.scrollTop+10)+'px';
			});
			
			var me = this;
			close.addEventListener('click',function(){
				me.setPreview(false);
			})
			
			elem.appendChild(div);
			renderKatexAndMermaid(div);
			this.previewState = {
				element : div,
				cursor:me.editor.getCursor()
			}
			triggerEvent(this,'previewChange',true);
		} else {
			this.previewState.element.remove();
			this.editor.setOption('readOnly',false);
			this.editor.focus();
			this.editor.setCursor(this.previewState.cursor);
			this.previewState = undefined;
			triggerEvent(this,'previewChange',false);
		}
	}
	
	Editor.prototype.getToolbar = function(){
		return this.toolbar;
	}
	
	Editor.prototype.isFullscreen = function(){
		return this.fullscreen === true;
	}
	
	Editor.prototype.setFullscreen = function(fullscreen){
		if(fullscreen === true && this.editor.getOption('fullScreen')) return;
		if(fullscreen !== true && !this.editor.getOption('fullScreen')) return;
		if(fullscreen === true){
			this.editor.setOption("fullScreen", true);
			this.fullscreen = true;
			triggerEvent(this,'fullscreenChange',true);
		} else {
			this.editor.setOption("fullScreen", false);
			this.fullscreen = false;
			triggerEvent(this,'fullscreenChange',false);
		}
	}
	
	Editor.prototype.on = function(eventName, handler) {
		if(eventName.startsWith("editor.")){
			eventName = eventName.substring(eventName.indexOf('.')+1);
			this.editor.on(eventName,handler);
			return ;
		}
		this.eventHandlers.push({
			name : eventName,
			handler : handler
		});
	}

	Editor.prototype.off = function(eventName, handler) {
		if(eventName.startsWith("editor.")){
			eventName = eventName.substring(eventName.indexOf('.')+1);
			this.editor.off(eventName,handler);
			return ;
		}
		for(var i=this.eventHandlers.length-1;i>=0;i--){
			var eh = this.eventHandlers[i];
			if(eh.handler === handler && eh.name === eventName){
				this.eventHandlers.splice(i,1);
				break;
			}
		}
	}
	
	Editor.prototype.setFocus = function(focus) {
		if(this.focusState && focus === true) return;
		if(!this.focusState && focus !== true) return;
		if(focus === true){
			var me = this;
			this.focusState = {
				changeHandler : function(cm){
					var pos = cm.cursorCoords(true,'local');
					var height = cm.getWrapperElement().clientHeight/2;
					cm.getWrapperElement().querySelector('.CodeMirror-lines').style.paddingBottom = height+'px';
					cm.scrollTo(null, pos.top - height + me.toolbar.getHeight()); 
				},
				fullscreenChange: function(fs){
					me.focusState.changeHandler(me.editor);
				}
			}
			this.on('fullscreenChange',this.focusState.fullscreenChange);
			this.editor.on('change',this.focusState.changeHandler);
			triggerEvent(this,'focusChange',true);
		} else {
			this.off('fullscreenChange',this.focusState.fullscreenChange);
			this.editor.off('change',this.focusState.changeHandler);
			this.focusState = undefined;
			this.editor.getWrapperElement().querySelector('.CodeMirror-lines').style.paddingBottom = '';
			triggerEvent(this,'focusChange',false);
		}
	}
	
	Editor.prototype.isFocus = function(){
		return !Util.isUndefined(this.focusState);
	}
	
	Editor.prototype.openSelectionHelper = function(){
		if(Util.mobile && !this.isPreview()){
			this.closeSelectionHelper();
			this.selectionHelper = new SelectionHelper(this);
		}
	}
	Editor.prototype.hasSelectionHelper = function(){
		return !Util.isUndefined(this.selectionHelper);
	}	
	Editor.prototype.closeSelectionHelper = function(){
		if(this.selectionHelper){
			this.selectionHelper.remove();
			this.selectionHelper = undefined;
		}
	}
	
	Editor.prototype.addKeyMap = function(keyMap) {
		var me = this;
		for (const key in keyMap) {
			var v = keyMap[key];
			if(typeof v === 'string'){
				var value = v;
				keyMap[key] = function(){
					me.execCommand(value);
				}
			}
		}
		this.editor.addKeyMap(keyMap);
	}	
	
	function handleDefaultEditorEvent(heather){
		var editor = heather.editor;
		//auto render
		var doRender = function(){
			var value = editor.getValue();
			heather.node = Util.parseHTML(heather.markdownParser.render(value)).body;
			triggerEvent(heather,'rendered',value,heather.node.cloneNode(true).childNodes);
			heather.rendered = true;
		}
		if(editor.getValue() != '')
			doRender();
		editor.on('change',function(cm,change){
			heather.rendered = false;
			if(heather.renderTimer){
				clearTimeout(heather.renderTimer);
			}
			heather.renderTimer = setTimeout(function(){
				doRender();
			},Util.getDefault(heather.config.autoRenderMill,300));	
		});	
		//change task list status
		editor.on('mousedown',function(cm,e){
			if(isWidget(e.target,cm)){
				e.codemirrorIgnore  = true;
				return ;
			}
			var x = e.clientX;
			var y = e.clientY;
			changeTastListStatus(heather,x,y);
		})
		
		editor.on('touchstart',function(cm,e){
			if(isWidget(e.target,cm)){
				e.codemirrorIgnore  = true;
				return ;
			}
			var x = e.touches[0].clientX;
			var y = e.touches[0].clientY;
			changeTastListStatus(heather,x,y);
		});
		//file upload 
		editor.on('paste', function(editor, evt) {
			var clipboardData, pastedData;
			clipboardData = evt.clipboardData || window.clipboardData;
			var files = clipboardData.files;
			if (files.length > 0 && heather.isFileUploadEnable()) {
				var f = files[0];
				var type = f.type;
				if (type.indexOf('image/') == -1) {
					return;
				}
				evt.preventDefault();
				evt.stopPropagation();
				f.from = 'paste';
				new FileUpload(f,heather, heather.config.upload).start();
			}
		});
		
		editor.on('drop',function(cm,e){
			var files = e.dataTransfer.files;
			if (files.length > 0) {
				var file = files[0];
				if(file.type.startsWith('text/') || file.name.toLowerCase().endsWith(".md")){
					e.preventDefault();
					e.stopPropagation();
					var reader = new FileReader();
					reader.readAsText(file);
					reader.onload = function (evt) {
						var text = evt.target.result;
						cm.replaceSelection(text);
					}
					
				} else if(heather.isFileUploadEnable()){
					e.preventDefault();
					e.stopPropagation();
					file.from = 'drop';
					new FileUpload(file,heather, heather.config.upload).start();
				}
			}
		});
	}
	
	function isWidget(target,cm){
		for(const widget of cm.getWrapperElement().querySelectorAll('[data-widget]')){
			if(widget.contains(target)) 
				return true;
		}
		return false;
	}
		
	function initKeyMap(heather) {
		var keyMap = Util.mac ? {
			"Ctrl-B": 'bold',
			"Ctrl-I": 'italic',
			"Shift-Cmd-T": 'table',
			"Ctrl-H": 'heading',
			"Ctrl-L": 'link',
			"Ctrl-Q": 'quote',
			"Shift-Cmd-B": 'codeBlock',
			"Shift-Cmd-U": 'uncheck',
			"Shift-Cmd-I": 'check',
			"Alt-/": 'commands',
			"Cmd-Enter": function() {
				heather.setFullscreen(!heather.isFullscreen());
			},
			"Ctrl-P": function() {
				heather.setPreview(!heather.isPreview())
			},
			"Tab":function(){
				if(!tab(heather)){
					heather.editor.execCommand('indentMore');
				}
			},
			"Shift-Tab":function(){
				heather.editor.execCommand('indentLess')
			}
		} : {
			"Ctrl-B": 'bold',
			"Ctrl-I": 'italic',
			"Alt-T": 'table',
			"Ctrl-H": 'heading',
			"Ctrl-L": 'link',
			"Ctrl-Q": 'quote',
			"Alt-B": 'codeBlock',
			"Alt-U": 'uncheck',
			"Alt-I": 'check',
			"Alt-/": 'commands',
			"Ctrl-Enter": function() {
				heather.setFullscreen(!heather.isFullscreen());
			},
			"Ctrl-P": function() {
				heather.setPreview(!heather.isPreview())
			},
			"Tab":function(){
				if(!tab(heather)){
					heather.editor.execCommand('indentMore');
				}
			},
			"Shift-Tab":function(){
				heather.editor.execCommand('indentLess')
			}
		}
		heather.addKeyMap(keyMap);
	}
	
	function triggerEvent(heather,name,... args){
		for(var i=0;i<heather.eventHandlers.length;i++){
			var eh = heather.eventHandlers[i];
			if(eh.name === name){
				try{
					var handler = eh.handler;
					handler.apply(heather,args);
				}catch(e){}
			}
		}	
	}
	
	
	function createEditor(textarea,config){
		config = config.editor || {};
		config.inputStyle = 'contenteditable';
		config.mode = {name : 'gfm'};
		config.lineNumbers = true;
		config.lineWrapping = true;
		config.dragDrop = true;
		config.extraKeys = (config.extraKeys || {});
		config.extraKeys['Enter'] = 'newlineAndIndentContinueMarkdownList';
		config.autofocus = true;
		var editor =  CodeMirror.fromTextArea(textarea,config);
		return editor;
	}
	
	var SelectionHelper = (function(){
		
		function SelectionHelper(heather){
			
			var editor = heather.editor;
			
			var html = '';
            html += '<div style="height:26.66%;text-align:center">';
            html += '<i class="fas fa-arrow-up" data-arrow="goLineUp" style="font-size:50px;cursor:pointer"></i>'
            html += '</div>';
            html += '<div style="height:26.66%">'
            html += '<i class="fas fa-arrow-left" data-arrow="goCharLeft" style="font-size:50px;float:left;cursor:pointer;margin-right:20px"></i>';
            html += '<i class="fas fa-arrow-right" data-arrow="goCharRight" style="font-size:50px;float:right;cursor:pointer"></i>';
            html += '<div style="clear:both"></div>';
            html += '</div>';
            html += '<div style="height:26.66%;text-align:center">';
            html += '<i class="fas fa-arrow-down" data-arrow="goLineDown" style="font-size:50px;cursor:pointer"></i>';
            html += '</div>';
			var div = document.createElement('div');
			div.classList.add('heather_selection_helper');
			div.style.visibility = 'hidden';
			div.setAttribute('data-widget','');
			div.innerHTML = html;
			editor.addWidget({line:0,ch:0},div);
			div.style.width = div.clientHeight+'px';
			div.style.visibility = 'visible';
			
			this.start = editor.getCursor(true);
			this.end = editor.getCursor(false);
			this.marked = editor.markText(this.start,this.end, {
				className: "heather_selection_marked"
			});
			var me = this;
			
			this.cursorActivityHandler = function(cm){
				if (me.movedByMouseOrTouch === true) {
					me.movedByMouseOrTouch = false;
					me.start = editor.getCursor();
				}
				div.style.top = (cm.cursorCoords(false,'local').top+cm.defaultTextHeight())+'px';
			};
			
			this.cursorActivityHandler(editor);
			
			this.movedHandler = function(cm,e){
				if(isWidget(e.target,cm)){
					e.codemirrorIgnore  = true;
					return ;
				}
				me.movedByMouseOrTouch = true;
				me.end = undefined;
				if(me.marked){
					me.marked.clear();
				}
			}
			editor.setOption('readOnly','nocursor');
			div.addEventListener('click',function(e){
				if(e.target.hasAttribute('data-arrow')){
					var action = e.target.dataset.arrow;
					if(me.end){
						editor.setCursor(me.end);
					}
					editor.execCommand(action);
					me.end = editor.getCursor();
					
					var cursors = me.getCursors();
					
					if(me.marked){
						me.marked.clear();
					}
					
					me.marked = editor.markText(cursors.start,cursors.end, {
						className: "heather_selection_marked"
					});
				}
			});
			
			
			editor.on('cursorActivity',this.cursorActivityHandler);
			editor.on("mousedown", this.movedHandler);
            editor.on("touchstart", this.movedHandler);
			
			heather.commandBar.setKeepHidden(true);
			heather.partPreview.setKeepHidden(true);
			
			this.previewChangeHandler = function(){
				me.remove();
			}
			
			heather.on('previewChange',this.previewChangeHandler);
			
			this.div = div;
			this.heather = heather;
		}
		
		SelectionHelper.prototype.getCursors = function(){
			var start;
			var end ;
			if(this.end.line > this.start.line || (this.end.line == this.start.line && this.end.ch >= this.start.ch)){
				start = this.start;
				end = this.end;
			}  else {
				start = this.end;
				end = this.start;
			}
			return {
				start : start,
				end : end
			}
		}
		
		SelectionHelper.prototype.remove = function(){
			if (this.marked) {
				this.marked.clear();
			}
			var editor = this.heather.editor;
			if(this.end){
				var cursors = this.getCursors();
				editor.setSelection(cursors.start,cursors.end);
			}
			this.div.remove();
			editor.on('cursorActivity',this.cursorActivityHandler);
			editor.on("mousedown", this.movedHandler);
            editor.on("touchstart", this.movedHandler);
			this.heather.off('previewChange',this.previewChangeHandler);
			this.heather.commandBar.setKeepHidden(false);
			this.heather.partPreview.setKeepHidden(false);
			editor.setOption('readOnly',false);
			editor.focus();
		}
		
		return SelectionHelper;
	})();
	
	var Bar = (function() {

		function Bar(element) {
			element.setAttribute('data-toolbar', '');
			this.element = element;
			this.keepHidden = false;
		}


		Bar.prototype.hide = function() {
			this.element.style.visibility = 'hidden';
			this.hidden = true;
		}
		Bar.prototype.getElement = function() {
			return this.element;
		}
		Bar.prototype.remove = function() {
			this.element.remove();
		}

		Bar.prototype.height = function() {
			return this.element.clientHeight;
		}

		Bar.prototype.width = function() {
			return this.element.clientWidth;
		}

		Bar.prototype.length = function() {
			return this.element.childNodes.length;
		}

		Bar.prototype.show = function() {
			if (this.keepHidden) {
				return;
			}
			this.element.style.visibility = 'visible';
			this.hidden = false;
		}

		Bar.prototype.addItem = function(item) {
			insertItem(this, item, this.items.length);
		}

		Bar.prototype.clear = function() {
			this.element.innerHTML = '';
		}

		function createElement(icon, handler) {
			var i = document.createElement('i');
			i.setAttribute('class', icon);
			i.setAttribute('style', 'cursor: pointer;margin-right:20px');
			if (handler) {
				var defaultEvent = 'click';
				var isFunction = typeof handler === "function";
				var event = isFunction ? defaultEvent : (handler.event || defaultEvent);

				i.addEventListener(event, function(e) {
					e.preventDefault();
					e.stopPropagation();

					if (isFunction) {
						handler(i);
					} else {
						handler.handler(i);
					}

				})
			}
			return i;
		}

		Bar.prototype.insertIcon = function(clazz, handler, index, callback) {
			var newIcon = createElement(clazz, handler);
			if (callback) callback(newIcon);
			this.insertElement(newIcon, index);
		}

		Bar.prototype.insertElement = function(element, index) {
			var toolbar = this.element;
			if (index >= this.length()) {
				toolbar.appendChild(element);
			} else {
				if (index <= 0) {
					toolbar.insertBefore(element, toolbar.childNodes[0])
				} else {
					toolbar.insertBefore(element, toolbar.childNodes[index])
				}
			}
		}

		Bar.prototype.addElement = function(element) {
			this.insertElement(element, this.length());
		}

		Bar.prototype.addIcon = function(clazz, handler, callback) {
			this.insertIcon(clazz, handler, this.length(), callback);
		}

		Bar.prototype.removeElement = function(deleteChecker) {
			var elements = this.element.childNodes;
			for (var i = elements.length - 1; i >= 0; i--) {
				var element = elements[i];
				if (deleteChecker(element)) {
					element.remove();
				}
			}
		}

		return Bar;
	})();	
	
	
	var Toolbar = (function(){
		
		function Toolbar(heather,config){
			var cm = heather.editor;
			var div = document.createElement('div');
			div.classList.add('heather_toolbar_bar');
			div.setAttribute('data-widget','');
			div.style.visibility = 'hidden';
			cm.addWidget({line:0,ch:0},div);
			div.style.top = '0px';
			cm.on('scroll',function(cm){
				div.style.top = cm.getScrollInfo().top+'px';
			});
			this.heather = heather;
			this.cm = cm;
			this.bar = new Bar(div);
		}
		
		Toolbar.prototype.addElement = function(element){
			this.bar.addElement(element);
			calcMarginTop(this);
		}
		
		Toolbar.prototype.insertElement = function(element,index){
			this.bar.insertElement(element,index);
			calcMarginTop(this);
		}	
		
		Toolbar.prototype.addIcon = function(icon,handler,callback){
			this.bar.addIcon(icon,handler,callback);
			calcMarginTop(this);
		}
		
		Toolbar.prototype.insertIcon = function(clazz, handler, index, callback){
			this.bar.addIcon(clazz, handler, index, callback);
			calcMarginTop(this);
		}	
		
		Toolbar.prototype.removeElement = function(deleteChecker){
			this.bar.removeElement(deleteChecker);
			calcMarginTop(this);
		}
		
		Toolbar.prototype.clear = function(){
			this.bar.clear();
			calcMarginTop(this);
		}
	
		Toolbar.prototype.hide = function(){
			this.cm.getWrapperElement().querySelector('.CodeMirror-code').style.marginTop = '';
			this.bar.getElement().style.visibility = 'hidden';
		}
		
		Toolbar.prototype.show = function(){
			calcMarginTop(this);
		}
		
		Toolbar.prototype.getHeight = function(){
			return this.bar.length() == 0 ? 0 : this.bar.height();
		}
		
		function calcMarginTop(toolbar){
			if(toolbar.bar.length() == 0){
				toolbar.cm.getWrapperElement().querySelector('.CodeMirror-code').style.marginTop = '';
				toolbar.bar.getElement().style.visibility = 'hidden';
				return ;
			}
			toolbar.cm.getWrapperElement().querySelector('.CodeMirror-code').style.marginTop = toolbar.bar.getElement().clientHeight+'px';
			toolbar.bar.getElement().style.visibility = 'visible';
			this.heather.commandBar.rePosition();
			this.heather.partPreview.rePosition();
		}
		
		return Toolbar;
	})();
	
	var CommandBar = (function(){
		
		function CommandBar(heather,config){
			var me = this;
			this.cursorActivityListener = function(cm){
				if(!me.bar){
					me.bar = createBar(cm,heather);
					me.bar.setAttribute('data-widget','');
					cm.addWidget({line:0,ch:0},me.bar);
				}
				me.rePosition();
			}
			this.cm = heather.editor;
			this.heather = heather;
			if(config.commandBarEnable !== false)
				this.enable();
		}
		
		CommandBar.prototype.rePosition = function(){
			if(!this.bar){
				return ;
			}
			var cm = this.cm;
			var pos = cm.cursorCoords(true,'local');
			if(this.keepHidden !== true)
				this.bar.style.visibility = 'visible';
			else
				this.bar.style.visibility = 'hidden';
			var toolbarHeight = this.heather.toolbar.getHeight();
			var top = pos.top - cm.getScrollInfo().top - toolbarHeight;
			var distance = 2*cm.defaultTextHeight();
			if(top > distance+this.bar.clientHeight){
				this.bar.style.top = (pos.top - distance - this.bar.clientHeight)+'px';
			} else {
				this.bar.style.top = (pos.top + distance)+'px';
			}
		}
		
		CommandBar.prototype.enable = function(){
			this.cursorActivityListener(this.cm);
			this.cm.on('cursorActivity',this.cursorActivityListener)
		}	
		
		CommandBar.prototype.disable = function(){
			if(this.bar){
				this.bar.remove();
				this.bar = undefined;
			}
			this.cm.off('cursorActivity',this.cursorActivityListener)
		}
		
		CommandBar.prototype.setKeepHidden = function(keepHidden){
			if(keepHidden === true && this.bar)
				this.bar.style.visibility = 'hidden';
			if(keepHidden !== true && this.bar)
				this.bar.style.visibility = 'visible';
			this.keepHidden = keepHidden === true;
		}	
		
		function createBar(cm,heather){
			var div = document.createElement('div');
			div.classList.add('heather_command_bar')
			var bar = new Bar(div);
			bar.addIcon('fas fa-heading heather_icon', function() {
				heather.execCommand('heading')
			});
			bar.addIcon('fas fa-bold heather_icon', function() {
				heather.execCommand('bold')
			});
			
			bar.addIcon('fas fa-italic heather_icon',function(){
				heather.execCommand('italic')
			});
			
			bar.addIcon('fas fa-quote-left heather_icon', function() {
				heather.execCommand('quote');
			})
			
			bar.addIcon('fas fa-strikethrough heather_icon', function() {
				heather.execCommand('strikethrough');
			})
			
			bar.addIcon('fas fa-link heather_icon', function() {
				heather.execCommand('link');
			})
			
			bar.addIcon('fas fa-code heather_icon', function() {
				heather.execCommand('code');
			})

			bar.addIcon('fas fa-file-code heather_icon', function() {
				heather.execCommand('codeBlock');
			})
			
			bar.addIcon('far fa-square heather_icon', function() {
				heather.execCommand('uncheck');
			})
			
			bar.addIcon('far fa-check-square heather_icon', function() {
				heather.execCommand('check');
			})
			
			bar.addIcon('fas fa-table heather_icon', function() {
				heather.execCommand('table');
			})

			bar.addIcon('fas fa-undo heather_icon', function() {
				heather.editor.execCommand('undo');
			})
			
			bar.addIcon('fas fa-redo heather_icon', function() {
				heather.editor.execCommand('redo');
			})
			

			div.addEventListener('click',function(e){
				var cursor = cm.coordsChar({left:e.clientX, top:e.clientY}, 'window');
				cm.focus();
				cm.setCursor(cursor);
			});
			return div;
		}
		
		return CommandBar;
	})();
	
	var PartPreview = (function() {
		
		function PartPreview(heather,config) {
			this.config = config || {};
			this.heather = heather;
			this.editor = heather.editor;
			if(this.config.partPreviewEnable !== false){
				this.enable();
			}
		}

		PartPreview.prototype.enable = function() {
			var editor = this.editor;
			var me = this;

			var changeHandler = function(cm) {
				if(me.disable === true) return ;
				if (me.disableCursorActivity !== true) {
					me.disableCursorActivity = true;
				}
			}

			var cursorActivityHandler = function() {
				me.widget.hide();
				if(me.disable === true || me.keepHidden === true || me.disableCursorActivity === true) return ;
				me.widget.update();
			}

			this.widget = new Widget(this);

			var afterRenderHandler = function() {
				if(me.disable === true || me.disableCursorActivity !== true) return ;
				me.disableCursorActivity = false;
				if(me.keepHidden !== true)
					me.widget.update();
			}
			this.editor.on('cursorActivity', cursorActivityHandler);
			this.editor.on('change', changeHandler);
			this.heather.on('rendered', afterRenderHandler);
			this.afterRenderHandler = afterRenderHandler;
			this.changeHandler = changeHandler;
			this.cursorActivityHandler = cursorActivityHandler;
		}

		PartPreview.prototype.disable = function() {
			if (this.widget) {
				this.widget.remove();
			}
			this.editor.off('cursorActivity', this.cursorActivityHandler);
			this.editor.off('change', this.changeHandler);
			this.heather.off('rendered', this.afterRenderHandler);
		}
		
		PartPreview.prototype.hidden = function(){
			if (this.widget) {
				this.widget.hide();
			}
		}
		
		PartPreview.prototype.rePosition = function(){
			if (this.widget) {
				this.widget.update();
			}
		}
		
		PartPreview.prototype.setKeepHidden = function(keepHidden){
			if(keepHidden === true)
				if(this.widget)
					this.widget.remove();
			else
				this.widget.update();
			this.keepHidden = keepHidden;
		}
		
		var Widget = (function() {
			
			var Widget = function(preview) {
				this.preview = preview;
			}

			Widget.prototype.create = function() {
				var editor = this.preview.editor;
				var status = editor.selectionStatus();
				
				var nodes;
				if(status.selected == ''){
					var nodeStatus = getNodeStatus(this.preview.heather);
					if(nodeStatus == null) return ;
					nodes = [nodeStatus.node];
				} else if(this.preview.config.renderSelected === true){
					nodes = Util.parseHTML(this.preview.heather.markdownParser.render(status.selected)).body.childNodes;
				}
				
				if(!nodes || nodes.length == 0) return ;
					
				var me = this;
				var div = document.createElement('div');
				div.classList.add('markdown-body');
				div.classList.add('heather_inline_preview');
				div.style.visibility = 'hidden';
				
				for(const node of nodes){
					div.appendChild(node);
				}
				
				div.setAttribute('data-widget','');
				
				
				div.addEventListener('click',function(e){
					if(me.preview.disable === true) return ;
					var cursor = editor.coordsChar({left:e.clientX, top:e.clientY}, 'window');
					editor.focus();
					editor.setCursor(cursor);
				});
				
				this.widget = div;
				var imgs = div.querySelectorAll('img');
				var hasImage = imgs.length > 0;
				
				if(hasImage){
					var len = imgs.length,
						counter = 0;
						
					[].forEach.call( imgs, function( img ) {
						img.addEventListener( 'load', incrementCounter, false );
					});

					function incrementCounter() {
						counter++;
						if (counter === len && div === me.widget) {
							me.show();
							me.scrollContent(nodeStatus);
						}
					}
				}
				
				editor.addWidget({
					line:0,ch:0
				},this.widget)
				
				renderKatexAndMermaid(this.widget,this.preview.config);
				if(!hasImage){
					this.show();
					this.scrollContent(nodeStatus);
				}
			}

			Widget.prototype.update = function() {
				this.remove();
				this.create();
			}
			
			Widget.prototype.hide = function() {
				if (this.widget)
					this.widget.style.visibility = 'hidden';
			}

			Widget.prototype.show = function() {
				if (this.widget) {
					this.updatePosition();
					if(this.keepHidden !== true)
						this.widget.style.visibility = 'visible';
				}
			}

			Widget.prototype.remove = function() {
				if (this.widget) 
					this.widget.remove();
			}
			
			Widget.prototype.scrollContent = function(nodeStatus) {
				if(nodeStatus == null) return ;
				var rootNode = this.widget.firstChild;
				var editor = this.preview.editor;
				var line = editor.getCursor().line;
				var startTop = editor.cursorCoords({line:nodeStatus.startLine,ch:0}, 'local').top;
				var endLine = Math.min(editor.lineCount()-1,nodeStatus.endLine);
				var endTop= editor.cursorCoords({line:endLine,ch:editor.getLine(endLine).length}, 'local').top;
				var currentTop = editor.cursorCoords(editor.getCursor(), 'local').top;
				var markdownHeight = endTop - startTop;
				var p = (Math.max(currentTop - startTop-50,0))/markdownHeight;
				var h = rootNode.clientHeight*p;
				this.widget.scrollTop = h;
			}
			
			
			Widget.prototype.updatePosition = function(){
				var editor = this.preview.editor;
				var pos = editor.cursorCoords(true,'local');
				var bar = this.preview.heather.commandBar.bar;
				var toolbarHeight = this.preview.heather.toolbar.getHeight();
				var top = pos.top - editor.getScrollInfo().top - toolbarHeight;
				var distance = 2*editor.defaultTextHeight()+(bar ? 5 : 0);
				var height = (bar ? bar.clientHeight : 0) + this.widget.clientHeight;
				if(top > height + distance){
					this.widget.style.top = (pos.top - distance - height) + 'px';
				} else {
					if(bar){
						if(pos.top - bar.offsetTop - bar.clientHeight < 0){
							this.widget.style.top =  (pos.top + distance + bar.clientHeight) + 'px';
							return ;
						}
					}
					this.widget.style.top = (pos.top + distance) + 'px';
				}
			}

			var getNodeStatus = function(heather) {
				var line = heather.editor.getCursor().line;
				var nodes = heather.getNodesByLine(line);
				if(nodes.length == 0) return null;
				var node = nodes[0];
				return {
					node: node,
					endLine: node.endLine,
					startLine: node.startLine,
				};
			}

			return Widget;
		})();

		return PartPreview;
	})();	
	
	//markdown render that with line numbers
	var MarkdownParser = (function(){
		
		function MarkdownParser(config){
			config = config||{};
			if(!config.highlight){
				config.highlight = function(str, lang) {
					if (lang == 'mermaid') {
                       return '<div class="mermaid">'+str+'</div>';
                    }
                    if (lang && hljs.getLanguage(lang)) {
                        try {
                            return '<pre class="hljs"><code class="language-' + lang + '">' +
                                hljs.highlight(lang, str, true).value +
                                '</code></pre>';
                        } catch (__) {}
                    }
                }
			}
			var md = window.markdownit(config);
			md.use(window.markdownitTaskLists);
			md.use(window.markdownitKatex);
			addLineNumberAttribute(md);
			this.md = md;
		}
		
		MarkdownParser.prototype.render = function(markdown){
			return this.md.render(markdown);
		}
		
		function addLineNumberAttribute(md){
			var injectLineNumbers = function(tokens, idx, options, env, slf) {
				var line;
				if (tokens[idx].map) {
					line = tokens[idx].map[0];
					tokens[idx].attrJoin('class', 'line');
					tokens[idx].attrSet('data-line', String(line));
					tokens[idx].attrSet('data-end-line', String(tokens[idx].map[1]));
				}
				return slf.renderToken(tokens, idx, options, env, slf);
			}
			md.renderer.rules.paragraph_open = injectLineNumbers;
			md.renderer.rules.heading_open = injectLineNumbers;
			md.renderer.rules.blockquote_open = injectLineNumbers;
			md.renderer.rules.hr = injectLineNumbers;
			md.renderer.rules.ordered_list_open = injectLineNumbers;
			md.renderer.rules.bullet_list_open = injectLineNumbers;
			md.renderer.rules.table_open = injectLineNumbers;
			md.renderer.rules.list_item_open = injectLineNumbers;
			md.renderer.rules.link_open = injectLineNumbers;
			addFenceLineNumber(md);
			addHtmlBlockLineNumber(md);
			addCodeBlockLineNumber(md);
			addMathBlockLineNumber(md);
		}
		
		function addMathBlockLineNumber(md){
			md.renderer.rules.math_block = function(tokens, idx, options, env, self) {
				var token = tokens[idx];
				var latex = token.content;
				var addLine = token.map;
				options.displayMode = true;
				if (addLine) {
					return "<div class='katex-block line' data-line='" + token.map[0] + "' data-end-line='" + token.map[1] + "'>"+latex+"</div>";
				} else {
					return renderKatexBlock(latex,options);
				}
			}
		}

		function addCodeBlockLineNumber(md){
			md.renderer.rules.code_block = function(tokens, idx, options, env, self) {
				var token = tokens[idx];
				if (token.map) {
					var line = token.map[0];
					var endLine = token.map[1];
					return '<pre' + self.renderAttrs(token) + ' class="line" data-line="' + line + '" data-end-line="' + endLine + '"><code>' +
						md.utils.escapeHtml(tokens[idx].content) +
						'</code></pre>\n';
				}
				return '<pre' + self.renderAttrs(token) + '><code>' +
					md.utils.escapeHtml(tokens[idx].content) +
					'</code></pre>\n';
			};
		}
		
		function addHtmlBlockLineNumber(md){
			md.renderer.rules.html_block = function (tokens, idx /*, options, env */) {
				var token = tokens[idx];
				var addLine = token.map;
				var content = token.content;
				if(addLine){
					var line = token.map[0];
					var div = document.createElement('div');
					div.classList.add("line");
					div.setAttribute("data-html", '');
					div.setAttribute("data-line", line);
					div.setAttribute("data-end-line", token.map[1]);
					div.innerHTML = content;
					return div.outerHTML;
				} else {
					return content;
				}
			};
		}
		
		
		function addFenceLineNumber(md){
			md.renderer.rules.fence = function(tokens, idx, options, env, slf) {
				var token = tokens[idx],
					info = token.info ? md.utils.unescapeAll(token.info).trim() : '',
					langName = '',
					highlighted, i, tmpAttrs, tmpToken;

				if (info) {
					langName = info.split(/\s+/g)[0];
				}

				if (options.highlight) {
					highlighted = options.highlight(token.content, langName) || md.utils.escapeHtml(token.content);
				} else {
					highlighted = md.utils.escapeHtml(token.content);
				}
				
				var addLine = token.map;
				if(langName == 'mermaid'){
					if(addLine){
						var div = document.createElement('div');
						div.innerHTML = highlighted;
						var ele = div.firstChild; 
						ele.classList.add("line");
						ele.setAttribute("data-line", token.map[0]);
						ele.setAttribute("data-end-line", token.map[1]);
						return div.innerHTML;
					}else{
						return highlighted;
					}
					
				}

				if (highlighted.indexOf('<pre') === 0) {
					if(addLine){
						var div = document.createElement('div');
						div.innerHTML = highlighted;
						var ele = div.firstChild; 
						ele.classList.add("line");
						ele.setAttribute("data-line", token.map[0]);
						ele.setAttribute("data-end-line", token.map[1]);
						return div.innerHTML;
					}
					return highlighted + '\n';
				}


				// If language exists, inject class gently, without modifying original token.
				// May be, one day we will add .clone() for token and simplify this part, but
				// now we prefer to keep things local.
				if (info) {
					i = token.attrIndex('class');
					tmpAttrs = token.attrs ? token.attrs.slice() : [];

					if (i < 0) {
						tmpAttrs.push(['class', options.langPrefix + langName]);
					} else {
						tmpAttrs[i][1] += ' ' + options.langPrefix + langName;
					}

					// Fake token just to render attributes
					tmpToken = {
						attrs: tmpAttrs
					};
					if (addLine) {
						return '<pre class="line" data-line="' + token.map[0] + '"  data-end-line="' + token.map[1] + '"><code' + slf.renderAttrs(tmpToken) + '>' +
							highlighted +
							'</code></pre>\n';
					} else {
						return '<pre><code' + slf.renderAttrs(tmpToken) + '>' +
							highlighted +
							'</code></pre>\n';
					}
				}

				if (addLine) {
					return '<pre class="line" data-line="' + token.map[0] + '" data-end-line="' + token.map[1] + '"><code' + slf.renderAttrs(token) + '>' +
						highlighted +
						'</code></pre>\n';
				} else {
					return '<pre><code' + slf.renderAttrs(token) + '>' +
						highlighted +
						'</code></pre>\n';
				}

			};	
		}
		
		return MarkdownParser;
		
	})();
	
	var Tooltip = (function() {

        var HljsTip = (function() {

            function HljsTip(editor) {
				var tip = document.createElement('div');
				tip.classList.add('heather_hljs_tip');
				tip.setAttribute('style','visibility:hidden;position:absolute;z-index:99;overflow:auto;background-color:#fff');
				document.body.appendChild(tip);
                var state = {
                    running: false,
                    cursor: undefined,
                    hideOnNextChange: false
                };
				
				tip.addEventListener('click',function(e){
					if(e.target.tagName === 'TD'){
						setLanguage(e.target);
					}
				})

                var setLanguage = function(selected) {
                    var lang = selected.textContent;
                    var cursor = editor.getCursor();
                    var text = editor.getLine(cursor.line);
                    editor.setSelection({
                        line: cursor.line,
                        ch: 4
                    }, {
                        line: cursor.line,
                        ch: text.length
                    });
                    editor.replaceSelection(lang);
					var line = cursor.line+1;
					if(line == editor.lineCount()){
						editor.replaceRange('\n', {
							line: cursor.line,
							ch: 4+lang.length
						});
					}
					editor.focus();
					editor.setCursor({line:line,ch:0})
                    state.hideOnNextChange = true;
                    hideTip();
                }

                var hideTip = function() {
					tip.style.visibility = 'hidden';
                    editor.removeKeyMap(languageInputKeyMap);
                    state.running = false;
                    state.cursor = undefined;
                }

                var languageInputKeyMap = {
                    'Up': function() {
                        var current = tip.querySelector('.selected');
                        var prev = current.previousElementSibling;
                        if (prev != null) {
                            current.classList.remove('selected');
                            prev.classList.add('selected');
                            prev.scrollIntoView();
                        }
                    },
                    'Down': function() {
                        var current = tip.querySelector('.selected');
                        var next = current.nextElementSibling;
                        if (next != null) {
                            current.classList.remove('selected');
                            next.classList.add('selected');
                            next.scrollIntoView();
                        }
                    },
                    'Enter': function(editor) {
                        setLanguage(tip.querySelector('.selected'));
                    },
                    'Esc': function(editor) {
                        hideTip();
                    }
                }
                var hljsTimer;
                var hljsLanguages = hljs.listLanguages();
                hljsLanguages.push('mermaid');
                this.hideTipOnCursorChange = function(editor) {
                    if (editor.getSelection() != '') {
                        hideTip();
                        return;
                    }
                    var cursor = editor.getCursor();
                    if (cursor.ch < 5) {
                        hideTip();
                        return;
                    }
                    if ((state.cursor || {
                            line: -1
                        }).line != cursor.line) {
                        hideTip();
                    }
                }
                this.hideTipOnScroll = function(cm,e) {
                    hideTip();
                }
                this.tipHandler = function(editor) {
                    hideTip();
                    if (editor.getSelection() == '') {
                        var cursor = editor.getCursor();
                        if (cursor.ch >= 5) {
                            if (hljsTimer) {
                                clearTimeout(hljsTimer);
                            }
                            hljsTimer = setTimeout(function() {
                                var text = editor.getLine(cursor.line);
                                if (text.startsWith("``` ")) {
                                    var lang = text.substring(4, cursor.ch).trimStart();
                                    var tips = [];
                                    for (var i = 0; i < hljsLanguages.length; i++) {
                                        var hljsLang = hljsLanguages[i];
                                        if (hljsLang.indexOf(lang) != -1) {
                                            tips.push(hljsLang);
                                        }
                                    }

                                    if (tips.length > 0) {
                                        if (state.hideOnNextChange) {
                                            state.hideOnNextChange = false;
                                            return;
                                        }
                                        state.running = true;
                                        state.cursor = cursor;
                                        var html = '<table style="width:100%">';
                                        for (var i = 0; i < tips.length; i++) {
                                            var clazz = i == 0 ? 'selected' : '';
                                            html += '<tr class="' + clazz + '"><td >' + tips[i] + '</td></tr>';
                                        }
                                        html += '</table>';
                                        var pos = editor.cursorCoords(true);
                                        tip.innerHTML = html;
                                        var height = tip.clientHeight;
										tip.style.top = pos.top + editor.defaultTextHeight()+'px';
										tip.style.left = pos.left+'px';
										tip.style.visibility = 'visible';
                                        editor.addKeyMap(languageInputKeyMap);
                                    } else {
                                        hideTip();
                                    }
                                } else {
                                    hideTip();
                                }
                            }, 100)
                        }
                    }
                }
                this.editor = editor;
            }

            HljsTip.prototype.enable = function() {
                this.editor.on('change', this.tipHandler);
                this.editor.on('cursorActivity', this.hideTipOnCursorChange);
                this.editor.on('scroll', this.hideTipOnScroll);
            }

            HljsTip.prototype.disable = function() {
                this.editor.off('change', this.tipHandler);
                this.editor.off('cursorActivity', this.hideTipOnCursorChange);
                this.editor.off('scroll', this.hideTipOnScroll);
            }

            return HljsTip;
        })();

        function Tooltip(editor,config) {
            this.hljsTip = new HljsTip(editor);
			if(config.tooltipEnable !== false)
				this.enable();
        }

        Tooltip.prototype.enable = function() {
            this.hljsTip.enable();
        }
        Tooltip.prototype.disable = function() {
            this.hljsTip.disable();
        }
        return Tooltip;

    })();
	
	var FileUpload = (function() {

        function FileUpload(file, heather,config) {
            this.uploadUrl = config.url;
            this.name = config.name || 'file';
            this.beforeUpload = config.beforeUpload;
            this.file = file;
            this.fileUploadFinish = config.uploadFinish;
            this.heather = heather;
			this.fileNameGen = config.fileNameGen;
        }

        FileUpload.prototype.start = function() {
            var me = this;
            var editor = this.heather.editor;
            var formData = new FormData();
			var fileName = this.file.name;
			if (this.fileNameGen)
				fileName = this.fileNameGen(this.file) || this.file.name;
			formData.append(this.name, this.file, fileName);
            var xhr = new XMLHttpRequest();
            var bar = document.createElement("div");
            bar.innerHTML = '<div class="heather_progressbar"><div></div><span style="position:absolute;top:0"></span><i class="fas fa-times middle-icon" style="position: absolute;top: 0;right: 0;"><i></div>'
            bar.querySelector('i').addEventListener('click', function() {
                xhr.abort();
            });
            var widget = editor.addLineWidget(editor.getCursor().line, bar, {
                coverGutter: false,
                noHScroll: true
            })
            xhr.upload.addEventListener("progress", function(e) {
                if (e.lengthComputable) {
                    var percentComplete = parseInt(e.loaded * 100 / e.total) + "";
                    var pb = bar.querySelector('.heather_progressbar').firstChild;
					pb.style.width = percentComplete + "%"
                    bar.querySelector('.heather_progressbar').querySelector('span').textContent = percentComplete + "%";
                }
            }, false);
            xhr.addEventListener('readystatechange', function(e) {
                if (this.readyState === 4) {
                    editor.removeLineWidget(widget);
                    if (xhr.status !== 0) {
                        var info = me.fileUploadFinish(xhr.response);
                        if (info) {
                            var type = (info.type || 'image').toLowerCase();
                            switch (type) {
                                case 'image':
                                    editor.focus();
                                    editor.replaceRange('![](' + info.url + ')', me.cursor);
                                    editor.setCursor({
                                        line: me.cursor.line,
                                        ch: me.cursor.ch + 2
                                    })
                                    break;
                                case 'file':
                                    editor.focus();
                                    editor.replaceRange('[](' + info.url + ')', me.cursor);
                                    editor.setCursor({
                                        line: me.cursor.line,
                                        ch: me.cursor.ch + 1
                                    })
                                    break;
                                case 'video':
                                    var video = document.createElement('video');
                                    video.setAttribute('controls', '');
                                    if (info.poster) {
                                        video.setAttribute('poster', info.poster);
                                    }
                                    var sources = info.sources || [];
                                    if (sources.length > 0) {
                                        for (var i = 0; i < sources.length; i++) {
                                            var source = sources[i];
                                            var ele = document.createElement('source');
                                            ele.setAttribute('src', source.src);
                                            ele.setAttribute('type', source.type);
                                            video.appendChild(ele);
                                        }
                                    } else {
                                        video.setAttribute('src', info.url);
                                    }
                                    editor.replaceRange(video.outerHTML, me.cursor);
                                    break;
                            }
                        }
                    }
                }
            });
			
            xhr.open("POST", this.uploadUrl);
            if (this.beforeUpload) {
                var result = this.beforeUpload({
					formData : formData,
					start : function(){
						xhr.send(formData);
					}
				}, this.file);
				
				if(result === true){
					xhr.send(formData);
				}
            } else {
				xhr.send(formData);
			}
            var cursor = editor.getCursor(true);
            this.cursor = cursor;
        }

        return FileUpload;
    })();
	
	var LazyLoader = (function() {
		
        var katexLoading = false;
        var katexLoaded = false;

        function loadKatex(callback) {
            if (katexLoaded) {
                if (callback) callback();
                return;
            }
            if (katexLoading) return;
            katexLoading = true;

            var link = document.createElement('link');
            link.setAttribute('type', 'text/css');
            link.setAttribute('rel', 'stylesheet');
            link.setAttribute('href', lazyRes.katex_css);
            document.head.appendChild(link);

            loadScript(lazyRes.katex_js, function() {
                katexLoaded = true;
                if (callback) callback();
            })
        }

        var mermaidLoading = false;
        var mermaidLoaded = false;

        function loadMermaid(callback) {
            if (mermaidLoaded) {
                if (callback) callback();
                return;
            }
            if (mermaidLoading) return;
            mermaidLoading = true;
            loadScript(lazyRes.mermaid_js, function() {
                mermaidLoaded = true;
                if (callback) callback();
            })
        }

        function loadScript(src, callback, relative) {
            var script = document.createElement('script');
            script.src = src;
            if (callback !== null) {
                script.onload = function() { // Other browsers
                    callback();
                };
            }
            document.body.appendChild(script);
        }

        return {
            loadKatex: loadKatex,
            loadMermaid: loadMermaid
        }
    })();
	
	var Util = (function(){
		
		var platform = navigator.platform;
		var userAgent = navigator.userAgent;
		var edge = /Edge\/(\d+)/.exec(userAgent);
		var ios = !edge && /AppleWebKit/.test(userAgent) && /Mobile\/\w+/.test(userAgent)
		var android = /Android/.test(userAgent);
		var mac = ios || /Mac/.test(platform);
		var chrome = !edge && /Chrome\//.test(userAgent)
		var mobile = ios || android || /webOS|BlackBerry|Opera Mini|Opera Mobi|IEMobile/i.test(userAgent);
	
		var isUndefined = function(o) {
			return (typeof o == 'undefined')
		}
		return {
			parseHTML : function(html){
				return new DOMParser().parseFromString(html, "text/html");	
			},
			isUndefined : isUndefined,
			getDefault: function(o, dft) {
				return isUndefined ? dft : o;
			},
			mobile : mobile,
			chrome : chrome,
			mac : mac,
			android : android,
			ios : ios,
			edge : edge
		}
		
	})();
	
	function removeCommandWidget(heather){
		if(heather.commandWidget){
			heather.commandWidget.remove();
			heather.commandBar.setKeepHidden(false);
			heather.partPreview.setKeepHidden(false);
			heather.commandWidget = undefined;
		}
	}
	
	function addCommandWidget(heather){
		
		removeCommandWidget(heather);
		
		var cm = heather.editor;
		var cursor = cm.getCursor();
		var div = document.createElement('div');
		div.style['z-index'] = "99";
		div.setAttribute('data-widget','');
		cm.addWidget(cursor,div);
		
		div.style['background-color'] = window.getComputedStyle( cm.getWrapperElement() ,null).getPropertyValue('background-color');
		
		function remove(){
			removeCommandWidget(heather);
			cm.off('cursorActivity',remove);
		}
		
		cm.on('cursorActivity',remove);
		heather.commandBar.setKeepHidden(true);
		heather.partPreview.setKeepHidden(true);
		heather.commandWidget = div;
		return div;
	}
	
	function posCommandWidget(cm,div){
		var pos = cm.cursorCoords(true,'local');
		
		var top = pos.top-cm.getScrollInfo().top;
		var left = parseFloat(div.style.left);
		var code = cm.getWrapperElement().querySelector('.CodeMirror-code');
		if(left + div.clientWidth + 5 > code.clientWidth){
			div.style.left = '';
			div.style.right = '5px';
		}
		
		var distance = cm.defaultTextHeight();
		if(top > distance+div.clientHeight){
			div.style.top = (pos.top -  div.clientHeight)+'px';
		} else {
			div.style.top = (pos.top + distance)+'px';
		}
	}
	
	commands['heading'] = function(heather){
		var div = addCommandWidget(heather);
		
		var innerHTML = '<select class="heather_command_heading_select">';
		for(var i=1;i<=6;i++){
			innerHTML += '<option value="'+i+'">H'+i+'</option>';
		}
		innerHTML += '</select>';
		if(Util.mobile){
			innerHTML += '<button class="heather_command_heading_button">取消</button>';
			innerHTML += '<button class="heather_command_heading_button">确定</button>';
		}
		div.innerHTML = innerHTML;	
			
		function doHeading(){
			var value = div.querySelector('select').value;
			if(value != ''){
				var cm = heather.editor;
				var v = parseInt(value);
				var status = cm.selectionStatus();
				selectionBreak(status, function(text) {
					var prefix = '';
					for (var i = 0; i < v; i++) {
						prefix += '#';
					}
					prefix += ' ';
					return prefix + text;
				});
				if (status.selected == '') {
					cm.replaceRange(status.text, cm.getCursor());
					cm.focus();
					cm.setCursor({
						line: status.startLine,
						ch: v + 1
					});
				} else {
					cm.replaceSelection(status.text);
				}
			}
		}
		var select = div.querySelector('select');
		select.addEventListener('keydown',function(e){
			if(e.key == 'Enter'){
				e.preventDefault();
				e.stopPropagation();
				doHeading();
			}
			if(e.key == 'Escape'){
				e.preventDefault();
				e.stopPropagation();
				div.remove();
				heather.editor.focus();
			}
		});
		
		if(Util.mobile){
			var buttons = div.querySelectorAll('button');
			buttons[0].addEventListener('click',function(){
				div.remove();
				heather.editor.focus();
			});
			buttons[1].addEventListener('click',function(){
				doHeading();
			});
		}
		
		
		select.size = 6;
		select.focus();
		
		posCommandWidget(heather.editor,div);
	}
	
	commands['commands'] = function(heather){
		var div = addCommandWidget(heather);
		
		var innerHTML = '<select class="heather_command_commands_select">';
		innerHTML += '<option value="heading">标题</option>';
		innerHTML += '<option value="table">表格</option>';
		innerHTML += '<option value="codeBlock">代码块</option>';
		innerHTML += '<option value="check">任务列表</option>';
		innerHTML += '<option value="quote">引用</option>';
		innerHTML += '<option value="mathBlock">数学公式块</option>';
		innerHTML += '<option value="mermaid">mermaid图表</option>';
		innerHTML += '</select>';
		if(Util.mobile){
			innerHTML += '<button class="heather_command_commands_button">取消</button>';
			innerHTML += '<button class="heather_command_commands_button">确定</button>';
		}
		div.innerHTML = innerHTML;
		function execCommand(){
			div.remove();
			var value = div.querySelector('select').value;
			heather.execCommand(value);
		}
		div.querySelector('select').addEventListener('keydown',function(e){
			if(e.key == 'Enter'){
				e.preventDefault();
				e.stopPropagation();
				execCommand();
			}
			if(e.key == 'Escape'){
				e.preventDefault();
				e.stopPropagation();
				div.remove();
				heather.editor.focus();
			}
		});
		if(Util.mobile){
			var buttons = div.querySelectorAll('button');
			buttons[0].addEventListener('click',function(){
				div.remove();
				heather.editor.focus();
			});
			buttons[1].addEventListener('click',function(){
				execCommand();
			});
		}
		var select = div.querySelector('select');
		select.size = 7;
		select.focus();
		posCommandWidget(heather.editor,div);
	}
	
	commands['table'] = function(heather){
		var div = addCommandWidget(heather);
		var innerHTML = '行：<input type="number" value="" style="width:80px">&nbsp;';
		innerHTML += '列：<input type="number" value="" style="width:80px">&nbsp;';
		innerHTML += '<button class="heather_command_table_button">确定</button>';
		div.innerHTML = innerHTML;
		
		var inputs = div.querySelectorAll('input');
		for(const input of inputs){
			input.addEventListener('keydown',function(e){
				if(e.key == 'Escape'){
					e.preventDefault();
					e.stopPropagation();
					div.remove();
					heather.editor.focus();
				}
			});	
		}
		div.querySelector('button').addEventListener('click',function(){
			var cm = heather.editor;
			var inputs = div.querySelectorAll('input');
			var rows;
			try{
				rows = parseInt(inputs[0].value);
			}catch(e){rows = 3}
			if(!rows || isNaN(rows)){
				rows = 3;
			}
			var cols;
			try{
				cols = parseInt(inputs[1].value);
			}catch(e){cols = 3}
			if(!cols || isNaN(cols)){
				cols = 3;
			}
			if (rows < 1)
				rows = 3;
			if (cols < 1)
				cols = 3;
			var text = '';
			for (var i = 0; i < cols; i++) {
				text += '|    ';
			}
			text += '|'
			text += "\n";
			for (var i = 0; i < cols; i++) {
				text += '|  --  ';
			}
			text += '|'
			if (rows > 1) {
				text += '\n';
				for (var i = 0; i < rows - 1; i++) {
					for (var j = 0; j < cols; j++) {
						text += '|    ';
					}
					text += '|'
					if (i < rows - 2)
						text += "\n";
				}
			}
			var cursor = cm.getCursor();
			var status = cm.selectionStatus();
			selectionBreak(status, function(selected) {
				return text;
			});
			cm.replaceSelection(status.text);
			var lineStr = cm.getLine(status.startLine);
			var startCh,endCh;
			
			for(var i=0;i<lineStr.length;i++){
				if(lineStr.charAt(i) == '|'){
					if(!Util.isUndefined(startCh)){
						endCh = i;
						break;
					}
					startCh = i;
				}
			}
			
			if(!Util.isUndefined(startCh) && !Util.isUndefined(endCh)){
				var ch = startCh + (endCh - startCh)/2;
				cm.focus();
				cm.setCursor({line : status.startLine,ch:ch});
			}	
		})
		div.querySelector('input').focus();
		posCommandWidget(heather.editor,div);
	}
	
	commands['uncheck'] = function(heather){
		var cm = heather.editor;
		insertTaskList(cm, false);
	}
	
	commands['check'] = function(heather){
		var cm = heather.editor;
		insertTaskList(cm, true);
	}
	
	commands['code'] = function(heather){
		var cm = heather.editor;
		var text = cm.getSelection();
		if (text == '') {
			cm.replaceRange("``", cm.getCursor());
			cm.focus();
			var start_cursor = cm.getCursor();
			var cursorLine = start_cursor.line;
			var cursorCh = start_cursor.ch;
			cm.setCursor({
				line: cursorLine,
				ch: cursorCh - 1
			});
		} else {
			cm.replaceSelection("`" + text + "`");
		}
	}
	
	commands['codeBlock'] = function(heather){
		var cm = heather.editor;
		var status = cm.selectionStatus();
		selectionBreak(status, function(text) {
			var newText = "``` ";
			newText += '\n';
			newText += text;
			newText += '\n'
			newText += "```";
			return newText;
		});
		cm.focus();
		cm.replaceSelection(status.text);
		cm.setCursor({
			line: status.startLine + 1,
			ch: 0
		});	
	}
	
	commands['link'] = function(heather){
		var cm = heather.editor;
		var text = cm.getSelection();
		if (text == '') {
			cm.replaceRange("[](https://)", cm.getCursor());
			cm.focus();
			var start_cursor = cm.getCursor();
			var cursorLine = start_cursor.line;
			var cursorCh = start_cursor.ch;
			cm.setCursor({
				line: cursorLine,
				ch: cursorCh - 11
			});
		} else {
			cm.replaceSelection("[" + text + "](https://)");
		}
	}
	
	commands['strikethrough'] = function(heather){
		var cm = heather.editor;
		var text = cm.getSelection();
		if (text == '') {
			cm.replaceRange("~~~~", cm.getCursor());
			cm.focus();
			var str = "~~";
			var mynum = str.length;
			var start_cursor = cm.getCursor();
			var cursorLine = start_cursor.line;
			var cursorCh = start_cursor.ch;
			cm.setCursor({
				line: cursorLine,
				ch: cursorCh - mynum
			});
		} else {
			cm.replaceSelection("~~" + text + "~~");
		}
	}
	
	commands['mermaid'] = function(heather){
		var cm = heather.editor;
		var status = cm.selectionStatus();
		selectionBreak(status, function(text) {
			var newText = "``` mermaid";
			newText += '\n';
			newText += text;
			newText += '\n'
			newText += "```";
			return newText;
		});
		cm.focus();
		cm.replaceSelection(status.text);
		cm.setCursor({
			line: status.startLine + 1,
			ch: 0
		});
	}
	
	commands['mathBlock'] = function(heather){
		var cm = heather.editor;
		var status = cm.selectionStatus();
		selectionBreak(status, function(text) {
			return '$$\n' + text+"\n$$";
		});
		if (status.selected == '') {
			cm.replaceRange(status.text, cm.getCursor());
			cm.focus();
			cm.setCursor({
				line: status.startLine+1,
				ch: 0
			});
		} else {
			cm.replaceSelection(status.text);
		}
	}
	
	commands['quote'] = function(heather){
		var cm = heather.editor;
		var status = cm.selectionStatus();
		selectionBreak(status, function(text) {
			var lines = [];
			for(const line of text.split('\n')){
				lines.push("> "+line);
			}
			return lines.join('\n');
		});
		if (status.selected == '') {
			cm.replaceRange(status.text, cm.getCursor());
			cm.focus();
			cm.setCursor({
				line: status.startLine,
				ch: 2
			});
		} else {
			cm.replaceSelection(status.text);
		}
	}
	
	commands['bold'] = function(heather){
		var cm = heather.editor;
		var text = cm.getSelection();
		if (text == '') {
			cm.replaceRange("****", cm.getCursor());
			cm.focus();
			var str = "**";
			var mynum = str.length;
			var start_cursor = cm.getCursor();
			var cursorLine = start_cursor.line;
			var cursorCh = start_cursor.ch;
			cm.setCursor({
				line: cursorLine,
				ch: cursorCh - mynum
			});
		} else {
			cm.replaceSelection("**" + text + "**");
		}
	}
	
	commands['italic'] = function(heather){
		var cm = heather.editor;
		var text = cm.getSelection();
		if (text == '') {
			cm.replaceRange("**", cm.getCursor());
			cm.focus();
			var str = "*";
			var mynum = str.length;
			var start_cursor = cm.getCursor();
			var cursorLine = start_cursor.line;
			var cursorCh = start_cursor.ch;
			cm.setCursor({
				line: cursorLine,
				ch: cursorCh - mynum
			});
		} else {
			cm.replaceSelection("*" + text + "*");
		}
	}
	
	CodeMirror.prototype.selectionStatus = function() {
        var status = {
            selected: '',
            startLine: -1,
            startCh: -1,
            endLine: -1,
            endCh: -1,
            prev: '',
            next: '',
            prevLine: '',
            nextLine: ''
        }
        var startCursor = this.getCursor(true);
        var endCursor = this.getCursor(false);

        status.startLine = startCursor.line;
        status.endLine = endCursor.line;
        status.startCh = startCursor.ch;
        status.endCh = endCursor.ch;
        status.prevLine = startCursor.line == 0 ? '' : this.getLine(startCursor.line - 1);
        status.nextLine = endCursor.line == this.lineCount() - 1 ? '' : this.getLine(endCursor.line + 1);

        var startLine = this.getLine(status.startLine);
        var text = this.getSelection();
        if (text == '') {
            if (startCursor.ch == 0) {
                status.next = startLine;
            } else {
                status.prev = startLine.substring(0, startCursor.ch);
                status.next = startLine.substring(startCursor.ch, startLine.length);
            }
        } else {

            var endLine = this.getLine(status.endLine);
            if (status.startCh == 0) {
                status.prev = '';
            } else {
                status.prev = startLine.substring(0, status.startCh);
            }
            if (status.endCh == endLine.length) {
                status.next = '';
            } else {
                status.next = endLine.substring(status.endCh, endLine.length);
            }
        }
        status.selected = text;
        return status;
    }
	
	
	function insertTaskList(editor, checked) {
        var status = editor.selectionStatus();
        var text = '';
        if (status.prev != '') {
            if (status.next == '') {
                if (!status.prev.startsWith("- [ ] ") && !status.prev.startsWith("- [x] ")) {
                    text += '\n\n';
                } else {
                    text += '\n';
                }
            } else {
                text += '\n\n';
            }
        } else {
            if (status.prevLine != '' && !status.prevLine.startsWith("- [ ] ") && !status.prevLine.startsWith("- [x] ")) {
                text += '\n';
            }
        }
        var prefix = checked ? '- [x]  ' : '- [ ]  ';
		
		var lines = [];
		for(const line of status.selected.split('\n')){
			lines.push(prefix + line);
		}
		text += lines.join('\n');
		
        if (status.next != '') {
            if (status.prev == '') {
                if (!status.next.startsWith("- [ ] ") && !status.next.startsWith("- [x] ")) {
                    text += '\n\n';
                } else {
                    text += '\n';
                }
            } else {
                text += '\n\n';
            }
        } else {
            if (status.nextLine != '' && !status.nextLine.startsWith("- [ ] ") && !status.nextLine.startsWith("- [x] ")) {
                text += '\n';
            }
        }
		var line = editor.getCursor(true).line;
		var ch = 0;
		for(const lineStr of text.split('\n')){
			if(lineStr == ''){
				line ++ ;
			} else {
				ch = lineStr.length;
				break;
			}
		}
        editor.replaceSelection(text);
		editor.focus();
		editor.setCursor({line:line,ch:ch});
    }
	
	function selectionBreak(status, callback) {
        var _text = '';
        if (status.prev != '') {
            _text += '\n\n';
            status.startLine += 2;
        } else {
            if (status.prevLine != '') {
                _text += '\n';
                status.startLine += 1;
            }
        }
        if (callback) {
            _text += callback(status.selected);
        }
        if (status.next != '') {
            _text += '\n\n';
            status.endLine += 2;
        } else {
            if (status.nextLine != '') {
                _text += '\n';
                status.endLine += 1;
            }
        }
        status.text = _text;
        return status;
    }
	
	function renderKatexAndMermaid(element,config) {
		LazyLoader.loadKatex(function() {
			var inlines = element.querySelectorAll(".katex-inline");
			for (var i = 0; i < inlines.length; i++) {
				var inline = inlines[i];
				var expression = inline.textContent;
				var result = parseKatex(expression, false);
				var div = document.createElement('div');
				div.innerHTML = result;
				var child = div.firstChild;
				child.setAttribute('data-inline-katex', '');
				inline.outerHTML = child.outerHTML;
			}
			var blocks = [];

			if(element.hasAttribute('data-block-katex')){
				blocks.push(element);
			} else {
				for(const elem of element.querySelectorAll(".katex-block")){
					blocks.push(elem);
				}
			}
			
			for (var i = 0; i < blocks.length; i++) {
				var block = blocks[i];
				var expression = block.textContent;
				var result = parseKatex(expression, true);
				var div = document.createElement('div');
				div.innerHTML = result;
				var child = div.firstChild;
				child.setAttribute('data-block-katex', '');
				block.innerHTML = child.outerHTML;
			}
		})
		LazyLoader.loadMermaid(function() {
			var mermaidElems = element.querySelectorAll('.mermaid');
			for(const mermaidElem of mermaidElems){
				if (!mermaidElem.hasAttribute("data-processed")) {
					try {
						mermaid.parse(mermaidElem.textContent);
						mermaid.init({}, mermaidElem);
					} catch (e) {
						mermaidElem.innerHTML = '<pre>' + e.str + '</pre>'
					}
				}
			}
		});
    }
	
	function parseKatex(expression, displayMode) {
        try {
            return katex.renderToString(expression, {
                throwOnError: true,
                displayMode: displayMode
            });
        } catch (e) {
            if (e instanceof katex.ParseError) {
                var attr = displayMode ? 'data-block-katex' : 'data-inline-katex'
                return '<span class="katex-mathml" data-error title="' + e.message + '" ' + attr + ' style="color:red"><math><semantics><annotation encoding="application/x-tex">' + expression + '</annotation></semantics></math>' + expression + '</span>';
            } else {
                throw e;
            }
        }
    }
	
	function changeTastListStatus(heather,x,y){
		if(heather.rendered !== true) return ;
		var cm = heather.editor;
		var cursor = cm.coordsChar({left:x, top:y}, 'window');
		var nodes = heather.getNodesByLine(cursor.line);
		if(nodes.length == 0) return ;
		var node = nodes[nodes.length - 1];
		if(node.classList.contains('task-list-item')){
			var lineStr = cm.getLine(cursor.line);
			if(lineStr.substring(0,cursor.ch).replaceAll(' ','') == '-['){
				//need change 
				var checkbox = node.firstChild;
				if(checkbox != null && checkbox.type == 'checkbox'){
					var startCh,endCh;
						
					for(var i=0;i<lineStr.length;i++){
						var ch = lineStr.charAt(i);
						if(ch == '['){
							startCh = i;
						}
						if(ch == ']'){
							endCh = i;
							break;
						}
					}
					if(!Util.isUndefined(startCh) && !Util.isUndefined(endCh)){
						cm.setSelection({line:cursor.line,ch:startCh+1},{line:cursor.line,ch:endCh});
						if(checkbox.checked){
							cm.replaceSelection(" ");
						} else {
							cm.replaceSelection("x");
						}
					}
					
				}
			}
		}
	}
	
	function tab(heather){
		var editor = heather.editor;
		if(!editor.somethingSelected() && heather.rendered === true){
			var cursor = editor.getCursor();
			var line = cursor.line;
			var nodes = heather.getNodesByLine(line);
			var mappingElem;
			if(nodes.length > 0)
				mappingElem = nodes[0];
			if(mappingElem){
				if(mappingElem.tagName === 'TABLE'){
					var startLine = parseInt(mappingElem.dataset.line);
					var endLine = parseInt(mappingElem.dataset.endLine) - 1;
					var setNextCursor = function(i,substr){
						if(i == startLine+1) return false;// 
						substr = i == line && substr === true;
						var lineStr = substr ? editor.getLine(i).substring(cursor.ch) : editor.getLine(i);
						var firstCh,lastCh;
						for(var j=0;j<lineStr.length;j++){
							var ch = lineStr.charAt(j);
							if(ch == '|'){
								//find prev char '\';
								var prevChar = j == 0 ? '' : lineStr.charAt(j-1);
								if(prevChar != '\\'){
									//find first
									//need to find next
									if(Util.isUndefined(firstCh)){
										firstCh = j;
									}else {
										lastCh = j;
										break;
									}
								}
							}
						}
						if(!Util.isUndefined(firstCh) && !Util.isUndefined(lastCh)){
							//set cursor at middle
							var ch = parseInt(Math.ceil((lastCh - firstCh)/2))+ firstCh + (substr ? cursor.ch : 0);
							editor.setCursor({line : i,ch : ch});
							return true;
						} else {
							return false;
						}
					}
					
					var hasNext = false;
					for(var i=line;i<=endLine;i++){
						if(setNextCursor(i,true)){
							hasNext = true;
							break;
						}
					}
					
					if(!hasNext){
						return setNextCursor(startLine);
					}
					
					return true;
				}
			}	
		}
		return false;
	}
	
	return {
		create : function(textarea,config){
			return new Editor(textarea,config);
		},
		commands : commands,
		lazyRes : lazyRes,
		Util : Util
	};
})();