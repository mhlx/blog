var storage = (function(){
	var localStore = (function(){
		var support = false;
		try{
			window.localStorage.setItem('1',1);
			window.localStorage.removeItem('1',1);
			support = true;
		}catch(e){
			
		}
		var key = "markdown-editor";
		var themeKey = "markdown-editor-theme";
		
		var toList = function(all){
			if(!support){
				return [];
			}
			var json = localStorage.getItem(key);
			if(json === null){
				return [];
			}
			var list = $.parseJSON(json);
			for(var i=list.length-1;i>=0;i--){
				if(!all && list[i].name == 'undefined'){
					list.splice(i,1);
					continue;
				}
				list[i].title = list[i].name;
			}
				
			return list;
		}
		
		var add = function(name,content){
			if(!support) return 'fail';
			var list = toList(true);
			removeByName(list,name);
			list.push({"name":name,"content":content});
			if(save(list)) return "success";
			return "fail";
		}
		
		
		var del = function(name){
			if(!support) return 'fail';
			var list = toList(true);
			removeByName(list,name);
			if(save(list)) return "success";
			return "fail";
		}
		
		var save = function(list){
			var json = JSON.stringify(list);
			try{localStorage.setItem(key,json);
			return true;
			}catch(e){
			return false;
			}
		}
		
		
		var removeByName = function(list,name){
			
			for(var i=0;i<list.length;i++){
				if(list[i].name == name){
					list.splice(i,1);
					break;
				}
			}
		}
		
		var get = function(name){
			var list = toList(true);
			for(var i=0;i<list.length;i++){
				if(list[i].name == name){
					list[i].title = name;
					return list[i];
				}
			}
			return null;
		}
		return {
			"getList":function(cb){
				if(cb){
					cb(toList(false));
				}
			},
			"del" :function(name,cb){
				var rst = del(name);
				if(cb)
					cb(rst);
			},
			"add":	function(name,content,cb){
				var rst = add(name,content);
				if(cb)
					cb(rst);
			},
			"get":function(name,cb){
				var rst = get(name);
				if(cb)
					cb(rst);
			},
			"saveTheme":function(theme,cb){
				var json = JSON.stringify(theme);
				try{localStorage.setItem(themeKey,json);
				if(cb) cb("success");
				}catch(e){
				if(cb) cb("fail");
				}
			},
			"getTheme":function(cb){
				if(!cb) return ;
				if(!support){
					cb(null);
				}
				var json = localStorage.getItem(themeKey);
				if(json == null ){
					cb(null)
				}else{
					cb($.parseJSON(json));
				}
					
			},
			"delTheme":function(cb){
				if(!support){
					if(cb) cb("fail");
				}
				try{
					localStorage.removeItem(themeKey);
					if(cb) cb("success");
				}catch(e){if(cb) cb("fail");
				}
			}
		}
	})();
	
	
	
	return localStore;
})();