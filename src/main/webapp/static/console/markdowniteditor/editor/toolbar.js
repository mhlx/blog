var bar = (function(editor, config) {
'use strict';
    var css = ".pc_bar{z-index:99;} .mobile_bar{height:60px !important;width:100% !important;overflow-x:auto;font-size:2.5rem !important;white-space:nowrap;z-index:99} ";
    var style = document.createElement('style');
    style.type = 'text/css';
    style.innerHTML = css;
    document.getElementsByTagName('head')[0].appendChild(style);
	
	var ios = CodeMirror.browser.ios;
	var originalPotion = false;
	$(document).ready(function(){
		if (originalPotion === false) 
			originalPotion = $(window).width() + $(window).height();
	});

    function BarItem(icon, click) {
        this.icon = icon;
        this.click = click;
    }

    function Bar() {
        this.items = [];
        this.barElement = this.toEle();
        document.getElementById('in').appendChild(this.barElement);
        this.$barElement = $(this.barElement);
        this.hidden = true;
        this.keepHidden = false;
        init(this);
    }

    function insertAfter(newNode, referenceNode) {
        referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
    }
	
	var keyboardOpen = false;
	
	
	function init(bar){
		if(!CodeMirror.browser.mobile){
			bar.barElement.classList.add('pc_bar');
            editor.on('cursorActivity', function() {
                cursorActivityHandler(bar);
            });
			editor.getScrollerElement().addEventListener('touchmove',function(evt){
				bar.hide();
			});
			editor.on('scroll',function(){
				bar.hide();
			})
		} else {
			editor.on('scroll',function(){
				$("html, body").scrollTop(0);
			})
            editor.on('cursorActivity', function() {
                mobileCursorActivityHandler(bar);
            });
			editor.getScrollerElement().addEventListener('touchmove',function(evt){
				bar.hide();
			});
			
			//ugly determine
			var onKeyboardOnOff = function(isOpen) {
				if (isOpen) {
					keyboardOpen = true;
				} else {
					keyboardOpen = false;
				}
			}
			var applyAfterResize = function() {
				if (!ios && originalPotion !== false) {
					var wasWithKeyboard = $('body').hasClass('view-withKeyboard');
					var nowWithKeyboard = false;
					var diff = Math.abs(originalPotion - ($(window).width() + $(window).height()));
					if (diff > 100) nowWithKeyboard = true;
					$('body').toggleClass('view-withKeyboard', nowWithKeyboard);
					if (wasWithKeyboard != nowWithKeyboard) {
						onKeyboardOnOff(nowWithKeyboard);
					}
				}
			}

			$(window).on('resize orientationchange', function(){
				applyAfterResize();
			});
			
            bar.barElement.classList.add('mobile_bar');
            bar.show();
		}
	}
	

    Bar.prototype.addItem = function(item) {
        insertItem(this,item, this.items.length);
    }

    Bar.prototype.hide = function() {
        this.$barElement.css({
            "visibility": "hidden"
        });
        this.hidden = true;
    }

    Bar.prototype.height = function() {
        return this.$barElement.height();
    }

    Bar.prototype.show = function() {
        if (!config.toolbar || this.keepHidden) {
            return;
        }
        this.$barElement.css({
            "visibility": "visible"
        });
        this.hidden = false;
    }
	
	Bar.prototype.insertItem = function(icon,click,index,action){
		insertItem(this,new BarItem(icon,click),index,action);
	}

   function insertItem(bar,item, index, action) {
        if (index < 0)
            index = 0;
        var action = action || 'replace';
        if (index >= bar.items.length) {
            bar.items.push(item);
            bar.barElement.appendChild(item.toEle());
        } else {
            if (action === 'before') {
                if (index == 0) {
                    bar.items.unshift(item);
                } else {
                    bar.items.splice(index - 1, 0, item);
                }
                bar.barElement.insertBefore(item.toEle(), bar.barElement.childNodes[index]);
            }
            if (action === 'after') {
                bar.items.splice(index, 0, item);
                var is = bar.barElement.querySelectorAll('i')[index];
                insertAfter(item.toEle(), is);
            }
            if (action == 'replace') {
                bar.items.splice(index, 1);
                bar.items.splice(index, 0, item);
                var is = bar.barElement.querySelectorAll('i')[index];
                insertAfter(item.toEle(), is);
                is.parentNode.removeChild(is);
            }
        }
    }

    Bar.prototype.toEle = function() {
        var div = document.createElement('div');
        div.setAttribute('class', 'alpha30 inner-toolbar');
        div.setAttribute('style', 'position:absolute;width:100%;font-size:20px;padding-left:10px;padding-right:10px;visibility:hidden');
        for (var i = 0; i < this.items.length; i++) {
            div.appendChild(this.items[i].toEle());
        }
        return div;
    }

    BarItem.prototype.toEle = function() {
        var i = document.createElement('i');
        i.setAttribute('class', this.icon);
        i.setAttribute('style', 'cursor: pointer;margin-right:20px');
        i.addEventListener('click', this.click)
        return i;
    }


    var bold = new BarItem('fas fa-bold', function() {
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
    var italicItem = new BarItem('fas fa-italic', function() {
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

    var quoteItem = new BarItem('fas fa-quote-left', function() {
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

    var strikethroughItem = new BarItem('fas fa-strikethrough', function() {
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

    var linkItem = new BarItem('fas fa-link', function() {
        var text = editor.getSelection();
        if (text == '') {
            editor.replaceRange("[](https://)", editor.getCursor());
            editor.focus();
            var start_cursor = editor.getCursor();
            var cursorLine = start_cursor.line;
            var cursorCh = start_cursor.ch;
            editor.setCursor({
                line: cursorLine,
                ch: cursorCh - 11
            });
        } else {
            editor.replaceSelection("[" + text + "](https://)");
        }
    });

    var codeItem = new BarItem('fas fa-code', function() {
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

    var codeBlockItem = new BarItem('fas fa-file-code', function() {
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

    var uncheckListItem = new BarItem('far fa-square', function() {
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

    var checkListItem = new BarItem('far fa-check-square', function() {
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


    var undoItem = new BarItem('fas fa-undo', function() {
        editor.execCommand("undo");
    });

    var redoItem = new BarItem('fas fa-redo', function() {
        editor.execCommand("redo");
    });


    var closeItem = new BarItem('fas fa-times', function() {
        bar.hide();
    });

    var tableItem = new BarItem('fas fa-table', function() {
        swal({
            html: '<input id="swal-input1" class="swal2-input" placeholder="列">' +
                '<input id="swal-input2" class="swal2-input" placeholder="行">',
            preConfirm: function() {
                return new Promise(function(resolve) {
                    resolve([
                        $('#swal-input1').val(),
                        $('#swal-input2').val()
                    ])
                })
            },
            onOpen: function() {
                $('#swal-input1').focus()
            }
        }).then(function(result) {
            var value = result.value;
            var cols = parseInt(value[0]) || 3;
            var rows = parseInt(value[1]) || 3;
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
            editor.replaceSelection("\n" + text);
        }).catch(swal.noop)
    })

    var headingItem = new BarItem('fas fa-heading', function() {
        async function getHeading() {
            const {
                value: heading
            } = await Swal.fire({
                input: 'select',
                inputValue: '1',
                inputOptions: {
                    '1': 'H1',
                    '2': 'H2',
                    '3': 'H3',
                    '4': 'H4',
                    '5': 'H5',
                    '6': 'H6'
                },
                inputPlaceholder: '选择标题',
                showCancelButton: true
            });
            if (heading) {
                var v = parseInt(heading);
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
            }
        }
        getHeading();
    })

    var bar = new Bar();
	bar.addItem(new BarItem('fas fa-smile',function(){
		emoji.choose(function(emoji){
			 var text = editor.getSelection();
			if (text == '') {
				editor.replaceRange(emoji, editor.getCursor());
			} else {
				editor.replaceSelection(emoji);
			}
		})
	}))
    bar.addItem(headingItem);
    bar.addItem(bold);
    bar.addItem(italicItem);
    bar.addItem(quoteItem);
    bar.addItem(strikethroughItem);
    bar.addItem(linkItem);
    bar.addItem(codeItem);
    bar.addItem(codeBlockItem);
    if (render.hasPlugin('task-lists')) {
        bar.addItem(uncheckListItem);
        bar.addItem(checkListItem);
    }
    bar.addItem(tableItem);

    bar.addItem(new BarItem('fas fa-search', function() {
        searchHelper.open();
    }));
    bar.addItem(new BarItem('fas fa-arrows-alt', function() {
        cursorHelper.open();
    }))
    bar.addItem(undoItem);
    bar.addItem(redoItem);
    bar.addItem(closeItem);

    var oldHidden;


    var openHandler = function() {
        bar.keepHidden = true;
        bar.hide();
    }

    var closeHandler = function() {
        bar.keepHidden = false;
        bar.show();
        var cursor = editor.getCursor();
        if (cursor)
            editor.setCursor({
                line: cursor.line
            })
    }

    searchHelper.bind('open', openHandler);
    searchHelper.bind('close', closeHandler);
    cursorHelper.bind('open', openHandler);
    cursorHelper.bind('close', closeHandler);

	
    var cursorActivityHandler = function(bar) {
        var lh = editor.defaultTextHeight();
        bar.$barElement.css({
            "top": (editor.cursorCoords(true).top + 2 * lh) + "px",
        });
        bar.show();
    }

	var keyboardTimer;
	var waitTime = 0;

    var mobileCursorActivityHandler = function(bar) {
		$("html, body").scrollTop(0);
        var lh = editor.defaultTextHeight();
        var top = editor.cursorCoords(true, 'local').top;
        var scrollTo = top -
            bar.height() - 2 * lh;
        if (scrollTo < 0) {
            bar.$barElement.css({
                "top": (editor.cursorCoords(true).top + 2 * lh) + "px"
            });
            bar.show();
        } else {
            var scrollElement = editor.getScrollerElement();
            var elem = $(scrollElement);
            if (elem[0].scrollHeight - elem.scrollTop() -
                elem.outerHeight() < 0) {
                var top = editor.cursorCoords(true).top - 2 * lh -
                    bar.height() - $("#toolbar").height();
                if (top > 0) {
                    bar.$barElement.css({
                        "top": (editor.cursorCoords(true).top - 2 *
                            lh - bar.height()) + "px"
                    });

                    bar.show();
                } else {
                    bar.$barElement.css({
                        "top": (editor.cursorCoords(true).top + 2 * lh) + "px"
                    });
                    bar.show();
                }

            } else {
                var _top = editor.cursorCoords(true).top;
				var showBar = function(){
					editor.scrollTo(0, scrollTo);
					setTimeout(function(){
						var h = editor.cursorCoords(true).top;
						var top = h > bar.height() + 2 * lh;
						bar.$barElement.css({
							"top": top ? (h - 2 * lh - bar.height()) + "px" : (h + 2 * lh) + "px"
						});
						bar.show();
					},50)
				}
				
				if(ios){
					showBar();
				} else {
					if(keyboardOpen){
						showBar();
					} else {
						if(keyboardTimer){
							waitTime = 0;
							clearInterval(keyboardTimer);
						}
						keyboardTimer = setInterval(function(){
							if(keyboardOpen){
								waitTime = 0;
								clearInterval(keyboardTimer);
								showBar();
							}else{
								waitTime += 20;
								if(waitTime >= 1020){
									waitTime = 0;
									clearInterval(keyboardTimer);
									showBar();
								}
							}
						},20);
					}
				}
				
               
            }

        }
    }
    return bar;
})(editor, config);