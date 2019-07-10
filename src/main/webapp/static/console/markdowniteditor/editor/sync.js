var sync = (function(editor) {
	'use strict';
	
	var scrollElement = $(render.getRenderElement());

    var syncResultScroll = function(ms) {
		var o = scrollElement;
        var editorScroll = getEditorScroll();
        let lastPosition = 0
        let nextPosition = o.outerHeight();
		var last;
        if (!isUndefined(editorScroll.lastMarker)) {
			last = getElementByLine(editorScroll.lastMarker);
			if(!isUndefined(last)){
				lastPosition = last.offsetTop - 10
			}
        }
		var next;
        if (!isUndefined(editorScroll.nextMarker)) {
			next = getElementByLine(editorScroll.nextMarker) || getElementByEndLine(editorScroll.nextMarker)
			if(!isUndefined(next)){
				nextPosition = next.offsetTop - 10
			}
        }
		var pos = nextPosition - lastPosition;
		if(!isUndefined(last) && !isUndefined(next) && last === next){
			pos = last.clientHeight;
		}
        const scrollTop = lastPosition + pos * editorScroll.percentage;
        o.stop(true).animate({
            scrollTop: scrollTop
        }, ms)
    }
	
	var getElementByLine = function(line){
		return document.getElementById("out").querySelector('[data-line="'+line+'"]');
	}
	
	var getElementByEndLine = function(line){
		return document.getElementById("out").querySelector('[data-end-line="'+line+'"]');
	}

	var getLineMarker = function(){
		return document.getElementById("out").querySelectorAll('[data-line]');
	}
	
	var oldLastLine;
    var getEditorScroll = function() {
       
		var lines = [];
		var lineMarkers = getLineMarker();
		lineMarkers.forEach(function(ele){
			lines.push(parseInt(ele.getAttribute('data-line')));
		});
        var currentPosition = editor.getScrollInfo().top
        let lastMarker
        let nextMarker
        for (let i = 0; i < lines.length; i++) {
            const line = lines[i];
            const height = editor.heightAtLine(line, 'local')
            if (height < currentPosition) {
                lastMarker = line
            } else {
                nextMarker = line
                break
            }
        }
		if(!isUndefined(lastMarker) && isUndefined(nextMarker)){
			nextMarker = parseInt(lineMarkers[lineMarkers.length-1].getAttribute('data-end-line'));
		}
        let percentage = 0
        if (!isUndefined(lastMarker) && !isUndefined(nextMarker) && lastMarker !== nextMarker) {
            percentage = (currentPosition - editor.heightAtLine(lastMarker, 'local')) / (editor.heightAtLine(nextMarker, 'local') - editor.heightAtLine(lastMarker, 'local'))
        }
        return {
            lastMarker: lastMarker,
            nextMarker: nextMarker,
            percentage
        }
    }
	
	var isUndefined = function(o){
		return (typeof  o == 'undefined')
	}
	
	
	var refreshDoc = function(){
		editor.setOption('readOnly',true);
		var viewport = editor.getViewport();
		var lastLine = editor.lineCount()-1;
		while(viewport.to < lastLine && viewport.to > 0){
			editor.scrollIntoView({line:viewport.to});
			viewport = editor.getViewport();
		}
		
		editor.scrollIntoView({line:lastLine});
		editor.scrollIntoView({top:0});
		editor.setOption('readOnly',false);
	}
	
	if (!CodeMirror.browser.mobile) {
		editor.on('scroll', function(){
			 if (!config.syncScroll) {
				return;
			}
			syncResultScroll(0);
		});
		editor.on('update', function handler(){
			 editor.off('update', handler);
			 refreshDoc();
		});
	}
	
	return {
		doSync:function(ms){
			syncResultScroll(ms);
		},
		setScrollElement:function(ele){
			scrollElement = $(ele);
		}
	}
	
})(editor);