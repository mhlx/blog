var inner_bar = (function(editor, config) {
    var $inner_bar = $('<div  class="alpha30 inner-toolbar" style="position:fixed;width:50%;font-size:20px;padding-left:10px;padding-right:10px;visibility:hidden">' +
        '<i class="fas" data-h  data-v="1" style="cursor: pointer;margin-right:20px">H1</i>' +
        '<i class="fas" data-h data-v="2" style="cursor: pointer;margin-right:20px">H2</i>' +
        '<i class="fas" data-h data-v="3" style="cursor: pointer;margin-right:20px">H3</i>' +
        '<i class="fas fa-bold" data-bold style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-italic" data-italic style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-strikethrough" data-strikethrough style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-quote-left" data-quote style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-file" data-file style="cursor: pointer;margin-right:20px;display:none"></i>' +
        '<i class="fas fa-link" data-link style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-table" data-table style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-code" data-code style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-file-code" data-codeblock style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="far fa-square" data-uncheck-list style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="far fa-check-square" data-check-list style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-undo" data-undo style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-redo" data-redo style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-search" data-search style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-times" data-remove style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fas fa-bullseye" data-cursor style="cursor: pointer;margin-right:20px"></i>' +
        '<i class="fab fa-adn" data-selectall style="cursor: pointer;margin-right:20px"></i>' +
        '</div>');

    $inner_bar.appendTo($('body'));
    
    if(getUrlParam("isLogin",'false') == 'true'){
    	$inner_bar.find('[data-file]').show();
    }
    editor.on('scrollCursorIntoView', function(editor, e) {
        if (!config.toolbar) {
            return;
        }
        if ($(window).width() <= 768) {
            cursorScroll = true;
        }
    });
    editor.on('scroll', function(editor) {
        if (!config.toolbar) {
            return;
        }
        if (cursorScroll) {
            cursorScroll = false;
            $("html, body").scrollTop(0);
            var lh = parseFloat($('.CodeMirror').css('line-height'));
            var h = editor.cursorCoords(true).top;
            var top = h > $inner_bar.height() + 2 * lh;
            $inner_bar.css({
                "top": top ? (h - 2 * lh - $inner_bar.height()) + "px" :
                    (h + 2 * lh) + "px",
                "visibility": "visible"
            });
        } else {
            $inner_bar.css({
                "visibility": "hidden"
            });
        }
    });
    var cursorScroll = false;
    editor.on('cursorActivity', function(doc) {
        if (!config.toolbar) {
            return;
        }
        var lh = parseFloat($('.CodeMirror').css('line-height'));
        if ($(window).width() <= 768) {
            var scrollTo = editor.cursorCoords(true, 'local').top -
                $inner_bar.height() - 2 * lh;
            if (scrollTo < 0) {
                $("html, body").scrollTop(0);
                $inner_bar.css({
                    "top": (editor.cursorCoords(true).top + 2 * lh) + "px",
                    "visibility": "visible"
                });
            } else {
                var scrollElement = editor.getScrollerElement();
                var elem = $(scrollElement);
                if (elem[0].scrollHeight - elem.scrollTop() -
                    elem.outerHeight() < 2) {
                    $("html, body").scrollTop(0);
                    var top = editor.cursorCoords(true).top - 2 * lh -
                        $inner_bar.height() - $("#toolbar").height();
                    if (top > 0) {
                        $inner_bar
                            .css({
                                "top": (editor.cursorCoords(true).top - 2 *
                                        lh - $inner_bar.height()) +
                                    "px",
                                "visibility": "visible"
                            });
                    } else {
                        $inner_bar.css({
                            "top": (editor.cursorCoords(true).top + 2 * lh) +
                                "px",
                            "visibility": "visible"
                        });
                    }

                } else {
                    cursorScroll = true;
                    editor.scrollTo(0, scrollTo);
                }

            }
        } else {
            $inner_bar.css({
                "top": (editor.cursorCoords(true).top + 2 * lh) + "px",
                "visibility": "visible"
            });
        }

    });
    $($inner_bar).on('click', '[data-bold]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("****", editor.getCursor());
            editor.focus();
            var str = "**";
            var mynum = str.length;
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh - mynum
            });
        } else {
            editor.replaceSelection("**" + text + "**");
        }
    });

    $inner_bar.on('click', '[data-italic]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("**", editor.getCursor());
            editor.focus();
            var str = "*";
            var mynum = str.length;
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh - mynum
            });
        } else {
            editor.replaceSelection("*" + text + "*");
        }
    });

    $inner_bar.on('click', '[data-quote]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("\n> ", editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh
            });
        } else {
            editor.replaceSelection("> " + text);
        }
    });

    $inner_bar.on('click', '[data-file]', function() {
        files.get();
    });

    $inner_bar.on('click', '[data-uncheck-list]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("\n- [ ] ", editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh
            });
        } else {
            editor.replaceSelection("- [ ] " + text);
        }
    });

    $inner_bar.on('click', '[data-check-list]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("\n- [x] ", editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh
            });
        } else {
            editor.replaceSelection("- [x] " + text);
        }
    });

    $inner_bar.on('click', '[data-link]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("[](http://)", editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh - 10
            });
        } else {
            editor.replaceSelection("[" + text + "](http://)");
        }
    });

    $inner_bar.on('click', '[data-code]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("``", editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh - 1
            });
        } else {
            editor.replaceSelection("`" + text + "`");
        }
    });

    $inner_bar.on('click', '[data-codeblock]', function() {
        var text = "\n```";
        text += '\n';
        text += editor.getSelection() + "";
        text += '\n'
        text += "```";
        editor.focus();
        editor.replaceSelection(text);
        editor.setCursor({
            line: editor.getCursor('start').line - 1,
            ch: 0
        });
    });

    $inner_bar.on('click', '[data-strikethrough]', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("~~~~", editor.getCursor());
            editor.focus();
            var str = "~~";
            var mynum = str.length;
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh - mynum
            });
        } else {
            editor.replaceSelection("~~" + text + "~~");
        }
    });

    $inner_bar.on('click', '[data-undo]', function() {
        editor.execCommand("undo")
    });

    $inner_bar.on('click', '[data-search]', function() {
        $inner_bar.css({
            "visibility": "hidden"
        });
        search.open();
    });

    $inner_bar.on('click', '[data-redo]', function() {
        editor.execCommand("redo")
    });

    $inner_bar.on('click', '[data-selectall]', function() {
        editor.focus();
        editor.execCommand("selectAll")
    });

    var table = '';
    table += '<div class="modal fade" tabindex="-1" role="dialog">';
    table += '<div class="modal-dialog" role="document">';
    table += '<div class="modal-content">';
    table += '<div class="modal-header">';
    table += '<h4 class="modal-title">添加表格</h4>';
    table += '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>';
    table += ' </div>';
    table += '<div class="modal-body">';
    table += '<form>';
    table += '<div class="form-group">';
    table += '<label >行数</label>';
    table += '<input type="number" class="form-control" placeholder="表格行数" id="rows">';
    table += '</div>';
    table += '<div class="form-group">';
    table += '<label >列数</label>';
    table += '<input type="number" class="form-control" placeholder="表格列数" id="cols">';
    table += '</div>';
    table += '</form>';
    table += ' </div>';
    table += '<div class="modal-footer">';
    table += '  <button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>';
    table += '  <button type="button" class="btn btn-primary" data-table-confirm>确定</button>';
    table += '  </div>';
    table += '  </div>';
    table += ' </div>';
    table += '</div>';
    var tableModal = $(table);
    tableModal.appendTo($('body'));

    $inner_bar.on('click', '[data-table]', function() {
        tableModal.modal('show');
    });

    $inner_bar.on('click', '[data-remove]', function() {
        $inner_bar.css({
            "visibility": "hidden"
        });
    });

    $inner_bar.on('click', '[data-up]', function() {
        editor.focus();
        editor.execCommand("goLineUp");
    });

    $inner_bar.on('click', '[data-down]', function() {
        editor.focus();
        editor.execCommand("goLineDown");
    });

    $inner_bar.on('click', '[data-h]', function() {
        var v = parseInt($(this).data('v'));
        var text = editor.getSelection();
        var _text = '\n';
        for (var i = 0; i < v; i++) {
            _text += '#';
        }
        _text += ' ';
        if (text == '') {
            editor.replaceRange(_text, editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh + v
            });
        } else {
            editor.replaceSelection(_text + text);
        }
    });

    $inner_bar.on('click', '[data-cursor]', function() {
        cp.show();
    });

    $("[data-table-confirm]").click(function() {
        var rows = parseInt($("#rows").val()) || 3;
        var cols = parseInt($("#cols").val()) || 3;
        if (rows < 1)
            rows = 3;
        if (cols < 1)
            cols = 3;
        var text = '';
        for (var i = 0; i <= cols; i++) {
            text += '|    ';
        }
        text += "\n";
        for (var i = 0; i < cols; i++) {
            text += '|  -  ';
        }
        text += '|'
        if (rows > 1) {
            text += '\n';
            for (var i = 0; i < rows - 1; i++) {
                for (var j = 0; j <= cols; j++) {
                    text += '|    ';
                }
                text += "\n";
            }
        }
        tableModal.modal('hide');
        editor.replaceSelection("\n" + text);
    });

    return {
        remove: function() {
            if (!cursorScroll || !config.toolbar) {
                $inner_bar.css({
                    "visibility": "hidden"
                });
            }
        }
    }

})(editor, config);