var insertFiles = (function(){
	var img_exts = ['jpg','jpeg','gif','png'];
	var video_exts = ['mp4','mov'];
	var isImg = function(ext){
		return img_exts.includes(ext.toLowerCase());
	}
	var isVideo = function(ext){
		return video_exts.includes(ext.toLowerCase());
	}
	
	function processFile(file){
		if(isImg(file.ext)){
			if(file.middleThumbPath){
				return '[!['+file.name+']('+rootPath+file.middleThumbPath+')]('+rootPath+file.largeThumbPath+')';
			} else {
				return '!['+file.name+']('+rootPath+file.path+')';
			}
		}
		if(isVideo(file.ext)){
			if(file.middleThumbPath){
				return '<video src="'+rootPath+file.path+'" poster="'+rootPath+file.middleThumbPath+'" controls></video>'
			} else {
				return '<video  src="'+rootPath+file.path+'" controls></video>'
			}
		}
		return '['+file.name+']('+rootPath+file.path+')';
	}
	
	return function(files,heather){
		var md = '';
		for(const file of files){
			md += processFile(file);
			md += '\n\n';
		}
		heather.replaceSelection(md);
	}
})();