	var createEditor = function(editorId,keys){
		var mixedMode = {
			name: "htmlmixed",
			scriptTypes: [{matches: /\/x-handlebars-template|\/x-mustache/i,
					   mode: null},
					  {matches: /(text|application)\/(x-)?vb(a|script)/i,
					   mode: "vbscript"}]
		};
		var extraKeys = {"Alt-/": "autocomplete","Alt-F": "findPersistent","Ctrl-A":"selectAll"};
		for(var i=0;i<keys.length;i++){
			var key = keys[i].key;
			if(!extraKeys[key])
				extraKeys[key] = keys[i].fun;
		}
		var editor = CodeMirror.fromTextArea(document.getElementById(editorId), {
			mode: mixedMode,
			lineNumbers: true,
			autoCloseTags: true,
			allowDropFileTypes:['text/html'],
			extraKeys: extraKeys,
			foldGutter: true,
		    gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
		
		var wrap = function(url){
			var ext = url.split('.').pop().toLowerCase();
			if(ext == 'css'){
				return '<link href="'+url+'" rel="stylesheet">';
			}
			if(isImage(ext)){
				return '<img src="'+url+'" />';
			}
			if(ext == 'js'){
				return '<script src="'+url+'"></script>';
			}
			return '<a href="'+url+'">'+url+'</a>';
		}
		
		var isImage = function(ext){
			return ext == 'png' || ext == 'jpg' || ext == 'jpeg' || ext == 'gif';
		}
		
		editor.format = function(){
			if(editor.somethingSelected()){
				var formatted = html_beautify(editor.getSelection());
				editor.replaceSelection(formatted);
			} else {
				editor.setValue(html_beautify(editor.getValue()));
			}
		}
		editor.insertUrl = function(url,smart){
			if(smart){
				editor.replaceSelection(wrap(url));
			}else{
				editor.replaceSelection(url);
			}
		}	
		editor.clear = function(){
			editor.setValue('');
		}
		
		
		return editor;
	};