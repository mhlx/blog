var emoji = (function(){
  	var emojiStr = $.trim("😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 🙃 😉 😌 😍 😘 😗 😙 😚 😋 😜 😝 😛 🤑 🤗 🤓 😎 🤡 🤠 😏 😒 😞 😔 😟 😕 🙁 ☹️ 😣 😖 😫 😩 😤 😠 😡 😶 😐 😑 😯 😦 😧 😮 😲 😵 😳 😱 😨 😰 😢 😥 🤤 😭 😓 😪 😴 🙄 🤔 🤥 😬 🤐 🤢 🤧 😷 🤒 🤕 😈 👿");
	var modal = '<div class="modal" tabindex="-1"';
	modal += 'role="dialog">';
	modal += '<div class="modal-dialog modal-lg" role="document">';
	modal += '<div class="modal-content">';
	modal += '<div class="modal-header">';
	modal += '<h5 class="modal-title">😄</h5>';
	modal += '<button type="button" class="close" data-dismiss="modal" aria-label="Close">';
	modal += '<span aria-hidden="true">&times;</span>';
	modal += '</button>';
	modal += '</div>';
	modal += '<div class="modal-body">';
  	emojiArray = emojiStr.split(' ');
    var emojiArray = emojiStr.split(' ');
	var str = '';
	for (var i = 0; i < emojiArray.length; i++) {
		str += '<span data-emoji style="cursor:pointer">' + emojiArray[i]
				+ '</span>';
	}
	modal += '<div class="container-fluid" style="font-size:1.5rem">'+str+'</div>';
	modal += '</div>';
	modal += '</div>';
	modal += '</div>';
	modal += '</div>';
	
	var emojiModal = $(modal);
	emojiModal.appendTo($('body'));
  
  return {
  	choose:function(fn){
      	emojiModal.modal('show');
    	emojiModal.find('[data-emoji]').off('click').on('click',function(){
        	var emoji =  $(this).text();
          	fn(emoji);
          	emojiModal.modal('hide');
        });
    }
  }
})();