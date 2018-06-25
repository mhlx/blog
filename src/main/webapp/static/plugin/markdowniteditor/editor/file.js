
var files= (function(editor){
	return {
		get : function(){
			 fileChooser.choose(function(data){
				    var cf = data.cf;
					var ext = cf.extension.toLowerCase();
					if($.inArray(ext,['jpeg','jpg','png','gif']) == -1){
						editor.replaceSelection('['+cf.originalFilename+']('+cf.url+')')
					} else {
						var thumb = cf.thumbnailUrl;
						if(thumb){
							editor.replaceSelection('[!['+cf.originalFilename+']('+thumb.middle+')]('+thumb.large+')')
						} else {
							editor.replaceSelection('!['+cf.originalFilename+']('+cf.url+')')
						}
					}
				   return true;
			 });
		}
	}
})(editor);