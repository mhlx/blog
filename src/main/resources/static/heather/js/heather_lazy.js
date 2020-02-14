var LazyHeather = (function(){
	
	var resources = [];
	var index = 0;
	var callback ;
	var loaded = false;
	function createResource(type, location, url, code,cb) {
        var obj = document.createElement(type);

        if (type === 'script') {
            if (url) obj.src = url;
            if (code) obj.text = code;
        } else {
            obj.href = url;
            obj.rel = 'stylesheet';
        }
		if(cb){
			cb(obj);
		}
        obj.location = location;
        obj.onload = indexCount;
        return obj;
    }

    function sendScript(index) {
        return resources[index].location.appendChild(resources[index]);
    }

    function indexCount() {
    	if(loaded){
    		if(callback){
        		callback(Heather)
        		callback = undefined;
        	}
    		return ;
    	}
        if (index < resources.length) {
            sendScript(index);
            index++;
        } else {
        	loaded = true;
        	if(callback){
        		callback(Heather)
        		callback = undefined;
        	}
		}
    }
    
    resources.push(createResource('link', document.head, rootPath+'static/heather/codemirror/lib/codemirror.css', null));
    resources.push(createResource('link', document.head, rootPath+'static/heather/css/markdown.css', null));
    resources.push(createResource('link', document.head, rootPath+'static/heather/css/style.css', null));
    resources.push(createResource('link', document.head, rootPath+'static/heather/highlight/styles/github.css', null));
    resources.push(createResource('link', document.head, rootPath+'static/heather/codemirror/addon/dialog/dialog.css', null));
    resources.push(createResource('link', document.head, rootPath+'static/heather/codemirror/addon/display/fullscreen.css', null));
    resources.push(createResource('link', document.head, rootPath+'static/heather/codemirror/addon/fold/foldgutter.css', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/highlight/highlight.pack.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/lib/codemirror.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/mode/overlay.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/mode/markdown/markdown.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/mode/gfm/gfm.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/selection/mark-selection.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/selection/active-line.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/search/searchcursor.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/edit/continuelist.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/search/search.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/dialog/dialog.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/display/fullscreen.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/fold/foldcode.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/fold/foldgutter.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/codemirror/addon/search/jump-to-line.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/js/markdown-it.min.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/js/markdown-it-task-lists.min.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/js/markdown-it-katex.min.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/js/markdownItAnchor.umd.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/js/morphdom-umd.min.js', null));
    resources.push(createResource('script', document.body, rootPath +'static/heather/js/heather.js', null));
    
    return {
    	load:function(cb){
    		callback = cb;
    		indexCount();
    	}
    }
})();
