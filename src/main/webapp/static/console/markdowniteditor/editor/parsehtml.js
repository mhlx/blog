(function ($, undefined) {
	var dom_parser = false;

	// based on: https://developer.mozilla.org/en/DOMParser
	// does not work with IE < 9
	// Firefox/Opera/IE throw errors on unsupported types
	try {
		// WebKit returns null on unsupported types
		if ((new DOMParser()).parseFromString("", "text/html")) {
			// text/html parsing is natively supported
			dom_parser = true;
		}
	} catch (ex) {}

	if (dom_parser) {
		$.parseHTML2 = function (html) {
			return new DOMParser().parseFromString(html, "text/html");
		};
	}
	else if (document.implementation && document.implementation.createHTMLDocument) {
		$.parseHTML2 = function (html) {
			var doc = document.implementation.createHTMLDocument("");
			var doc_el = doc.documentElement;

			doc_el.innerHTML = html;

			var els = [], el = doc_el.firstChild;

			while (el) {
				if (el.nodeType === 1) els.push(el);
				el = el.nextSibling;
			}

  			// are we dealing with an entire document or a fragment?
			if (els.length === 1 && els[0].localName.toLowerCase() === "html") {
				doc.removeChild(doc_el);
				el = doc_el.firstChild;
				while (el) {
					var next = el.nextSibling;
					doc.appendChild(el);
					el = next;
				}
			}
			else {
				el = doc_el.firstChild;
				while (el) {
					var next = el.nextSibling;
					if (el.nodeType !== 1 && el.nodeType !== 3) doc.insertBefore(el,doc_el);
					el = next;
				}
			}

			return doc;
		};
	}
})(jQuery);