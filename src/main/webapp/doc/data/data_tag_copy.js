(function(){
	var t;
	var clipboard = new Clipboard('[data-clipboard]', {
		target : function(trigger) {
			return trigger.nextElementSibling;
		}
	});
	clipboard.on('success', function(e) {
		e.clearSelection();
		var $trigger = $(e.trigger);
		$trigger.html('拷贝成功');
		if (t) {
			clearTimeout(t);
		}
		t = setTimeout(function() {
			$trigger.html('拷贝');
		}, 500);
	});
	clipboard.on('error', function(e) {
		console.log(e);
		var $trigger = $(e.trigger);
		$trigger.html('拷贝失败');
		if (t) {
			clearTimeout(t);
		}
		t = setTimeout(function() {
			$trigger.html('拷贝');
		}, 500);
	});
})();