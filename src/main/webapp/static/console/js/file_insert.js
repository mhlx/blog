var files = (function() {
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
    videoModal += '<button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>';
    videoModal += '<button type="button" class="btn btn-primary">插入</button>';
    videoModal += ' </div>';
    videoModal += ' </div>';
    videoModal += ' </div>';
    videoModal += ' </div>';
    $video = $(videoModal);
    $video.appendTo($('body'));
    var showVideoModal = function(url) {
        $video.find('input[type="text"]').val(url);
        $video.find('textarea').val('<video src="' + url + '" poster="" controls></video>');
        var index = 22 + url.length;
        $video.find('textarea').unbind('click').bind('click', function() {
            $(this)[0].setSelectionRange(index, index+1);
            $(this).unbind('click');
        });
        $video.modal({
            'backdrop': 'static'
        });
    }
    
    var updateCodeMirror = function(editor,data){
    	editor.replaceRange('\n'+data+'\n', editor.getCursor()); // adds a new line
	}
    var handleFiles = function(editor,datas){
    	for(var i=0;i<datas.length;i++){
    		var data = datas[i];
    		 var cf = data.cf;
             var ext = cf.extension.toLowerCase();
             if ($.inArray(ext, ['jpeg', 'jpg', 'png', 'gif']) == -1) {
                 if (ext == 'mp4' || ext == 'mov') {
                     showVideoModal(cf.url);
                     $video.find('.modal-footer button').eq(1).unbind('click').bind('click',function() {
                         var content = $video.find('textarea').val();
                         updateCodeMirror(editor,content);
                 		 $video.modal('hide');
                 		 var _datas = [];
                 		 for(var j=i+1;j<datas.length;j++){
                 			 _datas.push(datas[j]);
                 		 }
                 		 handleFiles(_datas);
                     });
                     return ;
                 } else {
                	 updateCodeMirror(editor,'[' + cf.originalFilename + '](' + cf.url + ')');
                 }
             } else {
                 var thumb = cf.thumbnailUrl;
                 if (thumb) {
                	 updateCodeMirror(editor,'[![' + cf.originalFilename + '](' + thumb.middle + ')](' + thumb.large + ')');
                 } else {
                	 updateCodeMirror(editor,'![' + cf.originalFilename + '](' + cf.url + ')');
                 }
             }
    	}
    	
    }
    return {
        get: function(editor) {
        	fileChooser.choose(function(datas) {
               handleFiles(editor,datas);
            });
        }
    }
})();