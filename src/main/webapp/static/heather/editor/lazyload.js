(function(){
	var mermaidLoading = false;
	function loadMermaid(){
		if(mermaidLoading) return ;
		mermaidLoading = true;
		$('<script>').appendTo('body').attr({
			 src: 'js/mermaid.min.js'
		});
		var t = setInterval(function(){
			try{	
				mermaid.initialize({theme:theme.mermaid.theme||'default'});
				clearInterval(t);
				mermaid.init({},'#out .mermaid');
			}catch(e){
				
			}
		},20)
	}
	
	var katexLoading = false;
	function loadKatex(){
		if(katexLoading) return ;
		katexLoading = true;
		$('<link>').appendTo('head').attr({
			  type: 'text/css', 
			  rel: 'stylesheet',
			  href: 'katex/katex.min.css'
		  });
		 $('<script>').appendTo('body').attr({
			 src: 'katex/katex.min.js'
		});
		var t = setInterval(function(){
			try{
				var html = katex.renderToString("", {
					throwOnError: false
				})
				clearInterval(t);
				var katexs = document.getElementById("out").querySelectorAll(".katex");
				for(var i=0;i<katexs.length;i++){
					var block = katexs[i];
					block.innerHTML = katex.renderToString(block.textContent, {
						throwOnError: false,
						displayMode:true
					});
				}
			}catch(e){
				
			}
		},20)
	}
})();