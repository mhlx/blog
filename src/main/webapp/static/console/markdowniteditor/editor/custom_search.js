var search = (function(cm,config) {
    var html = '';
    html += '<div style="position:fixed;bottom:0px;background-color:#fff;width:50%;z-index:99;display:none" id="search-box" >';
    html += ' <form class="form-inline">';
    html += ' <div class="form-group">';
    html += '  <label class="sr-only">内容</label>';
    html += ' <input type="text" class="form-control" placeholder="查找内容" id="search-box-search-input">';
    html += '  </div>';
    html += ' <div class="form-group">';
    html += '      <label class="sr-only">替换内容</label>';
    html += '     <input type="text" class="form-control" placeholder="替换内容" id="search-box-replace-input">';
    html += ' </div>';
    html += '</form>';
    html += '  <button class="btn btn-primary" id="search-btn" type="button">查找</button>';
    html += '  <button class="btn btn-primary" id="search-prev" data-hidden-before-search style="display:none" type="button"><i class="fas fa-arrow-up"></i></button>';
    html += '  <button class="btn btn-primary" id="search-next" data-hidden-before-search style="display:none" type="button"><i class="fas fa-arrow-down"></i></button>';
    html += '  <button class="btn btn-primary" id="search-replace" data-hidden-before-search type="button" style="display:none">替换</button>';
    html += '  <button class="btn btn-primary" id="search-replace-all" data-hidden-before-search type="button" style="display:none">替换全部</button>';
    html += '  <button class="btn btn-primary" id="search-close" type="button">关闭</button>';
    html += '  </div>';
    var searchBox = $(html);
    searchBox.appendTo($("#in"));

    $("#search-btn").click(function() {
        var query = parseQuery($('#search-box-search-input').val());
        if (query != '') {
            clearSearch();
            startSearch(getSearchState(), query);
            $('[data-hidden-before-search]').show();
        }
    });
    $("#search-next").click(function() {
        findNext(false);
    });

    $("#search-prev").click(function() {
        findNext(true);
    });

    $("#search-close").click(function() {
    	clearSearch();
        if (_toolbar) {
            config.toolbar = true;
        }
        $("#search-box form")[0].reset();
        $("#search-box").hide();
    });

    $("#search-replace-all").click(function() {
        var state = getSearchState(cm);
        var text = $("#search-box-replace-input").val();
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
    });

    $("#search-replace").click(function() {
        var state = getSearchState(cm);
        var text = $("#search-box-replace-input").val();
        if (!state.query) {
            return;
        }
        var cursor = getSearchCursor(cm, state.query, cm.getCursor("from"));
        var advance = function() {
            var start = cursor.from(),
            match;
            if (! (match = cursor.findNext())) {
                cursor = getSearchCursor(cm, state.query);
                if (! (match = cursor.findNext()) || (start && cursor.from().line == start.line && cursor.from().ch == start.ch)) return;
            }
            cm.setSelection(cursor.from(), cursor.to());

            var coords = cm.cursorCoords(cursor.from(), 'local');
            cm.scrollTo(0, coords.top);
            cursor.replace(typeof query == "string" ? text: text.replace(/\$(\d)/g,
            function(_, i) {}));
        };
        advance();
    });

    function findNext(rev, callback) {
        var state = getSearchState(cm);
        if (!state.query) {
            return;
        }
        cm.operation(function() {
            var cursor = getSearchCursor(cm, state.query, rev ? state.posFrom: state.posTo);
            if (!cursor.find(rev)) {
                cursor = getSearchCursor(cm, state.query, rev ? CodeMirror.Pos(cm.lastLine()) : CodeMirror.Pos(cm.firstLine(), 0));
                if (!cursor.find(rev)) return;
            }
            cm.setSelection(cursor.from(), cursor.to());
            var coords = cm.cursorCoords(cursor.from(), 'local');
            cm.scrollTo(0, coords.top);
            state.posFrom = cursor.from();
            state.posTo = cursor.to();
            if (callback) callback(cursor.from(), cursor.to())
        });
    }

    function clearSearch() {
        $('[data-hidden-before-search]').hide();
        cm.operation(function() {
            var state = getSearchState(cm);
            state.lastQuery = state.query;
            if (!state.query) return;
            state.query = state.queryText = null;
            cm.removeOverlay(state.overlay);
            if (state.annotate) {
                state.annotate.clear();
                state.annotate = null;
            }
        });
    }

    function getSearchState() {
        return cm.state.search || (cm.state.search = new SearchState());
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
                query = new RegExp(isRE[1], isRE[2].indexOf("i") == -1 ? "": "i");
            } catch(e) {} // Not a regular expression after all, do a string search
        } else {
            query = parseString(query)
        }
        if (typeof query == "string" ? query == "": query.test("")) query = /x^/;
        return query;
    }

    function startSearch(state, query) {
        state.queryText = query;
        state.query = parseQuery(query);
        cm.removeOverlay(state.overlay, queryCaseInsensitive(state.query));
        state.overlay = searchOverlay(state.query, queryCaseInsensitive(state.query));
        cm.addOverlay(state.overlay);
        if (cm.showMatchesOnScrollbar) {
            if (state.annotate) {
                state.annotate.clear();
                state.annotate = null;
            }
            state.annotate = cm.showMatchesOnScrollbar(state.query, queryCaseInsensitive(state.query));
        }
      findNext(false);
    }

    function SearchState() {
        this.posFrom = this.posTo = this.lastQuery = this.query = null;
        this.overlay = null;
    }

    function searchOverlay(query, caseInsensitive) {
        if (typeof query == "string") query = new RegExp(query.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"), caseInsensitive ? "gi": "g");
        else if (!query.global) query = new RegExp(query.source, query.ignoreCase ? "gi": "g");

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

    var _toolbar;

    return {
        open: function() {
            clearSearch();
            _toolbar = config.toolbar;
            if (_toolbar) {
                config.toolbar = false;
				if(inner_bar){
					inner_bar.remove();
				}
            }
            $("#search-box form")[0].reset();
            $("#search-box").show();
            $("#search-box-search-input").focus();
        }
    }

})(editor,config);