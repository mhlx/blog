(function(config,editor){
  var _cursor;
   var toEditor = function() {
                $("#out").hide();
                $("#toolbar").css({
                    width: '100%',
                    height: '45px'
                });
                $("#in").css({
                    width: '100%',
                    top: '45px'
                }).show();
                $("#editor-icon").remove();
                $("#preview-icon").remove();
                $(".icon").show();
                $("#toolbar")
                    .append(
                        '<span data-to-preview class="glyphicon glyphicon-eye-open icon" id="preview-icon" title="预览"></span>');
                if (editor) {
                    editor.focus();
                    if (_cursor) {
                        editor.setCursor(_cursor.line, _cursor.ch);
                    }
                }
            }

            var toPreview = function() {
                _cursor = editor.getCursor();
                inner_bar.remove();
                render.md(0);
             // sync.resetAndSync();
                $("#in").hide();
                $("#out").css({
                    left: '0%',
                    top: '45px'
                }).show();
                $("#editor-icon").remove();
                $("#preview-icon").remove();
                $(".icon").hide();
                $("#toolbar img").hide();
                $("#toolbar")
                    .append(
                        '<span data-to-editor class="glyphicon glyphicon-eye-close icon" id="editor-icon" title="取消预览"></span>');
            }
            
  					var wwidth = $(window).width();
                if (wwidth <= 768) {
                  config.autoRender = false;
                    $("#fullscreen-icon").remove();
                    toEditor();
                    $("#mobile-style").remove();
                    $("head")
                        .append(
                            "<style type='text/css' id='mobile-style'>.icon {font-size: 30px} .CodeMirror-scroll{margin-top:10px} .inner-toolbar{width:100% !important;font-size:20px !important} #search-box{width:100% !important}</style>");
                } else {
                  	var o = $("#toggle-toolbar");
                   o.addClass("fa-square").removeClass("fa-check-square");
                  config.toolbar = false;
                  config.autoRender = true;
                    $("#in").css({
                        width: '50%',
                        top: '30px'
                    }).show();
                    $("#out").css({
                        left: '50%',
                        top: 0
                    }).show();
                    $("#toolbar").css({
                        width: '50%'
                    });
                    $("#editor-icon").remove();
                    $("#preview-icon").remove();
                    $("#mobile-style").remove();
					editor.setOption('dropFileContentHandler',function(fileName,content){
                      var ext = fileName.split(".").pop().toLowerCase();
                      if (ext == "md") {
                       		return content;
                      } else if (ext == "html" || ext == 'htm') {
                           return turndownService
                                          .turndown(content);
                      }
                      return "";
                    });
                }

  
  $(document).on('click', '[data-to-preview]', function() {
    toPreview();
  });
  $(document).on('click', '[data-to-editor]', function() {
    toEditor();
  });
   $(document).on('click', '[data-to-fullscreen]', function() {
    fullScreen($(this));
  });
  
   function fullScreen(o) {
            if (o.hasClass("fa-window-minimize")) {
                $("#out").show();
                $("#toolbar").css({
                    width: '50%',
                    left: 0
                }).show();
                $("#in").css({
                    width: '50%',
                    left: 0
                }).show();
                $("#fullscreen-style").remove();
                o.removeClass("fa-window-minimize").addClass("fa-window-maximize");
              config.syncScroll = true;
              scrollMap = null;
              sync.doSync();
            } else {
                $("#out").hide();
                $("#toolbar").css({
                    width: '80%',
                    left: '10%'
                }).show();
                $("#in").css({
                    width: '80%',
                    left: '10%',
                    top: '30px'
                }).show();
                $("#mobile-style").remove();
                $("head")
                    .append(
                        "<style type='text/css' id='fullscreen-style'> .inner-toolbar{width:80% !important;left:10% !important} #search-box{width:80% !important;left:10% !important;} #stat{width:80% !important;left:10% !important;}</style>");
 				config.syncScroll = false;
                o.removeClass("fa-window-maximize").addClass("fa-window-minimize");
            }
        }
  editor.focus();
  editor.setCursor({line:0});

})(config,editor);