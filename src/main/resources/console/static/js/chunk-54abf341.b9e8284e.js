(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-54abf341"],{"09f4":function(t,e,n){"use strict";n.d(e,"a",(function(){return a})),Math.easeInOutQuad=function(t,e,n,i){return t/=i/2,t<1?n/2*t*t+e:(t--,-n/2*(t*(t-2)-1)+e)};var i=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(t){window.setTimeout(t,1e3/60)}}();function o(t){document.documentElement.scrollTop=t,document.body.parentNode.scrollTop=t,document.body.scrollTop=t}function r(){return document.documentElement.scrollTop||document.body.parentNode.scrollTop||document.body.scrollTop}function a(t,e,n){var a=r(),l=t-a,s=20,c=0;e="undefined"===typeof e?500:e;var u=function t(){c+=s;var r=Math.easeInOutQuad(c,a,l,e);o(r),c<e?i(t):n&&"function"===typeof n&&n()};u()}},"3f5e":function(t,e,n){"use strict";n.d(e,"d",(function(){return o})),n.d(e,"b",(function(){return r})),n.d(e,"e",(function(){return a})),n.d(e,"c",(function(){return l})),n.d(e,"f",(function(){return s})),n.d(e,"a",(function(){return c}));var i=n("b775");function o(t){return Object(i["a"])({url:"/files",method:"get",params:t})}function r(t){return Object(i["a"])({url:"/file",method:"post",data:t})}function a(t){return Object(i["a"])({url:"file",method:"get",params:{path:t}})}function l(t){return Object(i["a"])({url:"file",method:"delete",params:{path:t}})}function s(t,e){return Object(i["a"])({url:"file",method:"patch",params:{path:t},data:e})}function c(t,e){return Object(i["a"])({url:"file",method:"post",params:{path:t,dir:e}})}},"46c7":function(t,e,n){t.exports=n.p+"static/img/file.c1cdb1da.svg"},abfe:function(t,e,n){t.exports=n.p+"static/img/folder.f4979b25.svg"},b311:function(t,e,n){
/*!
 * clipboard.js v2.0.4
 * https://zenorocha.github.io/clipboard.js
 * 
 * Licensed MIT © Zeno Rocha
 */
(function(e,n){t.exports=n()})(0,(function(){return function(t){var e={};function n(i){if(e[i])return e[i].exports;var o=e[i]={i:i,l:!1,exports:{}};return t[i].call(o.exports,o,o.exports,n),o.l=!0,o.exports}return n.m=t,n.c=e,n.d=function(t,e,i){n.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:i})},n.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},n.t=function(t,e){if(1&e&&(t=n(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var i=Object.create(null);if(n.r(i),Object.defineProperty(i,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var o in t)n.d(i,o,function(e){return t[e]}.bind(null,o));return i},n.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return n.d(e,"a",e),e},n.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},n.p="",n(n.s=0)}([function(t,e,n){"use strict";var i="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(t){return typeof t}:function(t){return t&&"function"===typeof Symbol&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":typeof t},o=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),r=n(1),a=f(r),l=n(3),s=f(l),c=n(4),u=f(c);function f(t){return t&&t.__esModule?t:{default:t}}function p(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}function d(t,e){if(!t)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!e||"object"!==typeof e&&"function"!==typeof e?t:e}function h(t,e){if("function"!==typeof e&&null!==e)throw new TypeError("Super expression must either be null or a function, not "+typeof e);t.prototype=Object.create(e&&e.prototype,{constructor:{value:t,enumerable:!1,writable:!0,configurable:!0}}),e&&(Object.setPrototypeOf?Object.setPrototypeOf(t,e):t.__proto__=e)}var m=function(t){function e(t,n){p(this,e);var i=d(this,(e.__proto__||Object.getPrototypeOf(e)).call(this));return i.resolveOptions(n),i.listenClick(t),i}return h(e,t),o(e,[{key:"resolveOptions",value:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};this.action="function"===typeof t.action?t.action:this.defaultAction,this.target="function"===typeof t.target?t.target:this.defaultTarget,this.text="function"===typeof t.text?t.text:this.defaultText,this.container="object"===i(t.container)?t.container:document.body}},{key:"listenClick",value:function(t){var e=this;this.listener=(0,u.default)(t,"click",(function(t){return e.onClick(t)}))}},{key:"onClick",value:function(t){var e=t.delegateTarget||t.currentTarget;this.clipboardAction&&(this.clipboardAction=null),this.clipboardAction=new a.default({action:this.action(e),target:this.target(e),text:this.text(e),container:this.container,trigger:e,emitter:this})}},{key:"defaultAction",value:function(t){return v("action",t)}},{key:"defaultTarget",value:function(t){var e=v("target",t);if(e)return document.querySelector(e)}},{key:"defaultText",value:function(t){return v("text",t)}},{key:"destroy",value:function(){this.listener.destroy(),this.clipboardAction&&(this.clipboardAction.destroy(),this.clipboardAction=null)}}],[{key:"isSupported",value:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:["copy","cut"],e="string"===typeof t?[t]:t,n=!!document.queryCommandSupported;return e.forEach((function(t){n=n&&!!document.queryCommandSupported(t)})),n}}]),e}(s.default);function v(t,e){var n="data-clipboard-"+t;if(e.hasAttribute(n))return e.getAttribute(n)}t.exports=m},function(t,e,n){"use strict";var i="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(t){return typeof t}:function(t){return t&&"function"===typeof Symbol&&t.constructor===Symbol&&t!==Symbol.prototype?"symbol":typeof t},o=function(){function t(t,e){for(var n=0;n<e.length;n++){var i=e[n];i.enumerable=i.enumerable||!1,i.configurable=!0,"value"in i&&(i.writable=!0),Object.defineProperty(t,i.key,i)}}return function(e,n,i){return n&&t(e.prototype,n),i&&t(e,i),e}}(),r=n(2),a=l(r);function l(t){return t&&t.__esModule?t:{default:t}}function s(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}var c=function(){function t(e){s(this,t),this.resolveOptions(e),this.initSelection()}return o(t,[{key:"resolveOptions",value:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};this.action=t.action,this.container=t.container,this.emitter=t.emitter,this.target=t.target,this.text=t.text,this.trigger=t.trigger,this.selectedText=""}},{key:"initSelection",value:function(){this.text?this.selectFake():this.target&&this.selectTarget()}},{key:"selectFake",value:function(){var t=this,e="rtl"==document.documentElement.getAttribute("dir");this.removeFake(),this.fakeHandlerCallback=function(){return t.removeFake()},this.fakeHandler=this.container.addEventListener("click",this.fakeHandlerCallback)||!0,this.fakeElem=document.createElement("textarea"),this.fakeElem.style.fontSize="12pt",this.fakeElem.style.border="0",this.fakeElem.style.padding="0",this.fakeElem.style.margin="0",this.fakeElem.style.position="absolute",this.fakeElem.style[e?"right":"left"]="-9999px";var n=window.pageYOffset||document.documentElement.scrollTop;this.fakeElem.style.top=n+"px",this.fakeElem.setAttribute("readonly",""),this.fakeElem.value=this.text,this.container.appendChild(this.fakeElem),this.selectedText=(0,a.default)(this.fakeElem),this.copyText()}},{key:"removeFake",value:function(){this.fakeHandler&&(this.container.removeEventListener("click",this.fakeHandlerCallback),this.fakeHandler=null,this.fakeHandlerCallback=null),this.fakeElem&&(this.container.removeChild(this.fakeElem),this.fakeElem=null)}},{key:"selectTarget",value:function(){this.selectedText=(0,a.default)(this.target),this.copyText()}},{key:"copyText",value:function(){var t=void 0;try{t=document.execCommand(this.action)}catch(e){t=!1}this.handleResult(t)}},{key:"handleResult",value:function(t){this.emitter.emit(t?"success":"error",{action:this.action,text:this.selectedText,trigger:this.trigger,clearSelection:this.clearSelection.bind(this)})}},{key:"clearSelection",value:function(){this.trigger&&this.trigger.focus(),window.getSelection().removeAllRanges()}},{key:"destroy",value:function(){this.removeFake()}},{key:"action",set:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"copy";if(this._action=t,"copy"!==this._action&&"cut"!==this._action)throw new Error('Invalid "action" value, use either "copy" or "cut"')},get:function(){return this._action}},{key:"target",set:function(t){if(void 0!==t){if(!t||"object"!==("undefined"===typeof t?"undefined":i(t))||1!==t.nodeType)throw new Error('Invalid "target" value, use a valid Element');if("copy"===this.action&&t.hasAttribute("disabled"))throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');if("cut"===this.action&&(t.hasAttribute("readonly")||t.hasAttribute("disabled")))throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');this._target=t}},get:function(){return this._target}}]),t}();t.exports=c},function(t,e){function n(t){var e;if("SELECT"===t.nodeName)t.focus(),e=t.value;else if("INPUT"===t.nodeName||"TEXTAREA"===t.nodeName){var n=t.hasAttribute("readonly");n||t.setAttribute("readonly",""),t.select(),t.setSelectionRange(0,t.value.length),n||t.removeAttribute("readonly"),e=t.value}else{t.hasAttribute("contenteditable")&&t.focus();var i=window.getSelection(),o=document.createRange();o.selectNodeContents(t),i.removeAllRanges(),i.addRange(o),e=i.toString()}return e}t.exports=n},function(t,e){function n(){}n.prototype={on:function(t,e,n){var i=this.e||(this.e={});return(i[t]||(i[t]=[])).push({fn:e,ctx:n}),this},once:function(t,e,n){var i=this;function o(){i.off(t,o),e.apply(n,arguments)}return o._=e,this.on(t,o,n)},emit:function(t){var e=[].slice.call(arguments,1),n=((this.e||(this.e={}))[t]||[]).slice(),i=0,o=n.length;for(i;i<o;i++)n[i].fn.apply(n[i].ctx,e);return this},off:function(t,e){var n=this.e||(this.e={}),i=n[t],o=[];if(i&&e)for(var r=0,a=i.length;r<a;r++)i[r].fn!==e&&i[r].fn._!==e&&o.push(i[r]);return o.length?n[t]=o:delete n[t],this}},t.exports=n},function(t,e,n){var i=n(5),o=n(6);function r(t,e,n){if(!t&&!e&&!n)throw new Error("Missing required arguments");if(!i.string(e))throw new TypeError("Second argument must be a String");if(!i.fn(n))throw new TypeError("Third argument must be a Function");if(i.node(t))return a(t,e,n);if(i.nodeList(t))return l(t,e,n);if(i.string(t))return s(t,e,n);throw new TypeError("First argument must be a String, HTMLElement, HTMLCollection, or NodeList")}function a(t,e,n){return t.addEventListener(e,n),{destroy:function(){t.removeEventListener(e,n)}}}function l(t,e,n){return Array.prototype.forEach.call(t,(function(t){t.addEventListener(e,n)})),{destroy:function(){Array.prototype.forEach.call(t,(function(t){t.removeEventListener(e,n)}))}}}function s(t,e,n){return o(document.body,t,e,n)}t.exports=r},function(t,e){e.node=function(t){return void 0!==t&&t instanceof HTMLElement&&1===t.nodeType},e.nodeList=function(t){var n=Object.prototype.toString.call(t);return void 0!==t&&("[object NodeList]"===n||"[object HTMLCollection]"===n)&&"length"in t&&(0===t.length||e.node(t[0]))},e.string=function(t){return"string"===typeof t||t instanceof String},e.fn=function(t){var e=Object.prototype.toString.call(t);return"[object Function]"===e}},function(t,e,n){var i=n(7);function o(t,e,n,i,o){var r=a.apply(this,arguments);return t.addEventListener(n,r,o),{destroy:function(){t.removeEventListener(n,r,o)}}}function r(t,e,n,i,r){return"function"===typeof t.addEventListener?o.apply(null,arguments):"function"===typeof n?o.bind(null,document).apply(null,arguments):("string"===typeof t&&(t=document.querySelectorAll(t)),Array.prototype.map.call(t,(function(t){return o(t,e,n,i,r)})))}function a(t,e,n,o){return function(n){n.delegateTarget=i(n.target,e),n.delegateTarget&&o.call(t,n)}}t.exports=r},function(t,e){var n=9;if("undefined"!==typeof Element&&!Element.prototype.matches){var i=Element.prototype;i.matches=i.matchesSelector||i.mozMatchesSelector||i.msMatchesSelector||i.oMatchesSelector||i.webkitMatchesSelector}function o(t,e){while(t&&t.nodeType!==n){if("function"===typeof t.matches&&t.matches(e))return t;t=t.parentNode}}t.exports=o}])}))},bf7b:function(t,e,n){"use strict";n.r(e);var i=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"app-container"},[n("el-row",{staticClass:"panel-group",attrs:{gutter:40}},[n("el-col",[n("el-form",{staticClass:"demo-form-inline",attrs:{inline:!0,model:t.listQuery},nativeOn:{submit:function(t){t.preventDefault()}}},[n("el-form-item",{attrs:{label:"文件名"}},[n("el-input",{attrs:{size:"mini",placeholder:"名称"},on:{input:t.changeName},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.getList(e)}},model:{value:t.listQuery.name,callback:function(e){t.$set(t.listQuery,"name",e)},expression:"listQuery.name"}})],1),t._v(" "),n("el-form-item",[n("el-button",{attrs:{type:"primary",size:"mini",icon:"el-icon-search"},on:{click:t.getList}})],1),t._v(" "),n("el-form-item",[n("el-button",{attrs:{type:"button",size:"mini",icon:"el-icon-plus"},on:{click:t.beforeCreateFile}})],1),t._v(" "),n("el-form-item",[n("el-button",{attrs:{type:"button",size:"mini",icon:"el-icon-upload"},on:{click:t.beforeUploadFile}})],1)],1),t._v(" "),t.paths.length>0?n("el-breadcrumb",{staticStyle:{"margin-top":"10px","margin-bottom":"10px"},attrs:{"separator-class":"el-icon-arrow-right"}},t._l(t.paths,(function(e){return n("el-breadcrumb-item",{key:e.path},[n("a",{staticStyle:{color:"#1890ff"},on:{click:function(n){return t.getFilesInDir(e)}}},[t._v(t._s(e.name))])])})),1):t._e(),t._v(" "),n("el-table",{directives:[{name:"loading",rawName:"v-loading",value:t.listLoading,expression:"listLoading"}],attrs:{data:t.page.datas,"highlight-current-row":""},on:{"expand-change":t.expandChange}},[n("el-table-column",{attrs:{type:"expand"},scopedSlots:t._u([{key:"default",fn:function(e){return[t.loadingExpandRow?n("div",{directives:[{name:"loading",rawName:"v-loading",value:t.loadingExpandRow,expression:"loadingExpandRow"}]}):t._e(),t._v(" "),t.loadingExpandRow?t._e():n("div",t._l(t.getExpandFile(e.row),(function(i){return n("el-form",{key:i.path,attrs:{"label-width":"100px","label-position":"left"},nativeOn:{submit:function(t){t.preventDefault()}}},[n("el-form-item",{attrs:{label:"名称"}},[n("span",[t._v(t._s(i.name))])]),t._v(" "),n("el-form-item",{attrs:{label:"路径"}},[n("span",[n("a",{on:{click:function(e){return t.handleCopy(i.path,e)}}},[t._v(t._s(i.path))])])]),t._v(" "),n("el-form-item",{attrs:{label:"类型"}},[n("span",[t._v(t._s(i.dir?"文件夹":"文件"))])]),t._v(" "),n("el-form-item",{attrs:{label:"是否可编辑"}},[n("span",[t._v(t._s(i.editable?"是":"否"))])]),t._v(" "),n("el-form-item",{attrs:{label:"密码保护"}},[n("span",[t._v(t._s(i.protected?"是":"否"))])]),t._v(" "),n("el-form-item",{attrs:{label:"私有访问"}},[n("span",[t._v(t._s(i.private?"是":"否"))])]),t._v(" "),n("el-form-item",{attrs:{label:"大小(字节)"}},[n("span",[t._v(t._s(i.properties.size))])]),t._v(" "),e.row.dir?n("el-form-item",{attrs:{label:"子文件夹数目"}},[n("span",[t._v(t._s(i.properties.dirCount))])]):t._e(),t._v(" "),e.row.dir?n("el-form-item",{attrs:{label:"子文件数目"}},[n("span",[t._v(t._s(i.properties.fileCount))])]):t._e(),t._v(" "),i.properties.height?n("el-form-item",{attrs:{label:"高"}},[n("span",[t._v(t._s(i.properties.height))])]):t._e(),t._v(" "),i.properties.width?n("el-form-item",{attrs:{label:"宽"}},[n("span",[t._v(t._s(i.properties.width))])]):t._e(),t._v(" "),i.properties.duration?n("el-form-item",{attrs:{label:"时长(s)"}},[n("span",[t._v(t._s(i.properties.duration))])]):t._e()],1)})),1)]}}])}),t._v(" "),n("el-table-column",{attrs:{align:"center",label:"预览","min-width":"180"},scopedSlots:t._u([{key:"default",fn:function(e){return[e.row.dir?t._e():n("div",{domProps:{innerHTML:t._s(t.getPreview(e.row))}}),t._v(" "),e.row.dir?n("img",{staticStyle:{"max-width":"100px","max-height":"100px"},attrs:{src:t.dir_icon},on:{click:function(n){return t.getFilesInDir(e.row)}}}):t._e(),t._v(" "),n("div",[n("small",[t._v(t._s(e.row.name.substring(0,10)))])])]}}])}),t._v(" "),n("el-table-column",{attrs:{align:"center",label:"访问控制","min-width":"180"},scopedSlots:t._u([{key:"default",fn:function(e){return[!0===e.row.private?n("span",[t._v("私人访问")]):t._e(),t._v(" "),!0===e.row.protected?n("span",[t._v("密码访问")]):t._e()]}}])}),t._v(" "),n("el-table-column",{attrs:{align:"center",label:"操作","min-width":"130",fixed:!0===t.fixed&&"right"},scopedSlots:t._u([{key:"default",fn:function(e){return[n("ul",{staticClass:"el-dropdown-menu--mini"},[n("li",{staticClass:"el-dropdown-menu__item",on:{click:function(n){return t.handleRename(e.$index,e.row)}}},[n("a",[t._v("重命名")])]),t._v(" "),n("li",{staticClass:"el-dropdown-menu__item",on:{click:function(n){return t.handleSecurity(e.$index,e.row)}}},[n("a",[t._v("访问控制")])]),t._v(" "),n("li",{staticClass:"el-dropdown-menu__item",on:{click:function(n){return t.handleMove(e.$index,e.row)}}},[n("a",[t._v("移动")])]),t._v(" "),n("li",{staticClass:"el-dropdown-menu__item",on:{click:function(n){return t.handleCopyFile(e.$index,e.row)}}},[n("a",[t._v("拷贝")])]),t._v(" "),e.row.editable?n("li",{staticClass:"el-dropdown-menu__item"},[n("router-link",{attrs:{to:"/file/edit/"+e.row.ext.toLowerCase()+"?path="+e.row.path}},[t._v("编辑")])],1):t._e(),t._v(" "),n("li",{staticClass:"el-dropdown-menu__item",on:{click:function(n){return t.handleDelete(e.$index,e.row)}}},[n("a",[t._v("删除")])])])]}}])})],1)],1)],1),t._v(" "),n("pagination",{directives:[{name:"show",rawName:"v-show",value:t.page.totalRow>0,expression:"page.totalRow > 0"}],staticStyle:{"text-align":"center"},attrs:{total:t.page.totalRow,page:t.listQuery.currentPage,"page-size":t.listQuery.pageSize,limit:t.listQuery.pageSize,"pager-count":11},on:{"update:page":function(e){return t.$set(t.listQuery,"currentPage",e)},"update:limit":function(e){return t.$set(t.listQuery,"pageSize",e)},pagination:t.getList}}),t._v(" "),n("el-dialog",{attrs:{title:"新建文件",visible:t.createFileFormVisible,"close-on-click-modal":!1},on:{"update:visible":function(e){t.createFileFormVisible=e}}},[n("el-form",{attrs:{model:t.file},nativeOn:{submit:function(t){t.preventDefault()}}},[n("el-form-item",{attrs:{label:"文件名称"}},[n("el-input",{attrs:{autocomplete:"off"},model:{value:t.file.path,callback:function(e){t.$set(t.file,"path",e)},expression:"file.path"}})],1),t._v(" "),n("el-form-item",{attrs:{label:"文件夹"}},[n("el-switch",{attrs:{"active-color":"#13ce66","inactive-color":"#ff4949","active-value":"DIR","inactive-value":"FILE"},model:{value:t.file.type,callback:function(e){t.$set(t.file,"type",e)},expression:"file.type"}})],1),t._v(" "),"true"!==t.file.private?n("el-form-item",{attrs:{label:"访问密码"}},[n("el-input",{attrs:{placeholder:"访问密码",name:"password"},model:{value:t.file.password,callback:function(e){t.$set(t.file,"password",e)},expression:"file.password"}})],1):t._e(),t._v(" "),t.file.password&&""!==t.file.password?t._e():n("el-form-item",{attrs:{label:"仅限私有访问"}},[n("el-switch",{attrs:{"active-color":"#13ce66","inactive-color":"#ff4949","active-value":"true","inactive-value":"false"},model:{value:t.file.private,callback:function(e){t.$set(t.file,"private",e)},expression:"file.private"}})],1)],1),t._v(" "),n("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[n("el-button",{on:{click:function(e){t.createFileFormVisible=!1}}},[t._v("取 消")]),t._v(" "),n("el-button",{attrs:{type:"primary"},on:{click:t.createFile}},[t._v("确 定")])],1)],1),t._v(" "),n("el-dialog",{attrs:{title:"文件上传",visible:t.uploadFormVisible,"close-on-click-modal":!1},on:{"update:visible":function(e){t.uploadFormVisible=e},close:t.beforeFileUploadModalClose}},[n("el-upload",{ref:"upload",staticClass:"upload-demo",staticStyle:{margin:"0 auto"},attrs:{drag:"",headers:t.uploadHeaders,data:t.uploadData,action:t.uploadUrl,multiple:""}},[n("i",{staticClass:"el-icon-upload"}),t._v(" "),n("div",{staticClass:"el-upload__text"},[t._v("将文件拖到此处，或"),n("em",[t._v("点击上传")])])]),t._v(" "),n("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[n("el-button",{attrs:{type:"primary"},on:{click:t.afterUpload}},[t._v("关 闭")])],1)],1),t._v(" "),n("el-dialog",{attrs:{title:"访问控制",visible:t.securityVisible,"close-on-click-modal":!1},on:{"update:visible":function(e){t.securityVisible=e}}},[n("el-form",{attrs:{model:t.file},nativeOn:{submit:function(t){t.preventDefault()}}},["true"!==t.file.private?n("el-form-item",{attrs:{label:"访问密码"}},[n("el-input",{attrs:{placeholder:"访问密码",name:"password"},model:{value:t.file.password,callback:function(e){t.$set(t.file,"password",e)},expression:"file.password"}})],1):t._e(),t._v(" "),t.file.password&&""!==t.file.password?t._e():n("el-form-item",{attrs:{label:"仅限私有访问"}},[n("el-switch",{attrs:{"active-color":"#13ce66","inactive-color":"#ff4949","active-value":"true","inactive-value":"false"},model:{value:t.file.private,callback:function(e){t.$set(t.file,"private",e)},expression:"file.private"}})],1)],1),t._v(" "),n("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[n("el-button",{on:{click:function(e){t.securityVisible=!1}}},[t._v("取 消")]),t._v(" "),n("el-button",{attrs:{type:"primary"},on:{click:t.confirmSecurity}},[t._v("确 定")])],1)],1)],1)},o=[],r=n("2d63"),a=(n("7f7f"),n("2b0e")),l=n("b311"),s=n.n(l);function c(){a["default"].prototype.$message({message:"拷贝成功",type:"success",duration:1500})}function u(){a["default"].prototype.$message({message:"Copy failed",type:"error"})}function f(t,e){var n=new s.a(e.target,{text:function(){return t}});n.on("success",(function(){c(),n.destroy()})),n.on("error",(function(){u(),n.destroy()})),n.onClick(e)}var p=n("3f5e"),d=n("333d"),h=n("46c7"),m=n.n(h),v=n("abfe"),y=n.n(v),b=n("5f87"),g=n("ed08"),_={name:"File",components:{Pagination:d["a"]},data:function(){return{total:0,listLoading:!0,page:{datas:[],totalRow:0},listQuery:{currentPage:1,pageSize:10,querySubDir:!1},dir_icon:y.a,paths:[],createFileFormVisible:!1,file:{},uploadHeaders:{Token:Object(b["a"])()},uploadData:{dirPath:""},uploadFormVisible:!1,loadingExpandRow:!0,expandFiles:[],securityVisible:!1,uploadUrl:"http://localhost:8080/api/file",fixed:!0}},created:function(){this.getList()},methods:{getList:function(){var t=this;this.listLoading=!0,Object(p["d"])(this.listQuery).then((function(e){t.page=e,t.listLoading=!1,t.paths=t.page.paths.length>0?[{path:"/",name:"根目录"}].concat(t.page.paths):[],t.paths.length>0?t.uploadData.dirPath=t.paths[t.paths.length-1].path:t.uploadData.dirPath="",t.expandFiles=[]})).catch((function(){}))},getPreview:function(t){return t.smallThumbUrl?'<img src="'+t.smallThumbUrl+'" style="max-width:100px;max-height:100px"/>':Object(g["d"])(t.ext)?'<img src="'+t.url+'" style="max-width:100px;max-height:100px"/>':'<img src="'+m.a+'" style="max-width:100px;max-height:100px"/>'},getFilesInDir:function(t){this.listQuery.currentPage=1,this.listQuery.path=t.path,this.getList()},changeName:function(){this.listQuery.querySubDir=""!==this.listQuery.name.trim()},createFile:function(){var t=this,e=Object.assign({},this.file);this.paths.length>0&&(e.path=this.paths[this.paths.length-1].path+"/"+e.path),"true"===e.private?delete e.password:e.password&&""!==e.password&&(e.private=!1),Object(p["b"])(e).then((function(){t.getList(),t.createFileFormVisible=!1})).catch((function(){}))},beforeCreateFile:function(){this.createFileFormVisible=!0,this.file={type:"FILE"}},handleDelete:function(t,e){var n=this;this.$confirm("是否删除?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){Object(p["c"])(e.path).then((function(t){n.$message({type:"success",message:"删除成功!"}),n.getList()})).catch((function(){}))})).catch((function(){}))},beforeUploadFile:function(){this.uploadFormVisible=!0},afterUpload:function(){this.uploadFormVisible=!1,this.getList()},beforeFileUploadModalClose:function(){this.$refs.upload.abort(),this.$refs.upload.clearFiles()},expandChange:function(t,e){var n=this;if(this.fixed=0===e.length,e.length>0){var i,o=Object(r["a"])(e);try{for(o.s();!(i=o.n()).done;){var a=i.value;if(a.path===t.path){var l,s=Object(r["a"])(this.expandFiles);try{for(s.s();!(l=s.n()).done;){var c=l.value;if(c.path===t.path)return}}catch(u){s.e(u)}finally{s.f()}this.loadingExpandRow=!0,Object(p["e"])(a.path).then((function(t){n.expandFiles.push(t),n.loadingExpandRow=!1})).catch((function(){}));break}}}catch(u){o.e(u)}finally{o.f()}}},getExpandFile:function(t){var e,n=Object(r["a"])(this.expandFiles);try{for(n.s();!(e=n.n()).done;){var i=e.value;if(i.path===t.path)return[i]}}catch(o){n.e(o)}finally{n.f()}},handleCopy:function(t,e){f(t,e)},handleMove:function(t,e){var n=this;this.$prompt("请输入目标文件夹路径","提示",{confirmButtonText:"确定",cancelButtonText:"取消"}).then((function(t){var i=t.value;Object(p["f"])(e.path,{dirPath:i}).then((function(){n.$message({type:"success",message:"移动成功"}),n.getList()})).catch((function(){}))})).catch((function(){}))},handleRename:function(t,e){var n=this;this.$prompt("请输入新文件名(不包括后缀)","提示",{confirmButtonText:"确定",cancelButtonText:"取消",inputValue:e.name.substring(0,e.name.length-e.ext.length-1)}).then((function(t){var i=t.value;Object(p["f"])(e.path,{name:i}).then((function(){n.$message({type:"success",message:"重命名成功"}),n.getList()})).catch((function(){}))})).catch((function(){}))},handleCopyFile:function(t,e){var n=this;this.$prompt("请输入文件夹路径","提示",{confirmButtonText:"确定",cancelButtonText:"取消"}).then((function(t){var i=t.value;Object(p["a"])(e.path,i).then((function(){n.$message({type:"success",message:"拷贝成功"}),n.getList()})).catch((function(){}))})).catch((function(){}))},handleSecurity:function(t,e){this.file=Object.assign({},e),!0===this.file.private?this.file.private="true":this.file.private="false",this.securityVisible=!0},confirmSecurity:function(){var t=this,e={};"true"===this.file.private?e.private=!0:this.file.password&&(e.password=this.file.password),Object(p["f"])(this.file.path,e).then((function(){t.$message({type:"success",message:"操作成功"}),t.getList(),t.securityVisible=!1})).catch((function(){}))}}},w=_,x=n("2877"),k=Object(x["a"])(w,i,o,!1,null,null,null);e["default"]=k.exports}}]);