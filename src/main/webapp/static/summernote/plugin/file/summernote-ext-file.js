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

      // add file button
      context.memo('button.file', function() {
        // create button
        var button = ui.button({
          contents: '文件',
          tooltip: '文件',
          click: function() {
        	  fileSelectPageQuery(1,'');
	          $("#fileSelectModal").modal("show");
          }
        });

        // create jQuery object from button instance.
        var $file = button.render();
        return $file;
      });
    }
  });
}));
