## urls

#### 常用方法
1. `String getFullUrl()`获取当前请求的完整地址
2. `CookieHelper getCookieHelper()`获取cookie辅助对象
3. `String getUnlockUrl(Lock lock, String redirectUrl)`根据锁对象和解锁后的跳转地址获取解锁地址
4. `String getSpace()`获取当前访问空间的别名，可能为null
5. `String getCurrentUrl()`获取当前空间的主页地址
6. `ArticlesUrlHelper getArticlesUrlHelper()`获取文章分页地址
7. `ArticlesUrlHelper getArticlesUrlHelper(String path)`根据指定的路径获取文章分页地址
8. `String getDomain()`获取配置的域名
9. `String getUrl()`获取系统主页地址
10. `String getUrl(Space space)`获取某个空间的主页地址
11. `String getUrl(Article article)`获取某篇文章的访问地址
12. `String getUrl(PathTemplate pathTemplate)`获取模板的访问地址
13. `String getUrl(News news)`获取动态的访问地址
14. `getUrl(String relativeUrl)`根据相对地址获取完整的访问地址
15. `NewsUrlHelper getNewsUrlHelper()`获取动态的分页地址
16. `NewsUrlHelper getNewsUrlHelper(String path)`根据指定的路径获取动态分页地址

### CookieHelper常用方法
1. `Cookie getCookie(String name)`根据名称获取某个cookie，如果cookie不存在返回null
2. `void setCookie(String name, String value, int maxAge)`根据名称、值和cookie存活时间设置一个cookie，如果cookie已经存在，则不进行任何操作
3. `void addCookie(String name, String value, int maxAge)`根据名称、值和cookie存活时间设置一个cookie，如果cookie已经存在，则更新
4. `void deleteCookie(String name)`根据名称删除cookie

### ArticlesUrlHelper常用方法
1. `String getArticlesUrl(Tag tag)`根据标签构造文章列表的访问地址
2. `String getArticlesUrl(String tag)`根据标签名称获取文章列表的访问地址
3. `String getArticlesUrl(ArticleQueryParam param, String sortStr)`根据当前查询参数和排序值获取文章的分页地址
4. `String getArticlesUrl(ArticleQueryParam param, int page)`根据当前查询参数和指定页面获取文章的分页地址
5. `String getArticlesUrl(Date begin, Date end)`根据开始日期和结束日期获取文章的分页地址
6. `String getArticlesUrl(String begin, String end)`根据开始日期和结束日期获取文章的分页地址

### NewsUrlHelper常用方法
1. `String getNewsUrl(NewsArchivePageQueryParam param, int page)`根据当前查询参数和指定页面获取文章的分页地址
2. `String getNewsUrl(String ymd)`根据指定年月日获取文章的分页地址