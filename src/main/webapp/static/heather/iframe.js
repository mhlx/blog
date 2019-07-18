var Editor = (function(rootPath) {
	var style = document.createElement('style');
	style.innerHTML = '.noscroll{overflow: hidden;}'
	document.head.appendChild(style);
	var currentDiv;

	var Editor = function(textarea,config,callback) {
		removeCurrentDiv();
		var div = document.createElement('div');
		div.style.cssText = "position:fixed;top:0px;left:0px;width:100%;height:100%;z-index:999999";
		div.innerHTML = '<iframe src="'+rootPath+'heather" width="100%" height="100%" border="0" frameborder="0" ></iframe>';
		var iframe = div.querySelector('iframe');
		iframe.addEventListener('load', function() {
			var innerDoc  = iframe.contentWindow.document;
			iframe.contentWindow.createEditor(config);
			document.body.classList.add('noscroll');
			var wrapper = iframe.contentWindow.wrapper;
			var pos = getPos(textarea);
			if (pos)
				wrapper.editor.setCursor(pos);
			if(callback){
				callback(wrapper);
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

	Editor.prototype.remove = function() {
		removeCurrentDiv();
	}
	return {
		create : function(textarea,config,callback) {
			return new Editor(textarea,config,callback)
		}
	}

})(rootPath);