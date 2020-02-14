var HtmlPasteHelper = (function() {

    var blockElements = [
        'address', 'article', 'aside', 'audio', 'blockquote', 'body', 'canvas',
        'center', 'dd', 'dir', 'div', 'dl', 'dt', 'fieldset', 'figcaption',
        'figure', 'footer', 'form', 'frameset', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'header', 'hgroup', 'hr', 'html', 'isindex', 'li', 'main', 'menu', 'nav',
        'noframes', 'noscript', 'ol', 'output', 'p', 'pre', 'section', 'table',
        'tbody', 'td', 'tfoot', 'th', 'thead', 'tr', 'ul'
    ];

    function isBlockDefault(node) {
        if (node.nodeType == 1) {
            var tag = node.tagName.toLowerCase();
            for (const blockElementName of blockElements) {
                if (blockElementName == tag) {
                    return true;
                }
            }
        }
        return false;
    }

    var turndownService = (function() {


        var alignMap = {
            '': ' -- ',
            'left': ' :-- ',
            'center': ' :--: ',
            'right': ' --: '
        }

        var turndownService = new window.TurndownService({
            'headingStyle': 'atx',
            'codeBlockStyle': 'fenced',
            'emDelimiter': '*',
            'bulletListMarker': '-',
            defaultReplacement: function(innerHTML, node) {
                return '';
            }
        });
        turndownService.use(window.turndownPluginGfm.gfm);
        turndownService.addRule('paragraph', {
            filter: ['p'],
            replacement: function(content) {
                var lines = content.replaceAll('\r', '').split('\n');
                var rst = '';
                for (var i = 0; i < lines.length; i++) {
                    var line = lines[i];
                    if (line == '  ') {
                        rst += '<br>';
                    } else {
                        rst += line;
                    }
                    if (i < lines.length - 1)
                        rst += '\n'
                }
                return '\n\n' + rst + '\n\n';
            }
        });

        turndownService.addRule('fenceblock', {
            filter: function(node, options) {
                return (
                    options.codeBlockStyle === 'fenced' &&
                    node.nodeName === 'PRE' &&
                    node.firstChild &&
                    node.firstChild.nodeName === 'CODE'
                )
            },

            replacement: function(content, node, options) {
                var className = node.firstChild.className || '';
                var language = getLanguage(node.firstChild) || '';
                var textContent = node.firstChild.textContent;
                var lines = textContent.split('\n');
                var lastLine = lines[lines.length - 1];
                var lineBreaker = lastLine.replaceAll('\n', '') == '' ? '' : '\n';
                return '\n\n' + options.fence + " " + language + '\n' + textContent + lineBreaker + options.fence + '\n\n';
            }
        });

        turndownService.addRule('table', {
            filter: function(node) {
                return node.nodeName === 'TABLE'
            },
            replacement: function(content, node, options) {
                return '\n\n' + tableToMarkdown(node) + '\n\n';
            }
        });

        turndownService.addRule('list', {
            filter: ['ul', 'ol'],
            replacement: function(content, node, options) {
                return '\n\n' + listToMarkdown(node, options, 0) + '\n\n';
            }
        });

        turndownService.addRule('span', {
            filter: ['span'],
            replacement: function(content, node, options) {
                return turndownService.turndown(node.innerHTML);
            }
        });

        function getCheckbox(li) {
            var firstChild = li.childNodes[0];
            if (firstChild == null) {
                return null;
            }
            if (firstChild.nodeType != 1) {
                if (firstChild.nodeType == 3) {
                    //get next element if exists
                    var elem = firstChild;
                    var removes = [];
                    while (elem != null) {
                        if (elem.nodeType == 1) {
                            break;
                        }
                        if (elem.nodeValue.trim() != '') {
                            return null;
                        }
                        removes.push(elem);
                        elem = elem.nextSibling;
                    }
                    for (var i = 0; i < removes.length; i++) {
                        removes[i].remove();
                    }
                    if (elem == null) {
                        return null;
                    }
                    firstChild = elem;
                }
            }
            if (firstChild.type == 'checkbox') {
                return firstChild;
            }
            if (firstChild != null && firstChild.nodeType == 1 && firstChild.tagName == 'P') {
                var firstChildFirstChild = firstChild.firstChild;
                if (firstChildFirstChild != null && firstChildFirstChild.nodeType == 1 &&
                    firstChildFirstChild.type == 'checkbox') {
                    //move checkbox to first	
                    var clone = firstChildFirstChild.cloneNode(true);
                    li.parentElement.prepend(clone);
                    firstChildFirstChild.remove();
                    return clone;
                }
            }
            return null;
        }

        function wrapToParagraph(node, checkbox) {
            node.normalize();
            var array = [];
            //wrap inline|text nodes to paragraph
            var childNodes = node.childNodes;
            if (childNodes.length > 0) {
                for (var i = 0; i < childNodes.length; i++) {
                    var childNode = childNodes[i];
                    if (childNode == checkbox) {
                        continue;
                    }
                    if (childNode.nodeType == 1) {
                        if (isBlockDefault(childNode)) {
                            break;
                        }
                        array.push(childNode);
                    } else {
                        array.push(childNode);
                    }
                }
            }
            if (array.length > 0) {
                var paragraph = document.createElement('p');
                array[array.length - 1].after(paragraph);
                for (var i = 0; i < array.length; i++) {
                    paragraph.appendChild(array[i].cloneNode(true));
                    array[i].remove();
                }
            }
        }


        function listToMarkdown(node, options, indent) {
            var ol = node.tagName == 'OL';
            var index = 1;
            var markdown = '';
            for (const li of node.children) {
                var liMarkdowns = [];
                var checkbox = getCheckbox(li);
                wrapToParagraph(li, checkbox);
                var taskList = checkbox != null;
                var prefix = '';
                if (taskList) {
                    if (checkbox.checked) {
                        prefix = options.bulletListMarker + ' [x]';
                    } else {
                        prefix = options.bulletListMarker + ' [ ]';
                    }
                } else {
                    prefix = ol ? index + '.' : options.bulletListMarker;
                }
                var spaces = taskList ? getSpaces(2) : getSpaces(prefix.length + 1);
                var j = 0;
                for (const child of li.children) {
                    if (child === checkbox) continue;
                    if (child.tagName == 'OL' || child.tagName == 'UL') {
                        liMarkdowns.push(listToMarkdown(child, options, taskList ? 2 : prefix.length + 1));
                    } else {
                        var str = turndownService.turndown(child.outerHTML);
                        var strs = [];
                        for (const line of str.split('\n')) {
                            strs.push(spaces + line);
                        }
                        liMarkdowns.push(strs.join('\n'));
                    }
                    j++;
                }
                var liMarkdown = liMarkdowns.join('\n\n');
                liMarkdown = liMarkdown.substring(taskList ? ' ' : prefix.length);
                markdown += prefix + liMarkdown + '\n';
                index++;
            }
            var indentMarkdown = [];
            for (const line of markdown.split('\n')) {
                indentMarkdown.push(getSpaces(indent) + line);
            }
            return indentMarkdown.join('\n');
        }

        function getSpaces(indent) {
            var spaces = '';
            for (var k = 0; k < indent; k++) {
                spaces += ' ';
            }
            return spaces;
        }

        function tableToMarkdown(table) {

            //if td|th has colspan | rowspan attribute
            //can not parsed to markdown
            //return raw html
            if (table.querySelectorAll('[colspan][rowspan]').length > 0) {
                return table.outerHTML;
            }

            var markdown = '';
            var trs = table.querySelectorAll('tr');

            for (var i = 0; i < trs.length; i++) {
                var tr = trs[i];
                var headingRow = i == 0;
                if (headingRow) {

                    var ths = tr.querySelectorAll('th');
                    if (ths.length == 0) {
                        ths = tr.querySelectorAll('td');
                    }
                    markdown += getRowMarkdown(ths);
                    markdown += '\n';
                    for (var j = 0; j < ths.length; j++) {
                        var align = ths[j].style['text-align'];
                        markdown += "|" + alignMap[align];
                    }
                    markdown += "|";
                } else {
                    markdown += getRowMarkdown(tr.querySelectorAll('td'));
                }
                if (i < trs.length - 1) {
                    markdown += '\n';
                }
            }
            return markdown;
        }

        function getRowMarkdown(tds) {
            var markdown = '';
            for (var j = 0; j < tds.length; j++) {
                var td = tds[j];
                markdown += "|";
                var md = turndownService.turndown(td.innerHTML);
                // need to convert all '|' to '\|';
                var newMd = '';
                for (var i = 0; i < md.length; i++) {
                    var ch = md.charAt(i);
                    if (ch == '|') {
                        var prevCh = i == 0 ? '' : md.charAt(i - 1);
                        if (prevCh == '\\') {
                            newMd += ch;
                        } else {
                            newMd += '\\' + ch;
                        }
                    } else {
                        newMd += ch;
                    }
                }
                newMd = newMd.trim().replace(/\n\r/g, '<br>').replace(/\n/g, "<br>");
                markdown += newMd.length < 3 ? "  " + newMd : newMd;
            }
            markdown += "|";
            return markdown;
        }
        return turndownService;
    })();


    var unwrapSet = new Set(['DIV', 'ARTICLE', 'SECTION', 'DL', 'DT', 'DD']);

    var getNodesFromPasteHTML = function(html) {
        var root = Heather.Util.parseHTML(html);
        unwrapNode(unwrapSet, root);
        var needWraps = [];
        var lastBlockNode;
        for (const node of root.childNodes) {
            if (isBlockDefault(node)) {
                lastBlockNode = node;
                if (needWraps.length > 0) {
                    var p = document.createElement('p');
                    for (const needWrap of needWraps) {
                        p.appendChild(needWrap.cloneNode(true));
                        needWrap.remove();
                    }
                    node.before(p);
                    needWraps = [];
                }
                continue;
            } else {
                needWraps.push(node);
            }
        }
        if (needWraps.length > 0) {
            var p = document.createElement('p');
            for (const needWrap of needWraps) {
                p.appendChild(needWrap.cloneNode(true));
                needWrap.remove();
            }
            if (lastBlockNode) {
                lastBlockNode.after(p);
            } else {
                root.innerHTML = p.outerHTML;
            }
        }


        var childNodes = root.childNodes;
        var nodes = [];
        for (const childNode of childNodes) {
            var _nodes = [];
            var node = childNode;
            var nodeType = node.nodeType;
            if (nodeType != 1 && nodeType != 3) continue;
            _nodes.push(node);
            for (const _node of _nodes) {
                var n = _node;
                if (n.nodeType == 3) {
                    var p = document.createElement('p');
                    p.appendChild(n);
                    nodes.push(p);
                    continue;
                }
                n = processPasteNode(n);
                nodes.push(n);
            }
        }
        return nodes;
    }

    function unwrapNode(tagNames, node) {
        if (tagNames.has(node.tagName)) {
            node.normalize();
            var array = [];
            var array2 = [];
            var childNodes = node.childNodes;
            if (childNodes.length > 0) {
                for (var i = 0; i < childNodes.length; i++) {
                    var childNode = childNodes[i];
                    if (childNode.nodeType == 1) {
                        if (isBlockDefault(childNode)) {

                            if (array.length > 0) {
                                array2.push(array);
                                array = [];
                            }

                        } else {
                            array.push(childNode);
                        }
                    } else {
                        array.push(childNode);
                    }
                }
                if (array.length > 0) {
                    array2.push(array);
                }
            }
            for (const array of array2) {
                var paragraph = document.createElement('p');
                array[array.length - 1].after(paragraph);
                for (var i = 0; i < array.length; i++) {
                    paragraph.appendChild(array[i].cloneNode(true));
                    array[i].remove();
                }
            }

            var childNodes = node.childNodes;
            var nodes = [];
            var parent = node.parentNode;
            while (node.firstChild) {
                var first = node.firstChild;
                parent.insertBefore(first, node);
                nodes.push(first);
            };
            parent.removeChild(node);
            for (const _node of nodes) {
                unwrapNode(tagNames, _node);
            }
        } else {
            var children = node.children;
			for(var i=children.length-1;i>=0;i--){
				unwrapNode(tagNames, children[i]);
			}
        }
    }

    function removeAttributes(element, checker) {
        var attrs = element.attributes;
        for (var i = attrs.length - 1; i >= 0; i--) {
            if (checker(element, attrs[i]) === true) {
                element.removeAttribute(attrs[i].name);
            }
        }
        for (const child of element.children) {
            removeAttributes(child, checker);
        }
    }

    function getMarkdownFromPasteHTML(html) {
        var nodes = getNodesFromPasteHTML(html);
        var markdowns = [];
        for (const node of nodes) {
            var markdown = turndownService.turndown(node.outerHTML);
            if (markdown.trim() === '') continue;
            markdowns.push(markdown.replaceAll('&nbsp;', ' '))
        }
        return markdowns.join('\n\n');
    }



    function getLanguage(code) {
        var classes = code.getAttribute('class');
        if (classes != null) {
            var classArray = classes.split(' ');
            for (const clazz of classArray) {
                if (clazz.startsWith("language-")) {
                    return clazz.split('-')[1];
                }
            }
        }
    }

    function processPasteNode(node) {
        if (node.tagName === 'PRE') {
            var code = node.querySelector('code');
            var language;
            if (code != null) {
                language = getLanguage(code);
            }
            var innerHTML = code == null ? node.innerHTML : code.innerHTML;
            var pre = document.createElement('pre');
            var codeNode = document.createElement('code');
            if (!Heather.Util.isUndefined(language)) {
                codeNode.classList.add('language-' + language);
            }
            codeNode.innerHTML = innerHTML;
            pre.appendChild(codeNode);
            return pre;
        }
        return node;
    }

    return {
        getMarkdownFromPasteHTML: getMarkdownFromPasteHTML,
        unwrapSet: unwrapSet,
		turndownService : turndownService
    }

})();