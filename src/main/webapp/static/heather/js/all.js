var EditorWrapper = (function() {
	
	'use strict';
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

    function getDefault(o, dft) {
        return isUndefined(o) ? dft : o;
    }

    var Render = (function() {

        function MarkdownRender(config, theme) {
            var plugins = config.render_plugins || ['footnote', 'katex', 'mermaid', 'anchor', 'task-lists', 'sup', 'sub', 'abbr'];
            var hasMermaid = $.inArray('mermaid', plugins) != -1;
			this.md = createMarkdownParser({
                html: config.render_allowHtml !== false,
                plugins: plugins,
                lineNumber: true,
				highlight : function(str, lang) {
					if(hasMermaid && lang == 'mermaid'){
						return '<div class="mermaid-block"><div class="mermaid">'+str+'</div><div class="mermaid-source" style="display:none">'+str+'</div></div>';					
					}
					if (lang && hljs.getLanguage(lang)) {
						try {
							return '<pre class="hljs"><code>' +
					   hljs.highlight(lang, str, true).value +
					   '</code></pre>';
						} catch (__) {}
					}
					return '';
				}
            });
            this.md2 = createMarkdownParser({
                html: config.render_allowHtml !== false,
                plugins: plugins,
                lineNumber: false,
				highlight : function(str, lang) {
					if(hasMermaid && lang == 'mermaid'){
						return '<div class="mermaid">'+str+'</div>';
					}
					if (lang && hljs.getLanguage(lang)) {
						return '<pre><code class="'+lang+'">'+str+'</code></pre>'
					}
					return '';
				}
            });
            this.config = config;
            this.theme = theme;
        }

        var mermaidLoading = false;

        function loadMermaid(theme, config) {
            if (mermaidLoading) return;
            mermaidLoading = true;
            $('<script>').appendTo('body').attr({
                src: config.res_mermaid_js || 'js/mermaid.min.js'
            });
            var t = setInterval(function() {
                try {
                    mermaid.initialize({
                        theme: theme.mermaid.theme || 'default'
                    });
                    clearInterval(t);
                    try{
						mermaid.init({}, '#editor_out .mermaid');
					}catch(e){
						console.log(e);
					}
                } catch (__) {}
            }, 20)
        }

        var katexLoading = false;

        function loadKatex(config) {
            if (katexLoading) return;
            katexLoading = true;
            $('<link>').appendTo('head').attr({
                type: 'text/css',
                rel: 'stylesheet',
                href: config.res_katex_css || 'katex/katex.min.css'
            });
            $('<script>').appendTo('body').attr({
                src: config.res_katex_js || 'katex/katex.min.js'
            });
            var t = setInterval(function() {
                try {
                    var html = katex.renderToString("", {
                        throwOnError: false
                    })
                    clearInterval(t);
                    var katexs = document.getElementById("editor_out").querySelectorAll(".katex");
                    for (var i = 0; i < katexs.length; i++) {
                        var block = katexs[i];
                        try{
							block.innerHTML = katex.renderToString(block.textContent, {
								throwOnError: false,
								displayMode: true
							});
						}catch(e){
							console.log(e);
						}
                    }
                } catch (__) {

                }
            }, 20)
        }

        MarkdownRender.prototype.getHtml = function(markdownText) {
            return this.md2.render(markdownText);
        }

        MarkdownRender.prototype.renderAt = function(markdownText, element, patch) {
            var doc = $.parseHTML2(this.md.render(markdownText));
            var hasMermaid = doc.querySelector('.mermaid') != null;
            if (hasMermaid) {
                loadMermaid(this.theme, this.config);
            }
            var hasKatex = doc.querySelector(".katex") != null;
            if (hasKatex) {
                loadKatex(this.config)
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
                    theme: this.theme.mermaid.theme || 'default'
                });
                mermaid.init({}, '#editor_out .mermaid');
            } catch (e) {if(!isUndefined(window.mermaid)) console.log(e)}
        }

        return {
            create: function(config, theme) {
                return new MarkdownRender(config, theme)
            }
        }
    })();


    var Bar = (function() {
        

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


    var EditorWrapper = (function() {

        var mobile = CodeMirror.browser.mobile;
        var ios = CodeMirror.browser.ios;
		
        var Theme = (function() {
            function Theme(config) {
                this.toolbar = {};
                this.bar = {};
                this.stat = {};
                this.editor = {};
                this.inCss = {};
                this.searchHelper = {};
                this.mermaid = {};
                this.hljs = {
                    theme: 'github'
                };
                this.customCss = undefined;
                this.timer = undefined;
                this.config = config;
            }

            var themeKey = "markdown-editor-theme";

            function saveTheme(theme, cb) {
                var json = JSON.stringify(theme);
                try {
                    localStorage.setItem(themeKey, json);
                    if (cb) cb("success");
                } catch (e) {
                    if (cb) cb("fail");
                }
            }

            function getTheme(cb) {
                if (!cb) return;
                var json = localStorage.getItem(themeKey);
                if (json == null) {
                    cb(null)
                } else {
                    cb($.parseJSON(json));
                }

            }

            function delTheme(cb) {
                try {
                    localStorage.removeItem(themeKey);
                    if (cb) cb("success");
                } catch (e) {
                    if (cb) cb("fail");
                }
            }

            Theme.prototype.store = function(callback) {
                if (this.timer) {
                    clearTimeout(this.timer);
                }
                var theme = this;
                this.timer = setTimeout(function() {
                    saveTheme(theme, function(cb) {
                        if (callback) callback(cb == "success" ? true : false);
                    });
                }, 500)
            }

            Theme.prototype.reset = function() {
                var theme = new Theme();
                saveTheme(theme);
                theme.render();
                return theme;
            }

            Theme.prototype.clone = function() {
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

            var getCurrentTheme = function(config) {
                var current;
                getTheme(function(cb) {
                    current = cb
                });
                if (!current || current == null) {
                    return new Theme(config);
                }
                var theme = new Theme(config);
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
			
            function loadHljsTheme(theme) {
                if (theme.hljs.theme) {
                    var hljsTheme = theme.hljs.theme;
                    var hljsThemeFunction = theme.config.res_hljsTheme || function(hljsTheme) {
                        return 'highlight/styles/' + hljsTheme + '.css';
                    }
                    if ($('hljs-theme-' + hljsTheme + '').length == 0) {
                        $('<link id="hljs-theme-' + hljsTheme + '" >').appendTo('head').attr({
                            type: 'text/css',
                            rel: 'stylesheet',
                            href: hljsThemeFunction(hljsTheme)
                        })
                    }
                }
            }

            Theme.prototype.render = function() {
				loadEditorTheme(this);
                loadHljsTheme(this);
                var css = "";
                css += "#editor_toolbar{color:" + (this.toolbar.color || 'inherit') + "}\n";
                css += "#editor_innerBar{color:" + (this.bar.color || 'inherit') + "}\n"
                css += "#editor_stat{color:" + (this.stat.color || 'inherit') + "}\n";
                css += "#editor_in{background:" + (this.inCss.background || 'inherit') + "}\n";
				var searchHelperColor = (this.searchHelper.color || 'inherit');
                css += "#editor_searchHelper{color:" + searchHelperColor + "}\n#editor_searchHelper .form-control{color:" + searchHelperColor + "}\n#editor_searchHelper .input-group-text{color:" + searchHelperColor + "}\n#editor_searchHelper .form-control::placeholder {color: "+searchHelperColor+";opacity: 1;}\n#editor_searchHelper .form-control::-ms-input-placeholder {color: "+searchHelperColor+";}\n#editor_searchHelper .form-control::-ms-input-placeholder {color: "+searchHelperColor+";}";                             

			   $("#custom_theme").remove();
                if ($.trim(css) != '') {
                    $("head").append("<style type='text/css' id='custom_theme'>" + css + "</style>");
                }
                $("#custom_css").remove();
                $("head").append("<style type='text/css' id='custom_css'>" + (this.customCss || '') + "</style>");
            }
			
            return {
                current: function(config) {
                    return getCurrentTheme(config)
                }
            };
        })();
		
		 function loadEditorTheme(theme,callback) {
			if (theme.editor.theme) {
				var editorTheme = theme.editor.theme;
				var editorThemeFunction = theme.config.res_editorTheme || function(editorTheme) {
					return 'codemirror/theme/' + editorTheme + '.css';
				}
				if ($('#codemirror-theme-' + editorTheme + '').length == 0) {
					$('<link id="codemirror-theme-' + editorTheme + '" >').appendTo('head').attr({
						type: 'text/css',
						rel: 'stylesheet',
						onload:function(){
							if(callback){callback(editorTheme)}
						},
						href: editorThemeFunction(editorTheme)
					})
				} else {
					if(callback){callback(editorTheme)}
				}
			}
		}


        function EditorWrapper(config) {
			var html = '<div id="editor_wrapper">';
			html += '<div id="editor_toc">';
			html += '</div>' ;
			html += '<div id="editor_in">' ;
			html += '<div id="editor_toolbar"></div>' ;
			html += '<textarea  style="width: 100%; height: 100%"></textarea>' ;
			html += '<div id="editor_stat"></div>' ;
			html += '<div id="editor_innerBar"></div>' ;
			html += '</div>' ;
			html += '<div class="markdown-body" id="editor_out"></div>';	
			html += '</div>' ;
			var $wrapperElement = $(html);
			$('body').append($wrapperElement);
			$('body').addClass('editor_noscroll');
			$('html').addClass('editor_noscroll');
			this.wrapperElement = $wrapperElement[0]
            if (!mobile) {
                $("#editor_in").show();
                $("#editor_out").css({
                    'visibility': 'visible'
                });
            }
            $("#editor_wrapper").animate({
                scrollLeft: $("#editor_toc").outerWidth()
            }, 0);

			this.eventHandlers = [];
            var theme = Theme.current(config);
            theme.render();
            var scrollBarStyle = mobile ? 'native' : 'overlay';
            var editor = CodeMirror.fromTextArea(document.getElementById('editor_wrapper').querySelector('textarea'), {
                mode: {name: "gfm"},
                lineNumbers: false,
                matchBrackets: true,
                lineWrapping: true,
                dragDrop: true,
                scrollbarStyle: scrollBarStyle,
                theme: theme.editor.theme || 'default',
                styleSelectedText: true,
                extraKeys: {
                    "Enter": "newlineAndIndentContinueMarkdownList"
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
			var customChangeCssHandler = function() {
                theme.customCss = this.textContent;
                theme.store();
            };
            $('head').on('DOMSubtreeModified', '#custom_css', customChangeCssHandler);
			this.eventHandlers.push({
				name : 'remove',
				handler : function(){
					$('head').off('DOMSubtreeModified', '#custom_css', customChangeCssHandler)
				}
			})
            this.theme = theme;
            this.sync = Sync.create(editor, $("#editor_out")[0], config);
            this.render = Render.create(config, theme);
            this.toolbar = Bar.create($("#editor_toolbar")[0]);
            var innerBar = Bar.create($("#editor_innerBar")[0]);
            innerBar.hide();
            this.innerBar = innerBar;
            //sync 
            var wrapper = this;
            if (!mobile) {
                //auto render
                var ms = getDefault(config.render_ms, 500);
                var autoRenderTimer;
                var stat_timer;
				wrapper.eventHandlers.push({name:'remove',handler:function(){
					if (autoRenderTimer) {
                        clearTimeout(autoRenderTimer);
                    }
					if (stat_timer) {
                        clearTimeout(stat_timer);
                    }
				}})
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
                            return "当前字数：" + wrapper.getValue().length
                        }
                        $("#editor_stat").html(formatter(wrapper)).show();
                        if (stat_timer) {
                            clearTimeout(stat_timer);
                        }
                        stat_timer = setTimeout(function() {
                            $("#editor_stat").hide();
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
                }
                this.syncEnable = syncEnable;
                this.scrollHandler = scrollHandler;
            }

            if (mobile) {
                //swipe
                $("#editor_toc").touchwipe({
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
                    wipeRight: function(e) {
                        wrapper.toToc();
                    },
                    min_move_x: 10,
                    max_move_y: 5
                });
				
				function hasXScrollBar(element){
				  var overflowX = window.getComputedStyle(element)['overflow-x'];
				  return (overflowX === 'scroll' || overflowX === 'auto') && element.scrollWidth > element.clientWidth;
				}
				
				//if an element has a x scrollbar and scrollLeft > 0 then can not wipe
				function canWipe(element){
					if(isUndefined(element) || element == null){
						return true;
					}
					if(hasXScrollBar(element) && $(element).scrollLeft() > 0){
						return false;
					}
					return canWipe(element.parentElement);
				}
				
				
                $("#editor_out").touchwipe({
                    wipeRight: function(e) {
						if(canWipe(e.target)){
							wrapper.toEditor()
						}
                    },
                    min_move_x: 10,
                    max_move_y: 5
                });
            }
			
			var tocClickTimer;
			wrapper.eventHandlers.push({name:'remove',handler:function(){
				if (tocClickTimer) {
					clearTimeout(tocClickTimer);
				}
			}});
			
            $("#editor_toc").on('click', '[data-line]', function() {
                var line = parseInt($(this).data('line'));

                editor.scrollIntoView({
                    line: line
                });
                tocClickTimer = setTimeout(function() {
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
            this.config = config;
            initInnerBar(this);
            initToolbar(this);
			this.fullscreen = false;
            if (screenfull.enabled) {
				var screenFullChangeHandler = function() {
					wrapper.fullscreen = screenfull.isFullscreen;
                    changeStyleWhenFullScreenChange(wrapper, screenfull.isFullscreen);
					if(!screenfull.isFullscreen){
						wrapper.toEditor(undefined,0);
						wrapper.doSync();
					}
                }
				wrapper.eventHandlers.push({name:'remove',handler:function(){
					 screenfull.off('change',screenFullChangeHandler);
				}})
                screenfull.on('change',screenFullChangeHandler);
            }
			triggerEvent(this,'load');
        }
		
		function triggerEvent(wrapper,name,args){
			for(var i=0;i<wrapper.eventHandlers.length;i++){
				var evtHandler= wrapper.eventHandlers[i];
				if(evtHandler.name == name){
					try{
						evtHandler.handler.call(this,args);
					}catch(e){}
				}
			}
		}

        function changeStyleWhenFullScreenChange(wrapper, isFullscreen) {
            if (!CodeMirror.browser.mobile) {
                var cm = wrapper.editor;
                var wrap = cm.getWrapperElement();
                if (isFullscreen) {
                    $(wrapper.getFullScreenElement()).addClass('editor_fullscreen');
                    //from CodeMirror display fullscreen.js
                    cm.state.fullScreenRestore = {
                        scrollTop: window.pageYOffset,
                        scrollLeft: window.pageXOffset,
                        width: wrap.style.width,
                        height: wrap.style.height
                    };
                    wrap.style.width = "";
                    wrap.style.height = "auto";
                    wrap.className += " CodeMirror-fullscreen";
                    document.documentElement.style.overflow = "hidden";
                    cm.refresh();
                } else {
                    $(wrapper.getFullScreenElement()).removeClass('editor_fullscreen');
                    wrap.className = wrap.className.replace(/\s*CodeMirror-fullscreen\b/, "");
                    document.documentElement.style.overflow = "";
                    var info = cm.state.fullScreenRestore;
                    wrap.style.width = info.width;
                    wrap.style.height = info.height;
                    window.scrollTo(info.scrollLeft, info.scrollTop);
                    cm.refresh();
                }
            }
        }

        EditorWrapper.prototype.doRender = function(patch) {
            this.render.renderAt(this.editor.getValue(), $("#editor_out")[0], patch);
            renderToc();
        }
		
		EditorWrapper.prototype.remove = function(patch) {
			triggerEvent(this,'remove');
            $(this.getFullScreenElement()).removeClass('fullscreen');
			$('body').removeClass('editor_noscroll');
			$('html').removeClass('editor_noscroll');
            this.wrapperElement.parentNode.removeChild(this.wrapperElement);
			wrapperInstance.wrapper = undefined;
			delete wrapperInstance.wrapper;
        }
		
        EditorWrapper.prototype.doSync = function() {
            this.sync.doSync();
        }
		
		EditorWrapper.prototype.requestFullScreen = function() {
            if(!mobile){
				 if (screenfull.enabled) {
					screenfull.request(this.getFullScreenElement());
				} else {
					swal("当前浏览器不支持全屏模式")
				}
			}
        }
		
		EditorWrapper.prototype.getFullScreenElement = function() {
            return document.body;
        }
		
		EditorWrapper.prototype.exitFullScreen  = function() {
			if(!mobile && screenfull.enabled){
				screenfull.exit();
			}
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
		
		EditorWrapper.prototype.getValue = function() {
            return this.editor.getValue();
        }
		
		EditorWrapper.prototype.setValue = function(text) {
            return this.editor.setValue(text);
        }

        EditorWrapper.prototype.disableSync = function() {
            if (this.syncEnable) {
                editor.off('scroll', this.scrollHandler);
                this.syncEnable = false;
            }
        }

        EditorWrapper.prototype.toEditor = function(callback,_ms) {
            var ms = getDefault(_ms,getDefault(this.config.swipe_animateMs, 500));
            if (mobile) {
                $("#editor_wrapper").animate({
                    scrollLeft: $("#editor_in").width()
                }, ms, function() {
                    if (callback) callback();
                });
            } else {
                $("#editor_wrapper").animate({
                    scrollLeft: $("#editor_in").offset().left
                }, ms, function() {
                    if (callback) callback();
                });
            }
        }


        EditorWrapper.prototype.toToc = function(callback,_ms) {
            this.editor.getInputField().blur();
            if (mobile) {
                this.doRender(true);
            }
            var ms = getDefault(_ms,getDefault(this.config.swipe_animateMs, 500));
            $("#editor_wrapper").animate({
                scrollLeft: 0
            }, ms, function() {
                if (callback) callback();
            });
        }

        EditorWrapper.prototype.toPreview = function(callback,_ms) {
            if (mobile) {
                this.editor.getInputField().blur();
                this.doRender(true);
                this.doSync();
				var ms = getDefault(_ms,getDefault(this.config.swipe_animateMs, 500));
                $("#editor_wrapper").animate({
                    scrollLeft: $("#editor_out")[0].offsetLeft
                }, ms, function() {
                    if (callback) callback();
                });
            }
        }



        function initInnerBar(wrapper) {
            var innerBar = wrapper.innerBar;
            var editor = wrapper.editor;
            var config = wrapper.config;

            var innerBarElement = $("#editor_innerBar");
            var icons = config.innerBar_icons || ['emoji', 'heading', 'bold', 'italic', 'quote', 'strikethrough', 'link', 'code', 'code-block', 'uncheck', 'check', 'table', 'undo', 'redo', 'close'];
            var ios = CodeMirror.browser.ios;
            for (var i = 0; i < icons.length; i++) {
                var icon = icons[i];
                if (icon == 'emoji') {
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

            function addEmoji() {
                innerBar.addIcon('far fa-grin-alt icon', function() {
                    var emojiArray = config.emojiArray || $.trim("😀 😁 😂 🤣 😃 😄 😅 😆 😉 😊 😋 😎 😍 😘 😗 😙 😚 ☺️ 🙂 🤗 🤔 😐 😑 😶 🙄 😏 😣 😥 😮 🤐 😯 😪 😫 😴 😌 😛 😜 😝 🤤 😒 😓 😔 😕 🙃 🤑 😲 ☹️ 🙁 😖 😞 😟 😤 😢 😭 😦 😧 😨 😩 😬 😰 😱 😳 😵 😡 😠 😷 🤒 🤕 🤢 🤧 😇 🤠 🤡 🤥 🤓 😈 👿 👹 👺 💀 👻 👽 🤖 💩 😺 😸 😹 😻 😼 😽 🙀 😿 😾").split(' ');
                    var html = '';
                    for (var i = 0; i < emojiArray.length; i++) {
                        html += '<span data-emoji style="cursor:pointer">' + emojiArray[i] +
                            '</span>';
                    }
                    swal({
                        html: html
                    })
                    $(Swal.getContent()).find('[data-emoji]').click(function() {
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
                        html: '<input class="swal2-input" placeholder="行">' +
                            '<input class="swal2-input" placeholder="列">',
                        preConfirm: function() {
                            return new Promise(function(resolve) {
								var inputs = $(Swal.getContent()).find('input');
                                resolve([
                                    inputs.eq(0).val(),  
									inputs.eq(1).val()
                                ])
                            })
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

			wrapper.eventHandlers.push({name:'remove',handler:function(){
				if(keyboardTimer){
					clearInterval(keyboardTimer);
				}
			}})
            var mobileCursorActivityHandler = function(bar) {
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
                            bar.height() - $("#editor_toolbar").height();
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

                        showBar();
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
                            var wasWithKeyboard = $(wrapper.wrapperElement).hasClass('view-withKeyboard');
                            var nowWithKeyboard = false;
                            var diff = Math.abs(originalPotion - ($(window).width() + $(window).height()));
                            if (diff > 100) nowWithKeyboard = true;
                            $(wrapper.wrapperElement).toggleClass('view-withKeyboard', nowWithKeyboard);
                            if (wasWithKeyboard != nowWithKeyboard) {
                                onKeyboardOnOff(nowWithKeyboard);
                            }
                        }
                    }
					
					wrapper.eventHandlers.push({name:'remove',handler:function(){
						 $(window).off('resize orientationchange', applyAfterResize);
					}})

                    $(window).on('resize orientationchange', applyAfterResize);

                    return {
                        isOpen: keyboardOpen
                    }
                })();
				
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
            ////////////////////backup 
			var Backup = (function(){
				
				function Backup(key){
					this.key = key;
					var me = this;
					wrapper.editor.on('change', function() {
						if (me.autoSaveTimer) {
							clearTimeout(me.autoSaveTimer);
						}
						me.autoSaveTimer = setTimeout(function() {
							if (me.docName) {
								me.addDocument(me.docName, wrapper.getValue());
							} else {
								me.addDocument('default', wrapper.getValue());
							}
						}, getDefault(config.toolbar_autoSaveMs, 500));
					});
					wrapper.eventHandlers.push({name:'remove',handler:function(){
						if (me.autoSaveTimer) {
							clearTimeout(me.autoSaveTimer);
						}
					}});
					wrapper.eventHandlers.push({name:'load',handler:function(){
						setTimeout(function(){
							me.loadLastDocument();
						},100)
					}});
				}
				
				Backup.prototype.addDocument = function(title, content) {
                    var documents = this.getDocuments();
                    deleteDocumentByTitle(documents, title);
                    documents.push({
                        title: title,
                        content: content,
                        time: $.now()
                    });
                    storeDocuments(this.key, documents);
                }
				
				Backup.prototype.deleteDocument = function(title) {
					var doc = this.getDocument(title);
					if(doc != null && this.docName == doc.title){
						this.newDocument();
					}
                    var documents = this.getDocuments();
                    deleteDocumentByTitle(documents, title);
                    storeDocuments(this.key, documents);
                }

                Backup.prototype.getDocument = function(title) {
                    var documents = this.getDocuments();
                    for (var i = documents.length - 1; i >= 0; i--) {
                        if (documents[i].title == title) {
                            return documents[i];
                        }
                    }
                    return null;
                }

                Backup.prototype.getDocuments = function() {
                    var content = localStorage.getItem(this.key);
                    if (content == null) {
                        return [];
                    }
                    return $.parseJSON(content);
                }

                Backup.prototype.getLastDocument = function() {
                    var documents = this.getDocuments();
                    documents.sort(function(a, b) {
                        var ta = a.time;
                        var tb = b.time;
                        return ta > tb ? -1 : ta == tb ? 0 : 1;
                    });
                    return documents.length > 0 ? documents[0] : null;
                }
				
				Backup.prototype.loadLastDocument = function() {
					var doc = this.getLastDocument();
					loadDocument(this,doc);
                }
				
				Backup.prototype.loadDocument = function(title) {
					var doc = this.getDocument(title);
					loadDocument(this,doc);
                }
				
				function loadDocument(backup,doc){
					if (doc != null) {
						if (doc.title != 'default')
							backup.docName = doc.title;
						else
							backup.docName = undefined;
						wrapper.setValue(doc.content);
						//代码高亮的同步预览中，由于codemirror只渲染当前视窗，因此会出现不同步的现象
						//可以调用CodeMirror.renderAllDoc，但是这个方法在大文本中速度很慢，可以选择关闭
						if(wrapper.syncEnable !== false && config.renderAllDocEnable !== false){
							wrapper.editor.renderAllDoc(0);
						}
					}
				}
				
				Backup.prototype.newDocument = function() {
					this.docName = undefined;
					wrapper.setValue("");
                }
				
				Backup.prototype.backup = function() {
					if(this.docName){
						this.addDocument(this.docName,wrapper.getValue());
						this.deleteDocument('default');
						swal('保存成功');
					} else {
						var me = this;
						async function requestName() {
							const {
								value: name
							} = await Swal.fire({
								title: '标题',
								input: 'text',
								showCancelButton: true
							})
							if (name) {
								me.addDocument(name, wrapper.getValue());
								me.docName = name;
								me.deleteDocument('default');
								swal('保存成功');
							}
						}
						requestName();
					}
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
				
				return {create : function(key){
					return new Backup(key);
				}}
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
							theme.render();
                            loadEditorTheme(theme,function(_theme) {
                                setTimeout(function() {
                                    editor.setOption("theme", _theme);
                                    var bgColor = window.getComputedStyle(editor.getWrapperElement(), null).getPropertyValue('background-color');
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
                        href: config.res_colorpickerCss || 'colorpicker/dist/css/bootstrap-colorpicker.min.css'
                    });
                    $('<script>').appendTo('body').attr({
                        id: 'colorpicker-js',
                        src: config.res_colorpickerJs || 'colorpicker/dist/js/bootstrap-colorpicker.min.js'
                    });
                    editor.setOption('readOnly', true);
                    $("#editor_searchHelper input").attr('value', '点击设置字体颜色');
                    $("#editor_searchHelper").children().addClass('noclick');
                    searchHelper.show();
                    $("#editor_searchHelper").on('click', searchHelprHandler);
                    $("#editor_toolbar").children().addClass('noclick');
                    $(configIcon).removeClass('noclick');
                    $("#editor_toolbar").on('click', toolbarHandler);
                    $("#editor_stat").text("点击设置字体颜色").show();
                    $("#editor_stat").on('click', statHandler);
                    editor.on('cursorActivity', changeThemeHandler);
                    $("#editor_out").on('click', '.mermaid', changeMemaidThemeHandler);
                    cloneBar = $("#editor_innerBar").clone();
                    cloneBar.css({
                        'visibility': 'visible',
                        'top': '100px'
                    });
                    cloneBar.children().addClass('noclick');
                    $("#editor_in").append(cloneBar);
                    cloneBar.on('click', barHandler);
                }

                function outThemeMode() {
                    isThemeMode = false;
                    cloneBar.off('click', barHandler);
                    cloneBar.remove();
                    $("#editor_searchHelper").off('click', searchHelprHandler);
                    $("#editor_searchHelper input").removeAttr('value');
                    searchHelper.hide();
                    editor.off('cursorActivity', changeThemeHandler);
                    $("#editor_toolbar").off('click', toolbarHandler);
                    $("#editor_stat").off('click', statHandler);
                    $("#editor_out").off('click', '.mermaid', changeMemaidThemeHandler);
                    $("#editor_stat").text("").hide();
                    $('.noclick').removeClass('noclick');
                    editor.setOption('readOnly', false);
                }


                var colorPicker = function(currentColor, callback) {
                    async function getColor() {
                        const {
                            value: color
                        } = await Swal.fire({
                            html: '<div class="_colorpicker"></div>',
                            showCancelButton: true
                        });
                    }
                    getColor();
					var colorpickerElement = $(Swal.getContent()).find('._colorpicker');
                    colorpickerElement.colorpicker({
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
                        colorpickerElement.colorpicker('setValue', currentColor);
                    }
                    colorpickerElement.on('colorpickerChange', function(event) {
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
                html += '<div id="editor_searchHelper" style="position:absolute;bottom:10px;width:100%;z-index:99;display:none;padding:20px;padding-bottom:5px">';
                html += '<div style="width:100%;text-align:right;margin-bottom:5px"><i class="fas fa-times icon"  style="cursor:pointer;margin-right:0px"></i></div>';
                html += ' <form>';
                html += '<div class="input-group mb-3">';
                html += '<input type="text" style="border:none" class="form-control" placeholder="查找内容" >';
                html += '<div class="input-group-append" data-search>';
                html += ' <span class="input-group-text" ><i class="fas fa-search " style="cursor:pointer"></i></span>';
                html += ' </div>';
                html += '</div>';
                html += '<div class="input-group mb-3" style="display:none">';
                html += '<input type="text" style="border:none" class="form-control" placeholder="替换内容" >';
                html += '<div class="input-group-append" data-replace style="cursor:pointer">';
                html += ' <span class="input-group-text" ><i class="fas fa-exchange-alt" ></i></span>';
                html += ' </div>';
                html += '<div class="input-group-append" data-replace-all style="cursor:pointer">';
                html += ' <span class="input-group-text" ><i class="fas fa-sync-alt" ></i></span>';
                html += ' </div>';
                html += '<div class="input-group-append" data-up style="cursor:pointer">';
                html += ' <span class="input-group-text" ><i class="fas fa-arrow-up" ></i></span>';
                html += ' </div>';
                html += '<div class="input-group-append" data-down style="cursor:pointer">';
                html += ' <span class="input-group-text" ><i class="fas fa-arrow-down" ></i></span>';
                html += ' </div>';
                html += '</div>';
                html += '</form>';
                html += '  </div>';

                var ele = $(html);
                $("#editor_in").append(ele);


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
					return string.replace(/\\([nrt\\])/g, function(match, ch) {
					  if (ch == "n") return "\n"
					  if (ch == "r") return "\r"
					  if (ch == "t") return "\t"
					  if (ch == "\\") return "\\"
					  return match
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
            var icons = config.toolbar_icons || ['toc', 'innerBar', 'backup', 'search', 'config', 'expand'];
            for (var i = 0; i < icons.length; i++) {
                var icon = icons[i];
                if (icon == 'toc') {
                    wrapper.toolbar.addIcon('fas fa-book icon mobile-hide nofullscreen', function() {
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

                if (icon == 'search') {
                    wrapper.toolbar.addIcon('fas fa-search icon', function() {
                        editor.setOption('readOnly', true)
                        searchHelper.show();
                    });
                }

                if (icon == 'backup') {
                    var backup = Backup.create('markdown-documents');
                    wrapper.toolbar.addIcon('fas icon fa-upload ', function(ele) {
                        backup.backup();
                    });
                    wrapper.toolbar.addIcon('fas icon fa-download ', function(ele) {
                        selectDocuments(backup);
                    });
					wrapper.toolbar.addIcon('far fa-file icon nofullscreen', function(ele) {
                        newDocument(backup);
                    });
					wrapper.backup = backup;
                }

                if (icon == 'config') {
                    wrapper.toolbar.addIcon('fas icon fa-cog nofullscreen', function() {
                        
						swal({
                            html: '<input type="checkbox"  />主题编辑模式 <p style="margin-top:0.5rem"><button style="margin-bottom:0.5rem;border: 0;border-radius: .25em;background: initial;background-color: #3085d6;color: #fff;font-size: 1.0625em;margin: .3125em;padding: .625em 2em;font-weight: 500;box-shadow: none;">自定义css</button></p>'
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


                if (icon == 'expand') {
                    wrapper.toolbar.addIcon('fas fa-expand icon mobile-hide', function(ele) {
						if ($(ele).hasClass('fa-expand')) {
							wrapper.requestFullScreen();
						} else {
							wrapper.exitFullScreen();
						}
                    }, function(ele) {
                        if (screenfull.enabled) {
							
							var toggleHandler = function() {
                                if (screenfull.isFullscreen) {
                                    $(ele).removeClass('fa-expand').addClass('fa-compress');
                                } else {
                                    $(ele).removeClass('fa-compress').addClass('fa-expand');
                                }
                            };
							wrapper.eventHandlers.push({name:'remove',handler:function(){
								 screenfull.off('change',toggleHandler);
							}})
                            screenfull.on('change', toggleHandler);
                        }
                    });
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
			
			function selectDocuments(backup) {
				var documents = backup.getDocuments();
				for (var i = documents.length - 1; i >= 0; i--) {
					if (documents[i].title == 'default') {
						documents.splice(i, 1);
						break;
					}
				}
				if (documents.length == 0) {
					swal('没有保存的文档');
				} else {
					var html = '<table class="table">';
					for (var i = 0; i < documents.length; i++) {
						var doc = documents[i];
						html += '<tr><td>' + doc.title + '</td><td><i class="fas fa-times" data-title="' + doc.title + '" style="margin-right:20px;cursor:pointer"></i><i data-title="' + doc.title + '" class="fas fa-arrow-down" style=";cursor:pointer"></i></td><tr>';
					}
					html += '</table>';
					swal({
						html: html
					});
					$(Swal.getContent()).find('.fa-times').click(function() {
						var title = $(this).data('title');
						Swal.fire({
							title: '确定要删除吗?',
							type: 'warning',
							showCancelButton: true,
							confirmButtonColor: '#3085d6',
							cancelButtonColor: '#d33'
						}).then((result) => {
							if (result.value) {
								backup.deleteDocument(title);
								selectDocuments(backup);
							}
						})
					})
					
					$(Swal.getContent()).find('.fa-arrow-down').click(function() {
						var title = $(this).data('title');
						Swal.fire({
							title: '确定要加载吗?',
							type: 'warning',
							showCancelButton: true,
							confirmButtonColor: '#3085d6',
							cancelButtonColor: '#d33'
						}).then((result) => {
							if (result.value) {
								backup.loadDocument(title);
							}
						})
					})
				}
			}

			function newDocument(backup) {
				Swal.fire({
					title: '要打开一篇新文档吗?',
					type: 'warning',
					showCancelButton: true,
					confirmButtonColor: '#3085d6',
					cancelButtonColor: '#d33'
				}).then((result) => {
					if (result.value) {
						backup.newDocument();
					}
				})
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

        }

        function renderToc() {
            var headings = document.getElementById('editor_out').querySelectorAll("h1, h2, h3, h4, h5, h6");
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
                div.setAttribute('id', "editor_toc");
                div.innerHTML = html;
                morphdom($("#editor_toc")[0], div);
            } catch (e) {
                $("#editor_toc").html(html)
            };
        }
		
		var wrapperInstance = {};
        return {
            create: function(config) {
				if(wrapperInstance.wrapper){
					wrapperInstance.wrapper.remove();
				}
				wrapperInstance.wrapper = new EditorWrapper(config)
                return wrapperInstance.wrapper;
            }
        }
    })();



    return EditorWrapper;

})();