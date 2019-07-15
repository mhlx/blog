var Editor = (function(rootPath) {
	var style = document.createElement('style');
	style.innerHTML = '.noscroll{overflow: hidden;}'
	document.head.appendChild(style);
	var currentDiv;

	var Editor = function(textarea,callback) {
		removeCurrentDiv();
		var div = document.createElement('div');
		div.style.cssText = "position:fixed;top:0px;left:0px;width:100%;height:100%;z-index:999999";
		div.innerHTML = '<iframe src="'
				+ rootPath
				+ 'heather" width="100%" height="100%" border="0" frameborder="0" ></iframe>';
		var iframe = div.querySelector('iframe');
		var editor = this;
		iframe.addEventListener('load', function() {
			var innerDoc = iframe.contentDocument
					|| iframe.contentWindow.document;
			editor.editor = iframe.contentWindow.editor;
			editor.toolbarHelper = new ToolbarHelper(innerDoc
					.getElementById("toolbar"));
			document.body.classList.add('noscroll');
			editor.editor.getDoc().setValue(textarea.value)
			var pos = getPos(textarea);
			if (pos)
				editor.editor.setCursor(pos);
			if(callback){
				callback(editor);
			}
		});
		document.body.append(div);
		currentDiv = div;
	}

	var removeCurrentDiv = function() {
		if (currentDiv) {
			currentDiv.parentNode.removeChild(currentDiv);
			currentDiv = undefined;
		}
	}

	var getPos = function(textarea) {
		var lines = textarea.value.substr(0, textarea.selectionStart).split(
				"\n");
		var lineNum = lines.length - 1;
		return {
			line : lineNum,
			ch : lines[lineNum].length
		};
	}

	var ToolbarHelper = function(toolbarElement) {
		this.toolbar = toolbarElement;
	}

	ToolbarHelper.prototype.getSize = function() {
		return this.toolbar.querySelectorAll("i").length;
	}

	ToolbarHelper.prototype.insertIcon = function(clazz, handler, index) {
		var newIcon = document.createElement('i');
		for(var i=0;i<clazz.length;i++)
			newIcon.classList.add(clazz[i]);
		newIcon.classList.add('icon');
		newIcon.addEventListener('click',handler);
		if (index >= this.getSize()) {
			this.toolbar.appendChild(newIcon);
		}else{
			if(index<=0){
				this.toolbar.insertBefore(newIcon, this.toolbar.childNodes[0])
			} else {
				this.toolbar.insertBefore(newIcon, this.toolbar.childNodes[index])
			}
		}
	}
	
	ToolbarHelper.prototype.addIcon = function(clazz, handler) {
		this.insertIcon(clazz,handler,this.getSize());
	}
	
	ToolbarHelper.prototype.removeIcon = function(deleteChecker){
		var icons = this.toolbar.querySelectorAll("i");
		for(var i=0;i<icons.length;i++){
			var icon = icons[i];
			if(deleteChecker(icon)){
				this.toolbar.removeChild(icon);
			}
		}
	}

	Editor.prototype.remove = function() {
		removeCurrentDiv();
	}
	return {
		create : function(textarea,callback) {
			return new Editor(textarea,callback)
		}
	}

})(rootPath);