package me.qyh.blog.plugin.markdowniteditor;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import me.qyh.blog.core.plugin.PluginHandlerSupport;
import me.qyh.blog.core.plugin.PluginProperties;
import me.qyh.blog.core.util.Jsons;
import me.qyh.blog.core.vo.JsonResult;

/**
 * 利用和nodejs的交互来解析markdown<br>
 * 安装:
 * 
 * <pre>
 * npm install markdown-it --save
 * npm install markdown-it-footnote --save
 * npm install @iktakahiro/markdown-it-katex --save
 * npm install markdown-it-task-lists --save
 * npm install markdown-it-sup --save
 * npm install markdown-it-sub --save
 * npm install markdown-it-abbr --save
 * </pre>
 * 
 * 启动：
 * 
 * <pre>
 * node / path / to / app.js
 * </pre>
 * 
 * app.js:
 * 
 * <pre>
 * const http = require('http');

const hostname = '127.0.0.1';
const port = 3000;

var md = require('markdown-it')({
	html : true,
	linkify: true,
	typographer: true
}).use(require('markdown-it-footnote'))
.use(require('@iktakahiro/markdown-it-katex'))
.use(require("markdown-it-task-lists"))
.use(require('markdown-it-sup'))
.use(require('markdown-it-sub'))
.use(require('markdown-it-abbr'))
;

const server = http.createServer((req, res) => {
  if(req.method == 'POST'){
	var body = '';
	req.on('data', function (data) {
		body += data;
	});
	req.on('end', function () {
		body = md.render(body,{});
		var json = JSON.stringify({ 
			success: true, 
			data: body
		});
		
		res.writeHead(200, {'Content-Type': 'application/json'});
		res.end(json);
	});
  }else{
	  
	  var json = JSON.stringify({ 
		success: false, 
		message: "post only"
	  });
	  
	  res.writeHead(200, {'Content-Type': 'application/json'});
	  res.end(json);
  }
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});
 * </pre>
 * 
 * 这样通过 post http://localhost:3000 即可获取转化后的文本
 * 
 * 
 * @author wwwqyhme
 *
 */
public class MarkdownItEditorPluginHandler extends PluginHandlerSupport {

	private static final String ENABLE_KEY = "plugin.markdownit.enable";
	private static final String URL_KEY = "plugin.markdownit.url";
	private static final String EDITOR_ALWAYS = "plugin.markdownit.editor.always";

	private final PluginProperties pluginProperties = PluginProperties.getInstance();

	private final Logger logger = LoggerFactory.getLogger(MarkdownItEditorPluginHandler.class);

	private boolean serviceAvailable = false;

	@Override
	protected void registerBean(BeanRegistry registry) {
		Optional<String> opUrl = pluginProperties.get(URL_KEY);
		serviceAvailable = opUrl.isPresent() ? isServiceAvailable(opUrl.get()) : false;
		if (serviceAvailable) {
			registry.register(MarkdownItMarkdown2Html.class.getName(),
					BeanDefinitionBuilder.genericBeanDefinition(MarkdownItMarkdown2Html.class)
							.setScope(BeanDefinition.SCOPE_SINGLETON).addConstructorArgValue(opUrl.get())
							.getBeanDefinition());
		}
	}

	@Override
	protected void registerChildBean(BeanRegistry registry) {
		if (!pluginProperties.get(EDITOR_ALWAYS).map(Boolean::parseBoolean).orElse(true) && !serviceAvailable) {
			return;
		}
		// 代理系统本来的编辑文章|撰写文章的路径，指向新的markdown编辑器路径
		registry.register(ArticleEditorAspect.class.getName(), simpleBeanDefinition(ArticleEditorAspect.class));
	}

	private boolean isServiceAvailable(String url) {
		try {
			String json = Https.getIns().post(url, Jsons.write(Map.of(1, "# test 你好")));
			Jsons.readValue(JsonResult.class, json);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn("尝试转化markdown失败：" + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean enable() {
		return pluginProperties.get(ENABLE_KEY).map(Boolean::parseBoolean).orElse(false);
	}

}
