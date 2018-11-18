(function(factory) {
  /* global define */
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['jquery'], factory);
  } else if (typeof module === 'object' && module.exports) {
    // Node/CommonJS
    module.exports = factory(require('jquery'));
  } else {
    // Browser globals
    factory(window.jQuery);
  }
}(function($) {
  // Extends plugins for adding file.
  //  - plugin is external module for customizing.
  $.extend($.summernote.plugins, {
    /**
     * @param {Object} context - context object has status of editor.
     */
    'file': function(context) {
      var self = this;

      // ui has renders to build ui elements.
      //  - you can create a button with `ui.button`
      var ui = $.summernote.ui;
      var imageExtensions = ["jpg","jpeg","png","gif"];
      var videoExtensions = ["mp4","mov"];
      var isImage = function(ext){
    	  return $.inArray(ext.toLowerCase(),imageExtensions) != -1;
      }
      var isVideo = function(ext){
    	  return $.inArray(ext.toLowerCase(),videoExtensions) != -1;
      }

      // add file button
      context.memo('button.file', function() {
        // create button
        var button = ui.button({
          contents: '文件',
          container: false,  //add option
          tooltip: '文件',
          click: function() {
        	  fileChooser.choose(function(data){
        		  if(data.length > 0){
        			  for(var i=0;i<data.length;i++){
        				  var f = data[i];
        				  var cf = f.cf;
        				  if(isImage(cf.extension)){
        					  context.invoke('editor.pasteHTML', '<p><img src="'+cf.url+'"/></p>');
        				  }
        				  if(isVideo(cf.extension)){
        					  (async function getPath () {
    							const {value: path} = await swal({
    							  title: '插入视频',
    							  input: 'text',
    							  inputValue: cf.url,
    							  inputPlaceholder:'请输入视频封面地址',
    							  showCancelButton: true,
    							  confirmButtonText:'确定',
    							  cancelButtonText:'取消'
    							})

    							if (path) {
    								context.invoke('editor.pasteHTML', '<p><video controls="" poster="'+path+'"  src="'+cf.url+'" ></video></p>');
    							} else {
    								context.invoke('editor.pasteHTML', '<p><video controls=""  src="'+cf.url+'"  ></video></p>');
    							}

    						})()
        				  }
        			  }
        		  }
        	  })
          }
        });

        // create jQuery object from button instance.
        var $file = button.render();
        return $file;
      });
    }
  });
}));
