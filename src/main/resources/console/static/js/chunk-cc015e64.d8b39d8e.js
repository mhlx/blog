(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-cc015e64"],{"0827":function(t,e,a){"use strict";var n=a("0ead"),i=a.n(n);i.a},"09f4":function(t,e,a){"use strict";a.d(e,"a",(function(){return l})),Math.easeInOutQuad=function(t,e,a,n){return t/=n/2,t<1?a/2*t*t+e:(t--,-a/2*(t*(t-2)-1)+e)};var n=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(t){window.setTimeout(t,1e3/60)}}();function i(t){document.documentElement.scrollTop=t,document.body.parentNode.scrollTop=t,document.body.scrollTop=t}function r(){return document.documentElement.scrollTop||document.body.parentNode.scrollTop||document.body.scrollTop}function l(t,e,a){var l=r(),c=t-l,o=20,s=0;e="undefined"===typeof e?500:e;var u=function t(){s+=o;var r=Math.easeInOutQuad(s,l,c,e);i(r),s<e?n(t):a&&"function"===typeof a&&a()};u()}},"0ead":function(t,e,a){},2423:function(t,e,a){"use strict";a.d(e,"c",(function(){return i})),a.d(e,"b",(function(){return r})),a.d(e,"f",(function(){return l})),a.d(e,"a",(function(){return c})),a.d(e,"d",(function(){return o})),a.d(e,"e",(function(){return s}));var n=a("b775");function i(t){return Object(n["a"])({url:"/articles",method:"get",params:t})}function r(t){return Object(n["a"])({url:"/articles/"+t,method:"delete"})}function l(t){return Object(n["a"])({url:"/articles/"+t.id,method:"put",data:t,showError:!1})}function c(t){return Object(n["a"])({url:"/article",method:"post",data:t,showError:!1})}function o(t){return Object(n["a"])({url:"/editableArticles/"+t,method:"get"})}function s(t){return Object(n["a"])({url:"/articles/"+t,method:"get"})}},"3f5e":function(t,e,a){"use strict";a.d(e,"d",(function(){return i})),a.d(e,"b",(function(){return r})),a.d(e,"e",(function(){return l})),a.d(e,"c",(function(){return c})),a.d(e,"f",(function(){return o})),a.d(e,"a",(function(){return s}));var n=a("b775");function i(t){return Object(n["a"])({url:"/files",method:"get",params:t})}function r(t){return Object(n["a"])({url:"/file",method:"post",data:t})}function l(t){return Object(n["a"])({url:"file",method:"get",params:{path:t}})}function c(t){return Object(n["a"])({url:"file",method:"delete",params:{path:t}})}function o(t,e){return Object(n["a"])({url:"file",method:"patch",params:{path:t},data:e})}function s(t,e){return Object(n["a"])({url:"file",method:"post",params:{path:t,dir:e}})}},"45a5":function(t,e,a){"use strict";a.r(e);var n=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"app-container"},[a("el-form",{attrs:{model:t.article},nativeOn:{submit:function(t){t.preventDefault()}}},[a("el-form-item",[a("el-button",{attrs:{type:"primary"},on:{click:t.backToList}},[t._v("返回")]),t._v(" "),a("el-button",{attrs:{type:"primary"},on:{click:t.confirmArticle}},[t._v("保存")])],1),t._v(" "),a("el-form-item",{attrs:{label:"标题"}},[a("el-input",{model:{value:t.article.title,callback:function(e){t.$set(t.article,"title",e)},expression:"article.title"}})],1),t._v(" "),a("markdown-editor",{ref:"editor",staticStyle:{"margin-bottom":"20px"},attrs:{"preview-style":"tab",height:"600px"},on:{contentChanged:t.onContentChange}}),t._v(" "),a("el-form-item",{attrs:{label:"摘要"}},[a("markdown-editor",{ref:"summaryEditor",staticStyle:{width:"100%",clear:"both"},attrs:{"preview-style":"tab"},on:{contentChanged:t.onContentChange}})],1),t._v(" "),a("el-form-item",{attrs:{label:"别名"}},[a("el-input",{model:{value:t.article.alias,callback:function(e){t.$set(t.article,"alias",e)},expression:"article.alias"}})],1),t._v(" "),a("el-form-item",{attrs:{label:"分类"}},[a("el-select",{attrs:{multiple:"",placeholder:"请选择"},model:{value:t.article.categories,callback:function(e){t.$set(t.article,"categories",e)},expression:"article.categories"}},t._l(t.categories,(function(t){return a("el-option",{key:t.id,attrs:{label:t.name,value:t.id}})})),1)],1),t._v(" "),a("el-form-item",{attrs:{label:"标签","label-position":"top"}},[t._l(t.article.tags,(function(e){return a("el-tag",{key:e.name,attrs:{closable:"","disable-transitions":!1},on:{close:function(a){return t.handleClose(e.name)}}},[t._v("\n        "+t._s(e.name)+"\n      ")])})),t._v(" "),t.inputVisible?a("el-input",{ref:"saveTagInput",staticClass:"input-new-tag",attrs:{size:"small"},on:{blur:t.handleInputConfirm},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.handleInputConfirm(e)}},model:{value:t.inputValue,callback:function(e){t.inputValue=e},expression:"inputValue"}}):a("el-button",{staticClass:"button-new-tag",attrs:{size:"small"},on:{click:t.showInput}},[t._v("新标签")])],2),t._v(" "),a("el-form-item",{attrs:{label:"特征图像"}},[a("el-input",{model:{value:t.article.featureImage,callback:function(e){t.$set(t.article,"featureImage",e)},expression:"article.featureImage"}},[a("el-button",{attrs:{slot:"append",icon:"el-icon-search"},on:{click:t.selectFile},slot:"append"})],1)],1),t._v(" "),a("el-form-item",{attrs:{label:"状态"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:t.onStatusChange},model:{value:t.article.status,callback:function(e){t.$set(t.article,"status",e)},expression:"article.status"}},[a("el-option",{key:"PUBLISHED",attrs:{label:"发布",value:"PUBLISHED"}}),t._v(" "),a("el-option",{key:"DRAFT",attrs:{label:"草稿",value:"DRAFT"}}),t._v(" "),a("el-option",{key:"SCHEDULED",attrs:{label:"计划",value:"SCHEDULED"}})],1)],1),t._v(" "),t.scheduleDateVisible?a("el-form-item",{attrs:{label:"发布时间"}},[a("el-date-picker",{attrs:{type:"datetime",placeholder:"选择日期时间"},model:{value:t.article.pubDate,callback:function(e){t.$set(t.article,"pubDate",e)},expression:"article.pubDate"}})],1):t._e(),t._v(" "),a("el-form-item",{attrs:{label:"置顶级别"}},[a("el-input-number",{attrs:{min:1,max:10},model:{value:t.article.level,callback:function(e){t.$set(t.article,"level",e)},expression:"article.level"}})],1),t._v(" "),a("el-form-item",{attrs:{label:"访问密码"}},[a("el-input",{model:{value:t.article.password,callback:function(e){t.$set(t.article,"password",e)},expression:"article.password"}})],1),t._v(" "),a("el-form-item",{attrs:{label:"允许评论"}},[a("el-switch",{attrs:{"active-color":"#13ce66","inactive-color":"#ff4949","active-value":"true","inactive-value":"false"},model:{value:t.article.allowComment,callback:function(e){t.$set(t.article,"allowComment",e)},expression:"article.allowComment"}})],1),t._v(" "),a("el-form-item",{attrs:{label:"私有"}},[a("el-switch",{attrs:{"active-color":"#13ce66","inactive-color":"#ff4949","active-value":"true","inactive-value":"false"},model:{value:t.article.isPrivate,callback:function(e){t.$set(t.article,"isPrivate",e)},expression:"article.isPrivate"}})],1)],1),t._v(" "),a("file-selector",{ref:"fileSelector",attrs:{mode:"file",single:!0,exts:["jpeg","jpg","gif","png"]},on:{selected:t.selectedFile}})],1)},i=[],r=(a("7f7f"),a("3e82")),l=a("2423"),c=a("c405"),o=a("16d8"),s=a("a18c"),u=a("4360"),f=a("ed08"),d={allowComment:"true",isPrivate:"false",tags:[],status:"PUBLISHED",featureImage:""},m={name:"ArticleWrite",components:{MarkdownEditor:o["a"],FileSelector:r["a"]},data:function(){return{article:{},categories:[],tag:"",inputVisible:!1,inputValue:"",scheduleDateVisible:!1}},watch:{article:{handler:function(t,e){!0!==this.first?this.onContentChange():this.first=!1},deep:!0}},created:function(){var t=this;this.article=Object.assign({},d),this.first=!0,Object(c["c"])().then((function(e){t.categories=e,0===t.categories.length&&(t.$message({type:"error",message:"创建文章需要至少一个分类，请先添加一个分类"}),t.backToList())})).catch((function(){}))},methods:{onContentChange:function(){var t=this.$refs.editor.getMarkdown();if(""!==t||this.timer){this.timer&&clearTimeout(this.timer);var e=this;this.timer=setTimeout((function(){var a=Object.assign({},e.article);a.content=t,a.summary=e.$refs.summaryEditor.getMarkdown(),a.status="DRAFT",delete a.pubDate,e.article.id?Object(l["f"])(a).then((function(){})).catch((function(t){})):Object(l["a"])(a).then((function(t){e.article.id=t})).catch((function(t){}))}),500)}},confirmArticle:function(){var t=this,e=Object.assign({},this.article);e.content=this.$refs.editor.getMarkdown(),e.summary=this.$refs.summaryEditor.getMarkdown(),"SCHEDULED"===e.status?e.pubDate&&(e.pubDate=Object(f["f"])(e.pubDate)):delete e.pubDate;var a=e.id?Object(l["f"])(e):Object(l["a"])(e);a.then((function(){t.$message({type:"success",message:"保存成功"}),t.backToList()})).catch((function(e){t.$message({type:"error",dangerouslyUseHTMLString:!0,message:e.message})}))},backToList:function(){u["a"].dispatch("tagsView/delView",this.$route),s["b"].push("/article/index")},handleClose:function(t){for(var e=this.article.tags.length-1;e>=0;e--)if(this.article.tags[e].name===t){this.article.tags.splice(e,1);break}},showInput:function(){var t=this;this.inputVisible=!0,this.$nextTick((function(e){t.$refs.saveTagInput.$refs.input.focus()}))},handleInputConfirm:function(){var t=this.inputValue.trim();if(""!==t){var e=this.article.tags.filter((function(e){return e.name===t}));0===e.length&&this.article.tags.push({name:t})}this.inputVisible=!1,this.inputValue=""},selectFile:function(){this.$refs.fileSelector.select()},selectedFile:function(t){t.length>0&&(this.article.featureImage=t[0].url)},onStatusChange:function(){this.scheduleDateVisible="SCHEDULED"===this.article.status}}},p=m,h=(a("0827"),a("2877")),b=Object(h["a"])(p,n,i,!1,null,null,null);e["default"]=b.exports},"46c7":function(t,e,a){t.exports=a.p+"static/img/file.c1cdb1da.svg"},abfe:function(t,e,a){t.exports=a.p+"static/img/folder.f4979b25.svg"},c405:function(t,e,a){"use strict";a.d(e,"c",(function(){return i})),a.d(e,"a",(function(){return r})),a.d(e,"d",(function(){return l})),a.d(e,"b",(function(){return c}));var n=a("b775");function i(){return Object(n["a"])({url:"/categories",method:"get"})}function r(t){return Object(n["a"])({url:"/category",method:"post",data:t})}function l(t){return Object(n["a"])({url:"/categories/"+t.id,method:"put",data:t})}function c(t){return Object(n["a"])({url:"/categories/"+t,method:"delete"})}}}]);