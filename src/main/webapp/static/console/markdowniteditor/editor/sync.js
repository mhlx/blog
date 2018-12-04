var sync = (function(editor) {
  			var scrollMap;
            var oldVForScroll;
            var building = false;
            var t;
            var syncResultScrollAtLine = function(lineNoFun) {
              	if(!config.syncScroll){
                	return ;
                }
                if (!scrollMap) {

                    buildScrollMap(function() {
                        posTo = scrollMap[lineNoFun()];
                        $('#out').stop(true).animate({
                            scrollTop: posTo
                        }, 100);

                    });
                } else {
                    posTo = scrollMap[lineNoFun()];
                    $('#out').stop(true).animate({
                        scrollTop: posTo
                    }, 50);
                }
            }
            var syncResultScroll = function() {
              	if(!config.syncScroll){
                	return ;
                }
                var lineNo = function() {
                    return editor.lineAtHeight(editor.getScrollInfo().top, 'local');
                };
                syncResultScrollAtLine(lineNo);
            };



            var buildScrollMap = function(cb) {
                if (building) {
                    return;
                }
                building = true;
                oldVForScroll = editor.getValue();
                $('#out').waitForImages(function() {
                    var i, offset, nonEmptyList, pos, a, b, lineHeightMap, linesCount,
                        acc, textarea = $('.CodeMirror'),
                        _scrollMap;

                    offset = $('#out').scrollTop() - $('#out').offset().top;
                    _scrollMap = [];
                    nonEmptyList = [];

                    linesCount = editor.lineCount();
                    for (i = 0; i < linesCount; i++) {
                        _scrollMap.push(-1);
                    }

                    nonEmptyList.push(0);
                    _scrollMap[0] = 0;

                    $('.line').each(function(n, el) {
                        var $el = $(el),
                            t = $el.data('line');
                        if (t === '') {
                            return;
                        }
                        if (t !== 0) {
                            nonEmptyList.push(t);
                        }
                        _scrollMap[t] = Math.round($el.offset().top + offset);
                    });

                    nonEmptyList.push(linesCount);
                    _scrollMap[linesCount] = $('#out')[0].scrollHeight;

                    pos = 0;
                    for (i = 1; i < linesCount; i++) {
                        if (_scrollMap[i] !== -1) {
                            pos++;
                            continue;
                        }

                        a = nonEmptyList[pos];
                        b = nonEmptyList[pos + 1];
                        _scrollMap[i] = Math.round((_scrollMap[b] * (i - a) + _scrollMap[a] * (b - i)) / (b - a));
                    }

                    building = false;
                    if (editor.getValue() == oldVForScroll) {
                        scrollMap = _scrollMap;
                        if (cb) {
                            cb();
                        }
                    }
                })
            }
            return {
              	reset : function(){
                	scrollMap = null;
                },
              	resetAndSync:function(){
                	scrollMap = null;
                  	syncResultScroll();
                },
                doSync: syncResultScroll,
                doSyncAtLine: function(lineFun) {
                    syncResultScrollAtLine(lineFun)
                }
            }
        })(editor);