var getUrlVars = function() {
	var vars = {};
	var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
			function(m, key, value) {
				vars[key] = value;
			});
	return vars;
}
var getUrlParam = function(parameter, defaultvalue) {
	var urlparameter = defaultvalue;
	if (window.location.href.indexOf(parameter) > -1) {
		urlparameter = getUrlVars()[parameter];
	}
	return urlparameter;
}