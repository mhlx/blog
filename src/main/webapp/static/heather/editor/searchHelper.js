var searchHelper = (function(cm, config) {
	'use strict';
    var html = '';
    html += '<div id="searchHelper" style="position:absolute;bottom:0px;width:100%;z-index:99;display:none;padding:20px">';
    html += ' <form>';
    html += ' <div class="form-group">';
    html += ' <input type="text" style="outline: 0;border-width: 0 0 2px;" class="form-control" placeholder="查找内容" id="search-box-search-input">';
    html += '  </div>';
    html += ' <div class="form-group">';
    html += '     <input type="text" style="outline: 0;border-width: 0 0 2px;" class="form-control" placeholder="替换内容" id="search-box-replace-input">';
    html += ' </div>';
    html += '</form>';
    html += '<i class="fas fa-search" style="font-size:30px;margin-right:20px;cursor:pointer" id="search-btn"></i>'
    html += '<i data-hidden-before-search class="fas fa-arrow-up" style="font-size:30px;display:none;margin-right:20px;cursor:pointer" id="search-prev" ></i>';
    html += '<i data-hidden-before-search id="search-next" class="fas fa-arrow-down" style="display:none;margin-right:20px;font-size:30px;cursor:pointer"></i>';
    html += '<i data-hidden-before-search id="search-replace"  class="fas fa-undo" style="font-size:30px;display:none;margin-right:20px;cursor:pointer"></i>';
    html += '<i id="search-replace-all" data-hidden-before-search class="fab fa-asymmetrik"  style="font-size:30px;display:none;margin-right:20px;cursor:pointer"></i>';
  	html += '<i id="search-close" class="fas fa-times"  style="font-size:30px;cursor:pointer"></i>'
    html += '  </div>';
	
	function SearchHelper(){
		var div = document.createElement('div');
		div.innerHTML = html;
		var panel = div.firstChild;
		document.getElementById("in").appendChild(panel);
		this.$panel = $(panel);
		var helper = this;
		$("#search-btn").click(function(){
			var query = $("#search-box-search-input").val();
			if(query != ''){
				helper.startSearch(query,function(){
					$('[data-hidden-before-search]').show();
				});
			}
		});
		$("#search-replace").click(function() {
			var text = $("#search-box-replace-input").val();
		    helper.replace(text);
		});
		$("#search-replace-all").click(function() {
			var text = $("#search-box-replace-input").val();
		    helper.replaceAll(text);
		});
		 $("#search-next").click(function() {
			helper.findNext(false);
		});

		$("#search-prev").click(function() {
			helper.findNext(true);
		});
		
		$("#search-close").click(function() {
			helper.close();
		});
	}
	
	SearchHelper.prototype.bind = function(name,handler){
		this.$panel[0].addEventListener(name,handler);
	}
	SearchHelper.prototype.unbind = function(name,handler){
		this.$panel[0].removeEventListener(name,handler);
	}
	
	SearchHelper.prototype.startSearch = function(query,callback){
		this.clearSearch();
		var state = getSearchState();
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
        this.findNext(false,callback);
	}
	
	SearchHelper.prototype.clearSearch = function(){
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
	
	SearchHelper.prototype.open = function(){
		this.clearSearch();
		this.$panel.find('form')[0].reset();
		this.$panel.show();
		var  openEvent = new CustomEvent('open', {
		  bubbles: true
		});
		this.$panel[0].dispatchEvent(openEvent);
	}
	
	SearchHelper.prototype.close = function(){
		this.clearSearch();
		this.$panel.find('form')[0].reset();
        this.$panel.hide();
		this.$panel.find('[data-hidden-before-search]').hide();
		var  closeEvent = new CustomEvent('close', {
		  bubbles: true
		});
		this.$panel[0].dispatchEvent(closeEvent);
	}
	
	SearchHelper.prototype.replaceAll = function(text){
		var state = getSearchState(cm);
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
	
	SearchHelper.prototype.replace = function(text){
		var state = getSearchState(cm);
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
	}


	SearchHelper.prototype.findNext = function(rev,callback){
		var state = getSearchState(cm);
        if (!state.query) {
            return;
        }
        cm.operation(function() {
            var cursor = getSearchCursor(cm, state.query, rev ? state.posFrom : state.posTo);
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

	
	return new SearchHelper();

})(editor, config);