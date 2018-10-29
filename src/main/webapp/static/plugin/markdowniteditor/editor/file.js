var files = (function(editor) {
    var videoModal = '<div class="modal" tabindex="-1" role="dialog">';
    videoModal += '<div class="modal-dialog" role="document">';
    videoModal += '<div class="modal-content">';
    videoModal += '<div class="modal-header">';
    videoModal += '<h4 class="modal-title">插入视频</h4>';
    videoModal += '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>';
    videoModal += '</div>';
    videoModal += '<div class="modal-body">';
    videoModal += '<input type="text" class="form-control" placeholder="" onclick="var me = this;setTimeout(function(){me.setSelectionRange(0, 9999);},1);">';
    videoModal += '<textarea class="form-control" style="margin-top:10px" rows="8"></textarea>';
    videoModal += '</div>';
    videoModal += '<div class="modal-footer">';
    videoModal += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
    videoModal += '<button type="button" class="btn btn-primary">插入</button>';
    videoModal += ' </div>';
    videoModal += ' </div>';
    videoModal += ' </div>';
    videoModal += ' </div>';
    $video = $(videoModal);
    $video.appendTo($('body'));
    var showVideoModal = function(url) {
        $video.find('input[type="text"]').val(url);
        $video.find('textarea').val('<video src="' + url + '" poster="" style="max-width:100%" controls>您的浏览器不支持播放视频</video>');
        var index = 22 + url.length;
        $video.find('textarea').unbind('click').bind('click', function() {
            $(this)[0].setSelectionRange(index, index);
            $(this).unbind('click');
        });
        $video.modal({
            'backdrop': 'static'
        });
    }
    
    var updateCodeMirror = function(data){
	    var doc = editor.getDoc();
	    var cursor = doc.getCursor(); // gets the line number in the cursor position
	    var line = doc.getLine(cursor.line); // get the line contents
	    var pos = { // create a new object to avoid mutation of the original selection
	        line: cursor.line,
	        ch: line.length - 1 // set the character position to the end of the line
	    }
	    doc.replaceRange('\n'+data+'\n', pos); // adds a new line
	}
    var handleFiles = function(datas){
    	for(var i=0;i<datas.length;i++){
    		var data = datas[i];
    		 var cf = data.cf;
             var ext = cf.extension.toLowerCase();
             if ($.inArray(ext, ['jpeg', 'jpg', 'png', 'gif']) == -1) {
                 if (ext == 'mp4' || ext == 'mov') {
                     showVideoModal(cf.url);
                     $video.find('.modal-footer button').eq(1).unbind('click').bind('click',function() {
                         var content = $video.find('textarea').val();
                         updateCodeMirror(content);
                 		 $video.modal('hide');
                 		 var _datas = [];
                 		 for(var j=i+1;j<datas.length;j++){
                 			 _datas.push(datas[j]);
                 		 }
                 		 handleFiles(_datas);
                     });
                     return ;
                 } else {
                	 updateCodeMirror('[' + cf.originalFilename + '](' + cf.url + ')');
                 }
             } else {
                 var thumb = cf.thumbnailUrl;
                 if (thumb) {
                	 updateCodeMirror('[![' + cf.originalFilename + '](' + thumb.middle + ')](' + thumb.large + ')');
                 } else {
                	 updateCodeMirror('![' + cf.originalFilename + '](' + cf.url + ')');
                 }
             }
    	}
    	
    }
    return {
        get: function() {
        	fileChooser.choose(function(datas) {
               handleFiles(datas);
            });
        }
    }
})(editor);