emoji = (function() {
	var emojiStr = $
			.trim("😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 🙃 😉 😌 😍 😘 😗 😙 😚 😋 😜 😝 😛 🤑 🤗 🤓 😎 🤡 🤠 😏 😒 😞 😔 😟 😕 🙁 ☹️ 😣 😖 😫 😩 😤 😠 😡 😶 😐 😑 😯 😦 😧 😮 😲 😵 😳 😱 😨 😰 😢 😥 🤤 😭 😓 😪 😴 🙄 🤔 🤥 😬 🤐 🤢 🤧 😷 🤒 🤕 😈 👿");

	var emojiArray = emojiStr.split(' ');
	var str = '';
	for (var i = 0; i < emojiArray.length; i++) {
		str += '<span data-emoji style="cursor:pointer">' + emojiArray[i]
				+ '</span>';
	}
	var modal = '<div class="modal fade" tabindex="-1" role="dialog" id="emojiModal"> <div class="modal-dialog" role="document"> <div class="modal-content"> <div class="modal-header"><h4 class="modal-title">emoji</h4>  <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button> </div> <div class="modal-body"> <p style="font-family: Segoe UI Emoji; font-size: 2.5em;text-align:center">'
			+ str + '</p> </div> </div> </div></div>';

	$(modal).appendTo($('body'));

	$('[data-emoji]').click(function() {
		var text = $(this).text();
		if (click) {
			if (click(text)) {
				$("#emojiModal").modal('hide');
			}
		}
	});

	click: undefined;

	return {
		show : function() {
			$("#emojiModal").modal('show')
		},
		setClick : function(fn) {
			click = fn;
		}
	}
})();