## times

### 常用方法

1.  `LocalDateTime now()`获取当前时间
2.  `long nowMillis()` 获取当前时间戳
3.  `LocalDateTime parseAndGet(String text)`解析日期，如果解析失败，返回null
4.  `Optional<LocalDateTime> parse(String text, String pattern)` 解析日期，通过pattern可以指定日期格式
5.  `Optional<LocalDateTime> parse(String text)`解析日期
6.  `String format(Temporal temporal, String pattern)`格式化日期
7.  `String format(Date date, String pattern)`格式化日期
8.  `LocalDateTime toLocalDateTime(Date date)`将`Date`转化为`LocalDateTime`
9.  `Date parseAndGetDate(String text)`解析日期，返回`Date`格式
10.  `Date toDate(LocalDateTime time)` 将`LocalDateTime`转化为`Date`
11.  `int getYear(Temporal temporal)`获取日期中的年份
12.  `int getMonthOfYear(Temporal temporal)`获取日期中的月份
13.  `int getDayOfMonth(Temporal temporal)`获取日期中的天数
14.  `int getYear(Date date)`获取日期中的年份
15.  `int getMonthOfYear(Date date)`获取日期中的月份
16.  `int getDayOfMonth(Date date)`获取日期中的天数
17.  `long getTime(Date date)`将日期转化为时间戳
18.  `long getTime(LocalDateTime date)`将日期转化为时间戳

### 默认可被接受的日期格式
1. yyyyMMdd  
2. yyyy-MM-dd   
3. yyyy/MM/dd  
4. yyyy-MM-dd HH:mm:ss  
5. yyyy-MM-dd HH:mm
6. yyyy-MM-dd HH
7. yyyy/MM/dd HH:mm:ss
8. yyyy/MM/dd HH:mm