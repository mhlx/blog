## jsons

### 常用方法

1.  `Gson getGson()`获取`Gson`对象
2.  `String write(Object toWrite)` 将对象转化为json字符串
3.  `ExpressionExecutor read(String url)`将地址中的返回内容转化为`ExpressionExecutor`对象
4.  `ExpressionExecutors readForExecutors(String url)` 将地址中的内容转化为`ExpressionExecutors`对象
5.  `ExpressionExecutor readJson(String json)`将json字符串转化为`ExpressionExecutor`对象
6.  `ExpressionExecutors readJsonForExecutors(String json)`将json字符串转化为`ExpressionExecutors`对象

## ExpressionExecutor
用于更方便的从json字符串中取值

### 常用方法
1. `ExpressionExecutor executeForExecutor(String expression)`执行一个表达式，返回一个`ExpressionExecutor`对象
2. `ExpressionExecutors executeForExecutors(String expression)`执行一个表达式，返回一个
`ExpressionExecutors`对象
3. `Optional<String> execute(String expression)`执行一个表达式，返回一个值对象
4. `Optional<String> get()`等同于`Optional<String> execute(null)`返回当前`ExpressionExecutor`的值
5. `boolean isNull()`判断`ExpressionExecutor`是否为空


## ExpressionExecutors
用于更方便的从json数组中取值
1. `int size()`获取数组的大小
2. `ExpressionExecutor getExpressionExecutor(int index)`获取指定位置的`ExpressionExecutor`
