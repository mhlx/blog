package me.qyh.blog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.qyh.blog.utils.StreamUtils;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DefaultMarkdown2Html implements Markdown2Html {

    private final Markdown2Html delegate;
    private final ObjectMapper mapper;

    public DefaultMarkdown2Html(BlogProperties blogProperties, ObjectMapper mapper) {
        super();
        this.mapper = mapper;
        if (StringUtils.hasText(blogProperties.getMarkdownServiceUrl())) {
            this.delegate = new MarkdownItConverter(blogProperties.getMarkdownServiceUrl());
        } else {
            this.delegate = new CommonMarkdown2Html();
        }
    }

    @Override
    public Map<Integer, String> toHtmls(Map<Integer, String> markdownMap) {
        return delegate.toHtmls(markdownMap);
    }

    @Override
    public String toHtml(String markdown) {
        return delegate.toHtml(markdown);
    }

    static class CommonMarkdown2Html implements Markdown2Html {

        private final Parser parser;
        private final HtmlRenderer renderer;

        private CommonMarkdown2Html() {
            List<Extension> baseExtensions = List.of(AutolinkExtension.create(), TablesExtension.create(),
                    StrikethroughExtension.create(), HeadingAnchorExtension.create());
            parser = Parser.builder().extensions(baseExtensions).build();
            renderer = HtmlRenderer.builder().extensions(baseExtensions).build();
        }

        @Override
        public String toHtml(String markdown) {
            if (markdown == null) {
                return "";
            }
            Node document = parser.parse(markdown);
            return renderer.render(document);
        }

        @Override
        public Map<Integer, String> toHtmls(Map<Integer, String> markdownMap) {
            Map<Integer, String> map = new HashMap<>();
            for (Map.Entry<Integer, String> it : markdownMap.entrySet()) {
                map.put(it.getKey(), toHtml(it.getValue()));
            }
            return map;
        }

    }

    class MarkdownItConverter implements Markdown2Html {

        private final String url;

        public MarkdownItConverter(String url) {
            super();
            this.url = url;
        }

        @Override
        public String toHtml(String markdown) {
            if (markdown == null) {
                return "";
            }
            return toMap(Map.of(1, markdown)).get(1);
        }

        @Override
        public Map<Integer, String> toHtmls(Map<Integer, String> markdownMap) {
            return toMap(markdownMap);
        }

        private String post(String uri, String data) throws IOException {
            URL url = new URL(uri);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            try (OutputStream os = http.getOutputStream();
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
                bw.write(data);
            }
            try (InputStream is = http.getInputStream()) {
                return StreamUtils.toString(is);
            }
        }

        private Map<Integer, String> toMap(Map<Integer, String> markdownMap) {
            try {
                String json = post(url, mapper.writeValueAsString(markdownMap));
                JsonNode node = mapper.readTree(json);
                if (node.get("success").asBoolean()) {
                    JsonNode dataNode = node.get("data");
                    return mapper.convertValue(dataNode, new TypeReference<>() {
                    });
                }
                throw new RuntimeException("fail to convert markdown to html:" + node);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

    }
}
