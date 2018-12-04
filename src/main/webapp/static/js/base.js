$.ajaxSetup({ cache: false });
function humanFileSize(bytes, si) {
    var thresh = si ? 1000 : 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    var units = si
        ? ['kB','MB','GB','TB','PB','EB','ZB','YB']
        : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
    var u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(1)+' '+units[u];
}

var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");
if (token != null && header != null && token != "" && header != "") {
	$(document).ajaxSend(function(e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});
}

jQuery.prototype.serializeObject=function(){  
    var obj= {};  
    $.each(this.serializeArray(),function(index,param){  
          obj[param.name]=param.value;  
    });  
    return obj;  
}; 

function confirmLogout(){
	$("#mobile-nav").hide();
	swal({
		  title: '你确定要注销吗？',
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonColor: '#3085d6',
		  cancelButtonColor: '#d33',
		  confirmButtonText: '确定!',
		  cancelButtonText: '取消'
		}).then((result) => {
		  if (result.value) {
			  document.getElementById('logoutForm').submit();
		  }
		});
}