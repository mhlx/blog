## jsoups

### 常用方法

1.	 `Document body(String bodyHtml,String baseUri)`将html文本转化为`Jsoup`的文本对象
2.  `Document html(String html,String baseUri)` 将html文本转化为`Jsoup`的文本对象
3.  `Document body(String bodyHtml)`同`body(bodyHtml,'')`
4.  `Document html(String html)` 同`html(bodyHtml,'')`
5.	 `DocumentParser parser(Document document)`将`Document`转化为`DocumentParser`对象

### DocumentParser

#### 常用方法
1.  `String getText()`获取纯文本内容
2.  `List<String> getImages()`获取所有图片的访问地址(**仅获取&lt;img src=""/&gt;格式的图片**)
3.  `Optional<String> getImage()`获取第一张图片的访问地址
4.  `List<String> getVideoPosters()`获取所有视频的封面图片地址(**仅获取&lt;video poster=""/&gt;格式的视频**)
5.  `Optional<String> getVideoPoster()`获取第一个视频的封面图片地址
6.  `Document getDocument()`获取`Document`对象