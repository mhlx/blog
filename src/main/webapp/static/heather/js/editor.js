var config = {
    toolbar: true,
    syncScroll: true,
    autoRender: true
}

var scrollBarStyle = CodeMirror.browser.mobile ? 'native' : 'overlay';
var editorTheme = theme.editor.theme? theme.editor.theme : 'default';
var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
   mode: {name: "gfm"},
    lineNumbers: false,
    matchBrackets: true,
    lineWrapping: true,
    dragDrop: true,
	scrollbarStyle: scrollBarStyle,
	theme : editorTheme,
    extraKeys: {
        "Enter": "newlineAndIndentContinueMarkdownList",
        "Alt-F": "findPersistent",
        "Ctrl-A": "selectAll"
    }
});

CodeMirror.keyMap.default["Shift-Tab"] = "indentLess";
CodeMirror.keyMap.default["Tab"] = "indentMore";

var turndownService = new window.TurndownService({
	'headingStyle': 'atx',
	'codeBlockStyle': 'fenced',
	defaultReplacement: function(innerHTML, node) {
		return node.isBlock ? '\n\n' + node.outerHTML + '\n\n' : node.outerHTML
	}
});
turndownService.use(window.turndownPluginGfm.gfm);

editor.setOption('dropContentHandler', function(fileName, content) {
	var ext = fileName.split(".").pop().toLowerCase();
	if (ext == "md") {
		return content;
	} else if (ext == "html" || ext == 'htm') {
		return turndownService.turndown(content);
	}
	return "";
});



