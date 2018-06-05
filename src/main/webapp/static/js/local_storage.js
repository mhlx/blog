var local_storage = (function() {
	
	var try_do = function(exe){
		try {
			exe();
			return true;
		} catch (exception) {
			return false;
		}
	}
	
	//如果存储满了有bug
	var hasStorage = (function() {
		return try_do(function(){
			var mod = '1';
			localStorage.setItem(mod, mod);
			localStorage.removeItem(mod);
		})
	}());
	
	return {
		isSupprt:function(){
			return hasStorage;
		},
		store:function(k,v){
			if(!hasStorage){
				return false;
			}
			localStorage.setItem(k,v);
		},
		
		get:function(k){
			return localStorage.getItem(k);
		},
		
		remove : function(k){
			if(hasStorage){
				localStorage.removeItem(k);
			}
		},
		
		each : function(fn){
			if(hasStorage){
				$.each(localStorage, function(key, value){
					 fn(key,value);
				});
			}
		}
	}
}());