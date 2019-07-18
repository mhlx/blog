var EditorWrapper = (function() {
    function cloneAttributes(element, sourceNode) {
        let attr;
        let attributes = Array.prototype.slice.call(sourceNode.attributes);
        while (attr = attributes.pop()) {
            element.setAttribute(attr.nodeName, attr.nodeValue);
        }
    }

    function insertAfter(newNode, referenceNode) {
        referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
    }

    var isUndefined = function(o) {
        return (typeof o == 'undefined')
    }
	
	function getDefault(o,dft){
		return isUndefined(o) ? dft : o;
	}

    var Render = (function() {
        'use strict';

        function MarkdownRender(config) {
            this.md = createMarkdownParser({
                html: config.render_allowHtml !== false,
                plugins: config.render_plugins || ['footnote', 'katex', 'mermaid', 'anchor', 'task-lists', 'sup', 'sub', 'abbr'],
                lineNumber: true
            });
			this.md2 = createMarkdownParser({
                html: config.render_allowHtml !== false,
                plugins: config.render_plugins || ['footnote', 'katex', 'mermaid', 'anchor', 'task-lists', 'sup', 'sub', 'abbr'],
                lineNumber: false
            });
            this.config = config;
            this.eventHandlers = [];
        }

        var mermaidLoading = false;

        function loadMermaid() {
            if (mermaidLoading) return;
            mermaidLoading = true;
            $('<script>').appendTo('body').attr({
                src: rootPath+'static/heather/js/mermaid.min.js'
            });
            var t = setInterval(function() {
                try {
                    mermaid.initialize({
                        theme: theme.mermaid.theme || 'default'
                    });
                    clearInterval(t);
                    mermaid.init({}, '#out .mermaid');
                } catch (e) {

                }
            }, 20)
        }

        var katexLoading = false;

        function loadKatex() {
            if (katexLoading) return;
            katexLoading = true;
            $('<link>').appendTo('head').attr({
                type: 'text/css',
                rel: 'stylesheet',
                href: rootPath+'static/heather/katex/katex.min.css'
            });
            $('<script>').appendTo('body').attr({
                src: rootPath+'static/heather/katex/katex.min.js'
            });
            var t = setInterval(function() {
                try {
                    var html = katex.renderToString("", {
                        throwOnError: false
                    })
                    clearInterval(t);
                    var katexs = document.getElementById("out").querySelectorAll(".katex");
                    for (var i = 0; i < katexs.length; i++) {
                        var block = katexs[i];
                        block.innerHTML = katex.renderToString(block.textContent, {
                            throwOnError: false,
                            displayMode: true
                        });
                    }
                } catch (e) {

                }
            }, 20)
        }

		MarkdownRender.prototype.getHtml = function(markdownText){
			return this.md2.render(markdownText);
		}

        MarkdownRender.prototype.renderAt = function(markdownText, element, patch) {
            var doc = $.parseHTML2(this.md.render(markdownText));
            var hasMermaid = doc.querySelector('.mermaid') != null;
            if (hasMermaid) {
                loadMermaid();
            }
            var hasKatex = doc.querySelector(".katex") != null;
            if (hasKatex) {
                loadKatex()
            }
            if (this.config.render_beforeRender) {
                this.config.render_beforeRender(doc);
            }
            var innerHTML = doc.body.innerHTML;
            if (patch) {
                var div = document.createElement('div');
                cloneAttributes(div, element)
                div.innerHTML = innerHTML;
                morphdom(element, div, {
                    onBeforeElUpdated: function(f, t) {
                        if (f.isEqualNode(t)) {
                            return false;
                        }
                        if (f.classList.contains('mermaid-block') &&
                            t.classList.contains('mermaid-block')) {
                            var old = f.getElementsByClassName('mermaid-source')[0].textContent;
                            var now = t.getElementsByClassName('mermaid-source')[0].textContent;
                            if (old == now) {
                                //更新属性
                                cloneAttributes(f, t);
                                return false;
                            }
                        }
                        return true;
                    }
                });
            } else {
                element.innerHTML = innerHTML;
            }
            try {
                mermaid.initialize({
                    theme: theme.mermaid.theme || 'default'
                });
                mermaid.init({}, '#out .mermaid');
            } catch (e) {}
        }

        return {
            create: function(config) {
                return new MarkdownRender(config)
            }
        }
    })();


    var Bar = (function() {
        'use strict';

        function Bar(element, config) {
            this.element = $(element);
            this.keepHidden = false;
        }


        Bar.prototype.hide = function() {
            this.element.css({
                "visibility": "hidden"
            });
            this.hidden = true;
        }

        Bar.prototype.height = function() {
            return this.element.height();
        }

        Bar.prototype.show = function() {
            if (this.keepHidden) {
                return;
            }
            this.element.css({
                "visibility": "visible"
            });
            this.hidden = false;
        }


        Bar.prototype.addItem = function(item) {
            insertItem(this, item, this.items.length);
        }


        function createElement(icon, handler) {
            var i = document.createElement('i');
            i.setAttribute('class', icon);
            i.setAttribute('style', 'cursor: pointer;margin-right:20px');
            i.addEventListener('click', function() {
                handler(i);
            })
            return i;
        }


        Bar.prototype.getSize = function() {
            return this.element.find('i').length;
        }


        Bar.prototype.insertIcon = function(clazz, handler, index, callback) {
            var newIcon = createElement(clazz, handler);
            if (callback) callback(newIcon);
            var toolbar = this.element[0];
            if (index >= this.getSize()) {
                toolbar.appendChild(newIcon);
            } else {
                if (index <= 0) {
                    toolbar.insertBefore(newIcon, toolbar.childNodes[0])
                } else {
                    toolbar.insertBefore(newIcon, toolbar.childNodes[index])
                }
            }
        }

        Bar.prototype.addIcon = function(clazz, handler, callback) {
            this.insertIcon(clazz, handler, this.getSize(), callback);
        }

        Bar.prototype.removeIcon = function(deleteChecker) {
            var icons = this.element[0].querySelectorAll("i");
            for (var i = icons.length - 1; i >= 0; i--) {
                var icon = icons[i];
                if (deleteChecker(icon)) {
                    this.element[0].removeChild(icon);
                }
            }
        }

        return {
            create: function(element) {
                return new Bar(element);
            }
        };
    })();


    var Sync = (function(editor) {
        'use strict';

        function Sync(editor, scrollElement, config) {
            this.editor = editor;
            this.scrollElement = scrollElement;
            this.config = config;
        }

        var getElementByLine = function(scrollElement, line) {
            return scrollElement.querySelector('[data-line="' + line + '"]');
        }
        var getElementByEndLine = function(scrollElement, line) {
            return scrollElement.querySelector('[data-end-line="' + line + '"]');
        }
        var getLineMarker = function(scrollElement) {
            return scrollElement.querySelectorAll('[data-line]');
        }

        function getEditorScrollInfo(editor, scrollElement) {
            var lines = [];
            var lineMarkers = getLineMarker(scrollElement);
            lineMarkers.forEach(function(ele) {
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
            if (!isUndefined(lastMarker) && isUndefined(nextMarker)) {
                nextMarker = parseInt(lineMarkers[lineMarkers.length - 1].getAttribute('data-end-line'));
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

        Sync.prototype.doSync = function() {
            var editorScroll = getEditorScrollInfo(this.editor, this.scrollElement);
            let lastPosition = 0
            var o = $(this.scrollElement);
            let nextPosition = o.outerHeight();
            var last;
            if (!isUndefined(editorScroll.lastMarker)) {
                last = getElementByLine(this.scrollElement, editorScroll.lastMarker);
                if (!isUndefined(last)) {
                    lastPosition = last.offsetTop - 10
                }
            }
            var next;
            if (!isUndefined(editorScroll.nextMarker)) {
                next = getElementByLine(this.scrollElement, editorScroll.nextMarker) || getElementByEndLine(this.scrollElement, editorScroll.nextMarker)
                if (!isUndefined(next)) {
                    nextPosition = next.offsetTop - 10
                }
            }
            var pos = nextPosition - lastPosition;
            if (!isUndefined(last) && !isUndefined(next) && last === next) {
                pos = last.clientHeight;
            }
            var ms = this.config.sync_animateMs || 0;
            const scrollTop = lastPosition + pos * editorScroll.percentage;
            o.stop(true);
            o.animate({
                scrollTop: scrollTop
            }, ms);
        }

        return {
            create: function(editor, scrollElement, config) {
                return new Sync(editor, scrollElement, config);
            }
        }
    })();


    EditorWrapper = (function() {

        var mobile = CodeMirror.browser.mobile;
        var ios = CodeMirror.browser.ios;

        CodeMirror.prototype.renderAllDoc = function(scrollToTop) {
            var editor = this;
            editor.setOption('readOnly', true);
            var viewport = editor.getViewport();
            var lastLine = editor.lineCount() - 1;
            while (viewport.to < lastLine && viewport.to > 0) {
                editor.scrollIntoView({
                    line: viewport.to
                });
                viewport = editor.getViewport();
            }

            editor.scrollIntoView({
                line: lastLine
            });
            editor.scrollIntoView({
                top: scrollToTop
            });
            editor.setOption('readOnly', false);
        }


        CodeMirror.keyMap.default["Shift-Tab"] = "indentLess";
        CodeMirror.keyMap.default["Tab"] = "indentMore";
		
		var theme = (function(){
			function Theme(){
				this.toolbar = {};
				this.bar = {};
				this.stat = {};
				this.editor = {};
				this.inCss = {};
				this.searchHelper = {};
				this.mermaid = {};
				this.hljs = {theme:'github'};
				this.customCss = undefined;
				this.timer = undefined;
			}
			
			var themeKey = "markdown-editor-theme";
			function saveTheme(theme,cb){
				var json = JSON.stringify(theme);
				try{localStorage.setItem(themeKey,json);
				if(cb) cb("success");
				}catch(e){
				if(cb) cb("fail");
				}
			}
			function getTheme(cb){
				if(!cb) return ;
				var json = localStorage.getItem(themeKey);
				if(json == null ){
					cb(null)
				}else{
					cb($.parseJSON(json));
				}
					
			}
			function delTheme(cb){
				try{
					localStorage.removeItem(themeKey);
					if(cb) cb("success");
				}catch(e){if(cb) cb("fail");
				}
			}
			
			Theme.prototype.store = function(callback){
				if(this.timer){
					clearTimeout(this.timer);
				}
				var theme = this;
				this.timer = setTimeout(function(){	
					saveTheme(theme,function(cb){
						if(callback) callback(cb == "success" ? true : false);
					});
				},500)
			}
			
			Theme.prototype.reset = function(){
				var theme = new Theme();
				saveTheme(theme);
				theme.render();
				return theme;
			}
			
			Theme.prototype.clone = function(){
				var copy = JSON.parse(JSON.stringify(this));
				var theme = new Theme();
				theme.toolbar = copy.toolbar;
				theme.bar = copy.bar;
				theme.stat = copy.stat;
				theme.editor = copy.editor;
				theme.inCss = copy.inCss;
				theme.cursorHelper = copy.cursorHelper;
				theme.searchHelper = copy.searchHelper;
				theme.mermaid = copy.mermaid;
				theme.customCss = copy.customCss;
				return theme;
			}
			
			var getCurrentTheme = function(){
				var current;
				getTheme(function(cb){
					current = cb
				});
				if(!current || current == null){
					return new Theme();
				}
				var theme = new Theme();
				theme.toolbar = current.toolbar;
				theme.bar = current.bar;
				theme.stat = current.stat;
				theme.editor = current.editor;
				theme.inCss = current.inCss;
				theme.cursorHelper = current.cursorHelper;
				theme.searchHelper = current.searchHelper;
				theme.mermaid = current.mermaid;
				theme.customCss = current.customCss;
				return theme;
			}
			
			Theme.prototype.loadEditorTheme = function(onLoad){
				if(this.editor.theme){
					var editorTheme = this.editor.theme;
					var loadHandler = function(theme){
						onLoad(theme);
					}
					if($('head link[href="'+rootPath+'static/codemirror/theme/'+editorTheme+'.css"]').length == 0){
						$('<link id="codemirror-theme-'+editorTheme+'" >').appendTo('head').attr({
						  type: 'text/css', 
						  rel: 'stylesheet',
						  onload:function(){
							  loadHandler(editorTheme);
						  },
						  href: rootPath+'static/codemirror/theme/'+editorTheme+'.css'
					   })
					}else{
						loadHandler(editorTheme);
					}
				}
			}
			
			Theme.prototype.loadHljsTheme = function(onLoad){
				if(this.hljs.theme){
					var hljsTheme = this.hljs.theme;
					var loadHandler = function(theme){
						onLoad(theme);
					}
					if($('head link[href="'+rootPath+'static/heather/highlight/styles/'+hljsTheme+'.css"]').length == 0){
						$('<link id="hljs-theme-'+hljsTheme+'" >').appendTo('head').attr({
						  type: 'text/css', 
						  rel: 'stylesheet',
						  onload:function(){
							  loadHandler(hljsTheme);
						  },
						  href: ''+rootPath+'static/heather/highlight/styles/'+hljsTheme+'.css'
					   })
					}else{
						loadHandler(hljsTheme);
					}
				}
			}
			
			
			Theme.prototype.render = function(pro){
				var css = "";
				css += "#toolbar{color:"+(this.toolbar.color || 'inherit')+"}\n";
				css += "#innerBar{color:"+(this.bar.color || 'inherit')+"}\n"
				css += "#stat{color:"+(this.stat.color || 'inherit')+"}\n";
				css += "#in{background:"+(this.inCss.background || 'inherit')+"}\n";
				css += "#searchHelper{color:"+(this.searchHelper.color || 'inherit')+"}\n#searchHelper input{color:"+(this.searchHelper.color || 'inherit')+"}\n\n#searchHelper .input-group-text{background:#fff;color:"+(this.searchHelper.color || 'inherit')+"}\n";
				if(this.customCss){
					css += this.customCss;
				}
				var theme = this;
				this.loadEditorTheme(function(theme){
					if(pro && pro.editorThemeLoad){pro.editorThemeLoad(theme)}
				})
				this.loadHljsTheme(function(theme){
					if(pro && pro.hljsThemeLoad){pro.hljsThemeLoad(theme)}
				})
				$("#custom_theme").remove();
				if($.trim(css) != ''){
					$("head").append("<style type='text/css' id='custom_theme'>"+css+"</style>");
				}
			}
			
			return getCurrentTheme();
		})();
		
		theme.render();

        function EditorWrapper(config) {
            if (!mobile) {
                $("#in").show();
                $("#out").css({
                    'visibility': 'visible'
                });
            }
            $("#wrapper").animate({
                scrollLeft: $("#toc").outerWidth()
            }, 0);
            var scrollBarStyle = mobile ? 'native' : 'overlay';
            var editorTheme = theme.editor.theme ? theme.editor.theme : 'default';
            var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
                mode: {
                    name: "gfm"
                },
                lineNumbers: false,
                matchBrackets: true,
                lineWrapping: true,
                dragDrop: true,
                scrollbarStyle: scrollBarStyle,
                theme: editorTheme,
                styleSelectedText: true,
                extraKeys: {
                    "Enter": "newlineAndIndentContinueMarkdownList",
                    "Alt-F": "findPersistent",
                    "Ctrl-A": "selectAll"
                }
            });
            var turndownService = config.turndownService;

            if (!turndownService) {
                turndownService = new window.TurndownService({
                    'headingStyle': 'atx',
                    'codeBlockStyle': 'fenced',
                    defaultReplacement: function(innerHTML, node) {
                        return node.isBlock ? '\n\n' + node.outerHTML + '\n\n' : node.outerHTML
                    }
                });
                turndownService.use(window.turndownPluginGfm.gfm);
            }

            var dropHtmlMarkdownEnable = config.drop_htmlMarkdownEnable !== false;

            if (dropHtmlMarkdownEnable) {
                //drop md|html|htm file

                var contentHandler = config.drop_htmlMarkdownHandler || function(ext, content, turndownService) {
                    if (ext == "md") {
                        return content;
                    } else if (ext == "html" || ext == 'htm') {
                        return turndownService.turndown(content);
                    }
                }
                editor.setOption('dropContentHandler', function(fileName, content) {
                    var ext = fileName.split(".").pop().toLowerCase();
                    if (ext == 'md' || ext == 'html' || ext == 'htm') {
                        return contentHandler(ext, content, turndownService);
                    }
                    return "";
                });
            }

            this.sync = Sync.create(editor, $("#out")[0], config);
            this.render = Render.create(config);
            this.toolbar = Bar.create($("#toolbar")[0]);
            var innerBar = Bar.create($("#innerBar")[0]);
            innerBar.hide();
            this.innerBar = innerBar;
            //sync 
            var wrapper = this;
            if (!mobile) {
                //auto render
                var ms =  getDefault(config.render_ms,500);
                var autoRenderTimer;
                var stat_timer;
                editor.on('change', function() {
                    if (autoRenderTimer) {
                        clearTimeout(autoRenderTimer);
                    }
                    autoRenderTimer = setTimeout(function() {
                        wrapper.doRender(true);
                    }, ms)
                    var statEnable = config.stat_enable !== false;
                    if (statEnable) {
                        var formatter = config.stat_formatter || function(wrapper) {
                            return "当前字数：" + wrapper.editor.getValue().length
                        }
                        $("#stat").html(formatter(wrapper)).show();
                        if (stat_timer) {
                            clearTimeout(stat_timer);
                        }
                        stat_timer = setTimeout(function() {
                            $("#stat").hide();
                        }, 1000);
                    }

                });
                //sync
                var scrollHandler = function() {
                    wrapper.doSync();
                };
                var syncEnable = config.sync_enable !== false;
                if (syncEnable) {
                    editor.on('scroll', scrollHandler);
                    editor.on('update', function handler() {
                        editor.off('update', handler);
                        editor.renderAllDoc(0);
                    });
                }
                this.syncEnable = syncEnable;
                this.scrollHandler = scrollHandler;
            }

            if (mobile) {
                //swipe
                $("#toc").touchwipe({
                    wipeLeft: function() {
                        wrapper.toEditor()
                    },
                    min_move_x: 10,
                    max_move_y: 5
                });
                $(editor.getScrollerElement()).touchwipe({
                    wipeLeft: function() {
                        wrapper.toPreview()
                    },
                    wipeRight: function() {
                        wrapper.toToc()
                    },
                    min_move_x: 10,
                    max_move_y: 5
                });
                $("#out").touchwipe({
                    wipeRight: function() {
                        wrapper.toEditor()
                    },
                    min_move_x: 10,
                    max_move_y: 5
                });
            }
            $("#toc").on('click', '[data-line]', function() {
                var line = parseInt($(this).data('line'));

                editor.scrollIntoView({
                    line: line
                });
                setTimeout(function() {
                    var top = editor.charCoords({
                        line: line,
                        ch: 0
                    }, "local").top;
                    editor.scrollTo(null, top);
                    if (mobile) {
                        wrapper.toEditor();
                    }
                }, 500)
            })
            this.editor = editor;
			this.theme = theme;
			this.config = config;
            initInnerBar(this);
            initToolbar(this);
        }

        EditorWrapper.prototype.doRender = function(patch) {
            this.render.renderAt(this.editor.getValue(), $("#out")[0], patch);
            renderToc();
        }

        EditorWrapper.prototype.doSync = function() {
            this.sync.doSync();
        }

        EditorWrapper.prototype.enableSync = function() {
            if (!this.syncEnable) {
                editor.on('scroll', this.scrollHandler)
                this.syncEnable = true;
            }
        }
		
		EditorWrapper.prototype.getHtml = function() {
            return this.render.getHtml(this.editor.getValue());
        }

        EditorWrapper.prototype.disableSync = function() {
            if (this.syncEnable) {
                editor.off('scroll', this.scrollHandler);
                this.syncEnable = false;
            }
        }

        EditorWrapper.prototype.toEditor = function(callback) {
            var ms = getDefault(this.config.swipe_animateMs,500);
            if (mobile) {
                $("#wrapper").animate({
                    scrollLeft: $("#in").width()
                }, ms, function() {
                    if (callback) callback();
                });
            } else {
                $("#wrapper").animate({
                    scrollLeft: $("#in").offset().left
                }, ms, function() {
                    if (callback) callback();
                });
            }
        }


        EditorWrapper.prototype.toToc = function(callback) {
            this.editor.getInputField().blur();
            if (mobile) {
                this.doRender(true);
            }
            var ms = getDefault(this.config.swipe_animateMs,500);
            $("#wrapper").animate({
                scrollLeft: 0
            }, ms, function() {
                if (callback) callback();
            });
        }

        EditorWrapper.prototype.toPreview = function(callback) {
            if (mobile) {
                this.editor.getInputField().blur();
                this.doRender(true);
                this.doSync();
				var ms = getDefault(this.config.swipe_animateMs,500);
                $("#wrapper").animate({
                    scrollLeft: $("#out")[0].offsetLeft
                }, ms, function() {
                    if (callback) callback();
                });
            }
        }



        function initInnerBar(wrapper) {
            var innerBar = wrapper.innerBar;
            var editor = wrapper.editor;
			var config = wrapper.config;

            var innerBarElement = $("#innerBar");
            var icons = config.innerBar_icons || ['emoji','heading', 'bold', 'italic', 'quote', 'strikethrough', 'link', 'code', 'code-block', 'uncheck', 'check', 'table', 'undo', 'redo', 'close'];
            var ios = CodeMirror.browser.ios;
            for (var i = 0; i < icons.length; i++) {
                var icon = icons[i];
				if (icon == 'emoji'){
					addEmoji();
				}
                if (icon == 'heading') {
                    addHeading();
                }
                if (icon == 'bold') {
                    addBold();
                }
                if (icon == 'italic') {
                    addItalic();
                }
                if (icon == 'quote') {
                    addQuote();
                }
                if (icon == 'strikethrough') {
                    addStrikethrough();
                }
                if (icon == 'link') {
                    addLink();
                }
                if (icon == 'code') {
                    addCode();
                }
                if (icon == 'code-block') {
                    addCodeBlock();
                }
                if (icon == 'uncheck') {
                    addUncheckList();
                }
                if (icon == 'check') {
                    addCheckList();
                }
                if (icon == 'undo') {
                    addUndo();
                }
                if (icon == 'redo') {
                    addRedo();
                }
                if (icon == 'close') {
                    addClose();
                }
                if (icon == 'table') {
                    addTable();
                }
            }
			
			function addEmoji(){
				innerBar.addIcon('far fa-grin-alt icon', function() {
					var emojiArray = config.emojiArray || $.trim("😀 😁 😂 🤣 😃 😄 😅 😆 😉 😊 😋 😎 😍 😘 😗 😙 😚 ☺️ 🙂 🤗 🤔 😐 😑 😶 🙄 😏 😣 😥 😮 🤐 😯 😪 😫 😴 😌 😛 😜 😝 🤤 😒 😓 😔 😕 🙃 🤑 😲 ☹️ 🙁 😖 😞 😟 😤 😢 😭 😦 😧 😨 😩 😬 😰 😱 😳 😵 😡 😠 😷 🤒 🤕 🤢 🤧 😇 🤠 🤡 🤥 🤓 😈 👿 👹 👺 💀 👻 👽 🤖 💩 😺 😸 😹 😻 😼 😽 🙀 😿 😾").split(' ');
					var html = '';
					for (var i = 0; i < emojiArray.length; i++) {
						html += '<span data-emoji style="cursor:pointer">' + emojiArray[i]
						+ '</span>';
					}
					swal({
						html : html
					})
					$(Swal.getContent()).find('[data-emoji]').click(function(){
						var emoji = $(this).text();
						var text = editor.getSelection();
						if (text == '') {
							editor.replaceRange(emoji, editor.getCursor());
						} else {
							editor.replaceSelection(emoji);
						}
						Swal.close();
					})
				});
				
			}

            function addHeading() {
                innerBar.addIcon('fas fa-heading icon', function() {
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
                            inputPlaceholder: '',
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
            }

            function addBold() {
                innerBar.addIcon('fas fa-bold icon', function() {
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
            }

            function addItalic() {
                innerBar.addIcon('fas fa-italic icon', function() {
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
                })
            }

            function addQuote() {
                innerBar.addIcon('fas fa-quote-left icon', function() {
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
                })
            }

            function addStrikethrough() {
                innerBar.addIcon('fas fa-strikethrough icon', function() {
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
                })
            }


            function addLink() {
                innerBar.addIcon('fas fa-link icon', function() {
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
            }

            function addCodeBlock() {
                innerBar.addIcon('fas fa-file-code icon', function() {
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
            }

            function addCode() {
                innerBar.addIcon('fas fa-code icon', function() {
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
            }

            function addUncheckList() {
                innerBar.addIcon('far fa-square icon', function() {
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
            }

            function addCheckList() {
                innerBar.addIcon('far fa-check-square icon', function() {
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
            }

            function addUndo() {
                innerBar.addIcon('fas fa-undo icon', function() {
                    editor.execCommand("undo");
                });
            }

            function addRedo() {
                innerBar.addIcon('fas fa-redo icon', function() {
                    editor.execCommand("redo");
                });
            }

            function addClose() {
                innerBar.addIcon('fas fa-times icon', function() {
                    innerBar.hide();
                });
            }

            function addTable() {
                innerBar.addIcon('fas fa-table icon', function() {
                    innerBar.hide();
                    swal({
                        html: '<input id="swal-input1" class="swal2-input" placeholder="行">' +
                            '<input id="swal-input2" class="swal2-input" placeholder="列">',
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
                        innerBar.show();
                    }).catch(swal.noop)
                })
            }


            var cursorActivityHandler = function(bar) {
                var lh = editor.defaultTextHeight();
                innerBarElement.css({
                    "top": (editor.cursorCoords(true).top + 2 * lh) + "px",
                });
                bar.show();
            }

            var keyboardTimer;
            var mobileCursorActivityHandler = function(bar) {
                $("html, body").scrollTop(0);
                var lh = editor.defaultTextHeight();
                var top = editor.cursorCoords(true, 'local').top;
                var scrollTo = top -
                    bar.height() - 2 * lh;
                if (scrollTo < 0) {
                    innerBarElement.css({
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
                            innerBarElement.css({
                                "top": (editor.cursorCoords(true).top - 2 *
                                    lh - bar.height()) + "px"
                            });

                            bar.show();
                        } else {
                            innerBarElement.css({
                                "top": (editor.cursorCoords(true).top + 2 * lh) + "px"
                            });
                            bar.show();
                        }

                    } else {
                        var _top = editor.cursorCoords(true).top;
                        var showBar = function() {
                            editor.scrollTo(0, scrollTo);
                            setTimeout(function() {
                                var h = editor.cursorCoords(true).top;
                                var top = h > bar.height() + 2 * lh;
                                innerBarElement.css({
                                    "top": top ? (h - 2 * lh - bar.height()) + "px" : (h + 2 * lh) + "px"
                                });
                                bar.show();
                            }, 50)
                        }

                        if (ios) {
                            showBar();
                        } else {
                            if (keyboardDetector.isOpen) {
                                showBar();
                            } else {
                                if (keyboardTimer) {
                                    waitTime = 0;
                                    clearInterval(keyboardTimer);
                                }
                                keyboardTimer = setInterval(function() {
                                    if (keyboardDetector.isOpen) {
                                        waitTime = 0;
                                        clearInterval(keyboardTimer);
                                        showBar();
                                    } else {
                                        waitTime += 20;
                                        if (waitTime >= 1020) {
                                            waitTime = 0;
                                            clearInterval(keyboardTimer);
                                            showBar();
                                        }
                                    }
                                }, 20);
                            }
                        }


                    }

                }
            }

            if (!CodeMirror.browser.mobile) {
                editor.on('cursorActivity', function() {
                    cursorActivityHandler(innerBar);
                });
                editor.getScrollerElement().addEventListener('touchmove', function(evt) {
                    innerBar.hide();
                });
                editor.on('scroll', function() {
                    innerBar.hide();
                })
            } else {

                var keyboardDetector = (function() {
                    var keyboardOpen = false;
                    var originalPotion = false;
                    if (originalPotion === false)
                        originalPotion = $(window).width() + $(window).height();
                    var waitTime = 0;
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

                    $(window).on('resize orientationchange', function() {
                        applyAfterResize();
                    });

                    return {
                        isOpen: keyboardOpen
                    }
                })();

                editor.on('scroll', function() {
                    $("html, body").scrollTop(0);
                })
                editor.on('cursorActivity', function() {
                    mobileCursorActivityHandler(innerBar);
                });
                editor.getScrollerElement().addEventListener('touchmove', function(evt) {
                    innerBar.hide();
                });
            }
        }

        function initToolbar(wrapper) {
            var editor = wrapper.editor;
			var theme = wrapper.theme;
            var cm = editor;
			var config = wrapper.config;
            ////////////////////store 
            var Store = (function() {
                function Store(key) {
                    this.key = key;
                }
                Store.prototype.addDocument = function(title, content) {
                    var documents = this.getDocuments();
                    deleteDocumentByTitle(documents, title);
                    documents.push({
                        title: title,
                        content: content,
                        time: $.now()
                    });
                    storeDocuments(this.key, documents);
                }

                Store.prototype.deleteDocument = function(title) {
                    var documents = this.getDocuments();
                    deleteDocumentByTitle(documents, title);
                    storeDocuments(this.key, documents);
                }

                Store.prototype.getDocument = function(title) {
                    var documents = this.getDocuments();
                    for (var i = documents.length - 1; i >= 0; i--) {
                        if (documents[i].title == title) {
                            return documents[i];
                        }
                    }
                    return null;
                }

                Store.prototype.getDocuments = function() {
                    var content = localStorage.getItem(this.key);
                    if (content == null) {
                        return [];
                    }
                    return $.parseJSON(content);
                }

                Store.prototype.getLastDocument = function() {
                    var documents = this.getDocuments();
                    documents.sort(function(a, b) {
                        var ta = a.time;
                        var tb = b.time;
                        return ta > tb ? -1 : ta == tb ? 0 : 1;
                    });
                    return documents.length > 0 ? documents[0] : null;
                }


                function deleteDocumentByTitle(documents, title) {
                    for (var i = documents.length - 1; i >= 0; i--) {
                        if (documents[i].title == title) {
                            documents.splice(i, 1);
                            break;
                        }
                    }
                }

                function storeDocuments(key, documents) {
                    var json = JSON.stringify(documents);
                    localStorage.setItem(key, json);
                }

                return {
                    create: function(key) {
                        return new Store(key);
                    }
                }
            })();
            var themeMode = (function() {
                var toolbarHandler = function(e) {
                    if ($(e.target).hasClass('fa-cog')) {
                        return;
                    }
                    colorPicker(theme.toolbar.color, function(color) {
                        theme.toolbar.color = color;
                        theme.render();
                        theme.store();
                    });
                }
                var statHandler = function() {
                    colorPicker(theme.stat.color, function(color) {
                        theme.stat.color = color;
                        theme.render();
                        theme.store();
                    });
                }
                var searchHelprHandler = function() {
                    colorPicker(theme.searchHelper.color, function(color) {
                        theme.searchHelper.color = color;
                        theme.render();
                        theme.store();
                    });
                }
                var barHandler = function() {
                    colorPicker(theme.bar.color, function(color) {
                        theme.bar.color = color;
                        theme.render();
                        theme.store();
                    });
                }
                var cloneBar;
                var setTheme = false;
                var changeThemeHandler = function() {
                    async function getTheme() {
                        setTheme = true;
                        const {
                            value: _theme
                        } = await Swal.fire({
                            input: 'select',
                            inputValue: theme.editor.theme || '',
                            inputOptions: {
                                '3024-day': '3024-day',
                                '3024-night': '3024-night',
                                'abcdef': 'abcdef',
                                'ambiance-mobile': 'ambiance-mobile',
                                'ambiance': 'ambiance',
                                'base16-dark': 'base16-dark',
                                'base16-light': 'base16-light',
                                'bespin': 'bespin',
                                'blackboard': 'blackboard',
                                'cobalt': 'cobalt',
                                'colorforth': 'colorforth',
                                'darcula': 'darcula',
                                'dracula': 'dracula',
                                'duotone-dark': 'duotone-dark',
                                'duotone-light': 'duotone-light',
                                'eclipse': 'eclipse',
                                'elegant': 'elegant',
                                'erlang-dark': 'erlang-dark',
                                'gruvbox-dark': 'gruvbox-dark',
                                'hopscotch': 'hopscotch',
                                'icecoder': 'icecoder',
                                'idea': 'idea',
                                'isotope': 'isotope',
                                'lesser-dark': 'lesser-dark',
                                'liquibyte': 'liquibyte',
                                'lucario': 'lucario',
                                'material': 'material',
                                'mbo': 'mbo',
                                'mdn-like': 'mdn-like',
                                'midnight': 'midnight',
                                'monokai': 'monokai',
                                'neat': 'neat',
                                'neo': 'neo',
                                'night': 'night',
                                'oceanic-next': 'oceanic-next',
                                'panda-syntax': 'panda-syntax',
                                'paraiso-dark': 'paraiso-dark',
                                'paraiso-light': 'paraiso-light',
                                'pastel-on-dark': 'pastel-on-dark',
                                'railscasts': 'railscasts',
                                'rubyblue': 'rubyblue',
                                'seti': 'seti',
                                'shadowfox': 'shadowfox',
                                'solarized': 'solarized',
                                'ssms': 'ssms',
                                'the-matrix': 'the-matrix',
                                'tomorrow-night-bright': 'tomorrow-night-bright',
                                'tomorrow-night-eighties': 'tomorrow-night-eighties',
                                'ttcn': 'ttcn',
                                'twilight': 'twilight',
                                'vibrant-ink': 'vibrant-ink',
                                'xq-dark': 'xq-dark',
                                'xq-light': 'xq-light',
                                'yeti': 'yeti',
                                'zenburn': 'zenburn'
                            },
                            inputPlaceholder: '选择主题',
                            showCancelButton: true
                        });
                        if (_theme) {
                            theme.editor.theme = _theme;
                            theme.loadEditorTheme(function(_theme) {
                                setTimeout(function() {
                                    editor.setOption("theme", _theme);
                                    var bgColor = window.getComputedStyle(document.body.querySelector('.CodeMirror'), null).getPropertyValue('background-color');
                                    theme.inCss.background = bgColor;
                                    theme.render();
                                    theme.store();
                                }, 100)
                            })
                        }
                        setTimeout(function() {
                            setTheme = false;
                        }, 1000)
                    }
                    if (!setTheme) {
                        getTheme();
                    }
                }
                var changeMemaidThemeHandler = function() {
                    async function getTheme() {
                        const {
                            value: _theme
                        } = await Swal.fire({
                            input: 'select',
                            inputValue: theme.mermaid.theme || '',
                            inputOptions: {
                                'default': 'default',
                                'forest': 'forest',
                                'dark': 'dark',
                                'neutral': 'neutral'
                            },
                            inputPlaceholder: '选择主题',
                            showCancelButton: true
                        });
                        if (_theme) {
                            theme.mermaid.theme = _theme;
                            theme.store();
                            wrapper.doRender(false);
                            wrapper.doSync();
                        }
                    }
                    getTheme();
                }


                var clonedTheme;
                var isThemeMode = false;

                function inThemeMode() {
                    isThemeMode = true;
                    clonedTheme = theme.clone();
                    $('<link>').appendTo('head').attr({
                        id: 'colorpicker-css',
                        type: 'text/css',
                        rel: 'stylesheet',
                        href: rootPath+'static/heather/colorpicker/dist/css/bootstrap-colorpicker.min.css'
                    });
                    $('<script>').appendTo('body').attr({
                        id: 'colorpicker-js',
                        src: rootPath+'static/heather/colorpicker/dist/js/bootstrap-colorpicker.min.js'
                    });
                    editor.setOption('readOnly', true);
                    $("#searchHelper input").attr('value', '点击设置字体颜色');
                    $("#searchHelper").children().addClass('noclick');
                    searchHelper.show();
                    $("#searchHelper").on('click', searchHelprHandler);
                    $("#toolbar").children().addClass('noclick');
                    $(configIcon).removeClass('noclick');
                    $("#toolbar").on('click', toolbarHandler);
                    $("#stat").text("点击设置字体颜色").show();
                    $("#stat").on('click', statHandler);
                    editor.on('cursorActivity', changeThemeHandler);
                    $("#out").on('click', '.mermaid', changeMemaidThemeHandler);
                    cloneBar = $("#innerBar").clone();
                    cloneBar.css({
                        'visibility': 'visible',
                        'top': '100px'
                    });
                    cloneBar.children().addClass('noclick');
                    $("#in").append(cloneBar);
                    cloneBar.on('click', barHandler);
                }

                function outThemeMode() {
                    isThemeMode = false;
                    cloneBar.off('click', barHandler);
                    cloneBar.remove();
                    $("#searchHelper").off('click', searchHelprHandler);
                    $("#searchHelper input").removeAttr('value');
                    searchHelper.hide();
                    editor.off('cursorActivity', changeThemeHandler);
                    $("#toolbar").off('click', toolbarHandler);
                    $("#stat").off('click', statHandler);
                    $("#out").off('click', '.mermaid', changeMemaidThemeHandler);
                    $("#stat").text("").hide();
                    $('.noclick').removeClass('noclick');
                    editor.setOption('readOnly', false);
                }


                var colorPicker = function(currentColor, callback) {
                    async function getColor() {
                        const {
                            value: color
                        } = await Swal.fire({
                            html: '<div id="colorpicker"></div>',
                            showCancelButton: true
                        });
                    }
                    getColor();
                    $('#colorpicker').colorpicker({
                        inline: true,
                        container: true,
                        template: '<div class="colorpicker">' +
                            '<div class="colorpicker-saturation"><i class="colorpicker-guide"></i></div>' +
                            '<div class="colorpicker-hue"><i class="colorpicker-guide"></i></div>' +
                            '<div class="colorpicker-alpha">' +
                            '   <div class="colorpicker-alpha-color"></div>' +
                            '   <i class="colorpicker-guide"></i>' +
                            '</div>' +
                            '</div>'
                    });
                    if (currentColor) {
                        $('#colorpicker').colorpicker('setValue', currentColor);
                    }
                    $('#colorpicker').on('colorpickerChange', function(event) {
                        if (event.color && callback) {
                            callback(event.color.toString());
                        }
                    });
                }

                return {
                    toggle: function() {
                        if (isThemeMode) {
                            outThemeMode();
                        } else {
                            inThemeMode();
                        }
                        return isThemeMode;
                    },
                    isThemeMode: function() {
                        return isThemeMode;
                    }
                }
            })();

            var searchHelper = (function() {
                var html = '';
                html += '<div id="searchHelper" style="position:absolute;bottom:0px;width:100%;z-index:99;display:none;padding:20px;padding-bottom:5px">';
                html += '<div style="width:100%;text-align:right;margin-bottom:5px"><i class="fas fa-times icon"  style="cursor:pointer;margin-right:0px"></i></div>';
                html += ' <form>';
                html += '<div class="input-group mb-3">';
                html += '<input type="text" style="outline: 0;border-width: 0 0 2px;" class="form-control" placeholder="查找内容" >';
                html += '<div class="input-group-append" data-search>';
                html += ' <span class="input-group-text" style="outline: 0;border-width: 0 0 2px;"><i class="fas fa-search " style="cursor:pointer"></i></span>';
                html += ' </div>';
                html += '</div>';
                html += '<div class="input-group mb-3" style="display:none">';
                html += '<input type="text" style="outline: 0;border-width: 0 0 2px;" class="form-control" placeholder="替换内容" >';
                html += '<div class="input-group-append" data-replace style="cursor:pointer">';
                html += ' <span class="input-group-text" style="outline: 0;border-width: 0 0 2px;"><i class="fas fa-exchange-alt" ></i></span>';
                html += ' </div>';
                html += '<div class="input-group-append" data-replace-all style="cursor:pointer">';
                html += ' <span class="input-group-text" style="outline: 0;border-width: 0 0 2px;"><i class="fas fa-sync-alt" ></i></span>';
                html += ' </div>';
                html += '<div class="input-group-append" data-up style="cursor:pointer">';
                html += ' <span class="input-group-text" style="outline: 0;border-width: 0 0 2px;"><i class="fas fa-arrow-up" ></i></span>';
                html += ' </div>';
                html += '<div class="input-group-append" data-down style="cursor:pointer">';
                html += ' <span class="input-group-text" style="outline: 0;border-width: 0 0 2px;"><i class="fas fa-arrow-down" ></i></span>';
                html += ' </div>';
                html += '</div>';
                html += '</form>';
                html += '  </div>';

                var ele = $(html);
                $("#in").append(ele);


                ele.on('click', '[data-search]', function() {
                    startSearch(function(cursor) {
                        if (cursor == null) {
                            swal('没有找到符合条件的搜索内容');
                        } else {
                            ele.find(".input-group").eq(0).hide();
                            ele.find(".input-group").eq(1).show();

                        }
                    });
                });

                ele.on('click', '.fa-times', function() {
                    clearSearch();
                    ele.hide();
                    ele.find('input').val('');
                    ele.find(".input-group").eq(0).show();
                    ele.find(".input-group").eq(1).hide();
                    editor.setOption('readOnly', false)
                });

                ele.on('click', '[data-down]', function() {
                    findNext(false);
                });

                ele.on('click', '[data-up]', function() {
                    findNext(true);
                });

                ele.on('click', '[data-replace]', function() {
                    var text = ele.find('input').eq(1).val();
                    var state = getSearchState();
                    if (!state.query) {
                        return;
                    }
                    var cursor = getSearchCursor(cm, state.query, cm.getCursor("from"));
                    var advance = function() {
                        var start = cursor.from(),
                            match;
                        if (!(match = cursor.findNext())) {
                            cursor = getSearchCursor(cm, state.query);
                            if (!(match = cursor.findNext()) || (start && cursor.from().line == start.line && cursor.from().ch == start.ch)) return;
                        }
                        cm.setSelection(cursor.from(), cursor.to());

                        var coords = cm.cursorCoords(cursor.from(), 'local');
                        cm.scrollTo(0, coords.top);
                        cursor.replace(typeof query == "string" ? text : text.replace(/\$(\d)/g,
                            function(_, i) {}));
                    };
                    advance();
                });

                ele.on('click', '[data-replace-all]', function() {
                    Swal.fire({
                        title: '确定要替换全部吗?',
                        type: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#3085d6',
                        cancelButtonColor: '#d33'
                    }).then((result) => {
                        if (result.value) {
                            var text = ele.find('input').eq(1).val();
                            var state = getSearchState();
                            if (!state.query) {
                                return;
                            }
                            query = state.query;
                            cm.operation(function() {
                                for (var cursor = getSearchCursor(cm, query); cursor.findNext();) {
                                    if (typeof query != "string") {
                                        var match = cm.getRange(cursor.from(), cursor.to()).match(query);
                                        cursor.replace(text.replace(/\$(\d)/g,
                                            function(_, i) {
                                                return match[i];
                                            }));
                                    } else cursor.replace(text);
                                }
                            });
                        }
                    })

                });


                function startSearch(callback) {
                    clearSearch();
                    var state = getSearchState();
                    var query = ele.find('input').eq(0).val();
                    if ($.trim(query) == '') {
                        swal('搜索内容不能为空');
                        return;
                    }
                    state.queryText = query;
                    state.query = parseQuery(query);
                    cm.removeOverlay(state.overlay, queryCaseInsensitive(state.query));
                    //state.overlay = searchOverlay(state.query, queryCaseInsensitive(state.query));
                    //cm.addOverlay(state.overlay);
                    findNext(false, callback);
                }

                function findNext(rev, callback) {
                    var state = getSearchState();
                    if (!state.query) {
                        return;
                    }

                    cm.operation(function() {
                        var cursor = getSearchCursor(cm, state.query, rev ? state.posFrom : state.posTo);
                        if (!cursor.find(rev)) {
                            cursor = getSearchCursor(cm, state.query, rev ? CodeMirror.Pos(cm.lastLine()) : CodeMirror.Pos(cm.firstLine(), 0));
                            if (!cursor.find(rev)) {
                                if (callback)
                                    callback(null);
                                return;
                            }
                        }
                        cm.setSelection(cursor.from(), cursor.to());

                        var ranges = editor.getAllMarks();
                        for (var i = 0; i < ranges.length; i++) ranges[i].clear();

                        editor.markText(cursor.from(), cursor.to(), {
                            className: "styled-background"
                        });

                        var coords = cm.cursorCoords(cursor.from(), 'local');
                        cm.scrollTo(0, coords.top);
                        state.posFrom = cursor.from();
                        state.posTo = cursor.to();
                        if (callback) callback({
                            from: cursor.from(),
                            to: cursor.to()
                        })
                    });
                }

                function clearSearch() {
                    var ranges = editor.getAllMarks();
                    for (var i = 0; i < ranges.length; i++) ranges[i].clear();
                    wrapper.editor.operation(function() {
                        var state = getSearchState();
                        state.lastQuery = state.query;
                        if (!state.query) return;
                        state.query = state.queryText = null;
                        wrapper.editor.removeOverlay(state.overlay);
                        if (state.annotate) {
                            state.annotate.clear();
                            state.annotate = null;
                        }
                    });
                }

                function getSearchState() {
                    return wrapper.editor.state.search || (wrapper.editor.state.search = new SearchState());
                }

                function queryCaseInsensitive(query) {
                    return typeof query == "string" && query == query.toLowerCase();
                }

                function getSearchCursor(cm, query, pos) {
                    return cm.getSearchCursor(query, pos, {
                        caseFold: queryCaseInsensitive(query),
                        multiline: true
                    });
                }

                function parseString(string) {
                    return string.replace(/\\(.)/g,
                        function(_, ch) {
                            if (ch == "n") return "\n"
                            if (ch == "r") return "\r"
                            return ch
                        })
                }

                function parseQuery(query) {
                    if (query == '') return query;
                    var isRE = query.match(/^\/(.*)\/([a-z]*)$/);
                    if (isRE) {
                        try {
                            query = new RegExp(isRE[1], isRE[2].indexOf("i") == -1 ? "" : "i");
                        } catch (e) {} // Not a regular expression after all, do a string search
                    } else {
                        query = parseString(query)
                    }
                    if (typeof query == "string" ? query == "" : query.test("")) query = /x^/;
                    return query;
                }

                function SearchState() {
                    this.posFrom = this.posTo = this.lastQuery = this.query = null;
                    this.overlay = null;
                }

                function searchOverlay(query, caseInsensitive) {
                    if (typeof query == "string") query = new RegExp(query.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"), caseInsensitive ? "gi" : "g");
                    else if (!query.global) query = new RegExp(query.source, query.ignoreCase ? "gi" : "g");

                    return {
                        token: function(stream) {
                            query.lastIndex = stream.pos;
                            var match = query.exec(stream.string);
                            if (match && match.index == stream.pos) {
                                stream.pos += match[0].length || 1;
                                return "searching";
                            } else if (match) {
                                stream.pos = match.index;
                            } else {
                                stream.skipToEnd();
                            }
                        }
                    };
                }

                return {
                    'show': function() {
                        ele.show()
                    },
                    'hide': function() {
                        ele.hide()
                    }
                };

            })();

            var configIcon;
            var icons = config.toolbar_icons || ['toc', 'innerBar', 'backup', 'new', 'search', 'config'];
            var store;
            for (var i = 0; i < icons.length; i++) {
                var icon = icons[i];
                if (icon == 'toc') {
                    wrapper.toolbar.addIcon('fas fa-book icon mobile-hide', function() {
                        toggleToc();
                    });
                }

                if (icon == 'innerBar') {
                    wrapper.innerBar.keepHidden = true;
                    wrapper.innerBar.hide();
                    wrapper.toolbar.addIcon('far icon fa-square', function(ele) {
                        toggleInnerbar(ele);
                    });
                }

                if (icon == 'new') {
                    wrapper.toolbar.addIcon('far fa-file icon', function(ele) {
                        newDocument();
                    });
                }

                if (icon == 'search') {
                    wrapper.toolbar.addIcon('fas fa-search icon', function() {
                        editor.setOption('readOnly', true)
                        searchHelper.show();
                    });
                }

                if (icon == 'backup') {
                    store = Store.create('markdown-documents');
                    loadLastDocument();
                    autoSave();
                    wrapper.toolbar.addIcon('fas icon fa-upload', function(ele) {
                        backup();
                    });
                    wrapper.toolbar.addIcon('fas icon fa-download', function(ele) {
                        selectDocuments();
                    });
                }

                if (icon == 'config') {
                    wrapper.toolbar.addIcon('fas icon fa-cog', function() {
                        swal({
                            html: '<input type="checkbox"  />主题编辑模式 <p style="margin-top:0.5rem"><button style="margin-bottom:0.5rem" class="alert alert-info">自定义css</button></p>'
                        });
                        var cb = $(Swal.getContent().querySelector('input'));
                        cb.prop('checked', themeMode.isThemeMode);
                        cb.change(function() {
                            var isThemeMode = themeMode.toggle();
                        });
                        $(Swal.getContent().querySelector('button')).click(function() {
                            writeCustomCss();
                        });
                    }, function(ele) {
                        configIcon = ele;
                    })
                }

            }

            var isToc = false;

            function toggleToc() {
                if (isToc) {
                    wrapper.toEditor(function() {
                        isToc = false
                    });
                } else {
                    wrapper.toToc(function() {
                        isToc = true
                    });
                }
            }

            var innerBar = wrapper.innerBar;

            function toggleInnerbar(ele) {
                innerBar.keepHidden = !innerBar.keepHidden;
                if (innerBar.keepHidden) {
                    innerBar.hide();
                    $(ele).addClass("fa-square").removeClass("fa-check-square");
                } else {
                    $(ele).addClass("fa-check-square").removeClass("fa-square");
                }
            }

            var docName;

            function backup() {
                if (!docName) {
                    async function save() {
                        const {
                            value: name
                        } = await Swal.fire({
                            title: '标题',
                            input: 'text',
                            showCancelButton: true
                        })
                        if (name) {
                            store.addDocument(name, wrapper.editor.getValue());
                            docName = name;
                            store.deleteDocument('default');
                            swal('保存成功');
                        }
                    }
                    save();
                } else {
                    store.addDocument(docName, wrapper.editor.getValue());
                    store.deleteDocument('default');
                    swal('保存成功');
                }
            }

            function writeCustomCss() {
                async function write() {
                    const {
                        value: css
                    } = await Swal.fire({
                        input: 'textarea',
                        inputPlaceholder: 'css',
                        inputValue: theme.customCss || '',
                        showCancelButton: true
                    })
                    if (css) {
                        theme.customCss = css;
                        theme.render();
                        theme.store();
                    }
                }
                write();
            }

            var autoSaveTimer;

            function autoSave() {
                wrapper.editor.on('change', function() {
                    if (autoSaveTimer) {
                        clearTimeout(autoSaveTimer);
                    }
                    autoSaveTimer = setTimeout(function() {
                        if (docName) {
                            store.addDocument(docName, wrapper.editor.getValue());
                        } else {
                            store.addDocument('default', wrapper.editor.getValue());
                        }
                    }, getDefault(config.toolbar_autoSaveMs,500));
                });
            }

            function loadLastDocument() {
                var doc = store.getLastDocument();
                if (doc != null) {
                    if (doc.title != 'default')
                        docName = doc.title;
                    wrapper.editor.setValue(doc.content);
					wrapper.editor.renderAllDoc(0);
                }
            }




            function selectDocuments() {
                var documents = store.getDocuments();
                for (var i = documents.length - 1; i >= 0; i--) {
                    if (documents[i].title == 'default') {
                        documents.splice(i, 1);
                        break;
                    }
                }
                if (documents.length == 0) {
                    swal('没有保存的文档');
                } else {
                    var html = '<table class="table" id="doc-table">';
                    for (var i = 0; i < documents.length; i++) {
                        var doc = documents[i];
                        html += '<tr><td>' + doc.title + '</td><td><i class="fas fa-times" data-title="' + doc.title + '" style="margin-right:20px;cursor:pointer"></i><i data-title="' + doc.title + '" class="fas fa-arrow-down" style=";cursor:pointer"></i></td><tr>';
                    }
                    html += '</table>';
                    swal({
                        html: html
                    });

                }
            }

            function newDocument() {
                Swal.fire({
                    title: '要打开一篇新文档吗?',
                    type: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33'
                }).then((result) => {
                    if (result.value) {
                        docName = undefined;
                        wrapper.editor.setValue('');
                    }
                })
            }

            $(document).on('click', '#doc-table .fa-times', function() {
                var title = $(this).data('title');
                Swal.fire({
                    title: '确定要删除吗?',
                    type: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33'
                }).then((result) => {
                    if (result.value) {
                        store.deleteDocument(title);
                        if (title == docName) {
                            var doc = store.getLastDocument();
                            docName = undefined;
                            if (doc == null) {
                                wrapper.editor.setValue('');
                            } else {
                                if (doc.title != 'default')
                                    docName = doc.title;
                                wrapper.editor.setValue(doc.content);
                            }
                        }
                        selectDocuments();
                    }
                })

            })

            $(document).on('click', '#doc-table .fa-arrow-down', function() {
                var title = $(this).data('title');
                Swal.fire({
                    title: '确定要加载吗?',
                    type: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33'
                }).then((result) => {
                    if (result.value) {
                        var doc = store.getDocument(title);
                        if (doc != null) {
                            docName = doc.title;
                            wrapper.editor.setValue(doc.content);
                        } else {
                            swal('要加载的文档不存在');
                        }
                    }
                })

            })
        }

        function renderToc() {
            var headings = document.getElementById('out').querySelectorAll("h1, h2, h3, h4, h5, h6");
            var toc = [];
            for (var i = 0; i < headings.length; i++) {
                var head = headings[i];
                var index = head.tagName.substring(1);
                var line = head.getAttribute('data-line');
                if (toc.length == 0) {
                    toc.push([{
                        indent: index,
                        name: head.textContent,
                        line: line
                    }]);
                } else {
                    var last = toc[toc.length - 1];
                    var first = last[0];
                    if (index > first.indent) {
                        last.push({
                            indent: index,
                            name: head.textContent,
                            line: line
                        });
                    } else {
                        toc.push([{
                            indent: index,
                            name: head.textContent,
                            line: line
                        }]);
                    }
                }
            }



            var html = '<h1>TOC</h1><hr>';
            if (toc.length > 0) {
                for (var i = 0; i < toc.length; i++) {
                    var block = toc[i];
                    for (var j = 0; j < block.length; j++) {
                        var item = block[j];
                        var indent = item.indent;
                        html += '<h' + indent + ' data-line="' + item.line + '">' + item.name + '</h' + indent + '>';
                    }
                }
            }
            try {
                var div = document.createElement("div");
                div.setAttribute('id', "toc");
                div.innerHTML = html;
                morphdom($("#toc")[0], div);
            } catch (e) {
                $("#toc").html(html)
            };
        }

        return {
            create: function(config) {
                return new EditorWrapper(config);
            }
        }
    })();



    return EditorWrapper;

})();