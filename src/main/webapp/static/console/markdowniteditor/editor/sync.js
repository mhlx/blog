var sync = (function(editor) {
  			var scrollMap;
            var syncResultScrollAtLine = function(line) {
				if(!config.syncScroll){
                	return ;
                }
				if(!scrollMap){
					buildScrollMap();
					if(!scrollMap){
						return ;
					}
				}
				var offset = scrollMap[line];
				if(offset){
					$('#out').stop(true).animate({
						scrollTop: offset
					}, 100);
				}
            }
            var syncResultScroll = function() {
              	if(!config.syncScroll){
                	return ;
                }
                syncResultScrollAtLine(editor.lineAtHeight(editor.getScrollInfo().top,'local'));
            };
			
			var building = false;

            var buildScrollMap = function() {
				scrollMap = undefined;
				//editor.setOption("readOnly", true);
				if(building)
					return ;
				building = true;
				var offset = ($('#out').scrollTop() - $('#out').offset().top);
				var _scrollMap = [];
				var linesCount = editor.lineCount();
				var lines = document.getElementById("out").querySelectorAll('[data-line]');
				for(var i=0;i<lines.length;i++){
					var ele = lines[i];
					var top = ele.getBoundingClientRect().top;
					var l = parseInt(ele.getAttribute('data-line'));
					_scrollMap[l] = Math.round(top +offset );
					var el = parseInt(ele.getAttribute('data-end-line'));
					if(el != 'NaN' && el > l){
						var scope = el-l;
						for(var j=l;j<=el;j++){
							var p = (j-l)/scope;
							_scrollMap[j] = Math.round(top+ele.clientHeight*(p)  + offset)
						}
					}
				}
				
				scrollMap = _scrollMap;
				//editor.setOption("readOnly", false);
				building = false;
            }
            return {
              	rebuild : function(){
                	buildScrollMap();
                },
              	resetAndSync:function(){
					scrollMap = undefined;
                  	syncResultScroll();
                },
                doSync: syncResultScroll,
                doSyncAtLine: function(line) {
                    syncResultScrollAtLine(line)
                }
            }
        })(editor);