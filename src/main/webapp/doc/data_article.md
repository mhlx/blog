## article

### 完整写法
```
<data name="article" ignoreException="" idOrAlias=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| idOrAlias   | 文章当ID或者别名   | 是   | string   |    |    
|  ignoreException  |  当文章不存在时，是否忽略异常  |  否  | [true,false]   |  false  |  

### 默认DataName
article

### 返回值
`Article`如果 ignoreException属性值为true，那么返回值可能为null,否则不为null

### 返回值常用属性和方法

### Article
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |  
|  content  | 文章内容   | 否   |  string  | 
|  pubDate  | 文章发布日期   | 否   |  Timestamp  |   
|  lastModifyDate  | 文章最后修改日期   | 是   |  Timestamp  |     
|  title  | 文章标题   | 否   |  string  |         
|  isPrivate  | 是否是私人文章   | 否   |  boolean  |  
|  hits  | 文章点击量  | 否   |  int  | 
|  comments  | 文章评论数 | 否   |  int  | 
|  summary  | 文章摘要 | 否  |  string  | 
|  level  | 文章置顶级别 | 是   |  Integer  |
|  editor  | 文章编辑器 | 否   |  enum [ 'MD','HTML']  |
|  from  | 文章来源 | 否   | enum [ 'ORIGINAL','COPIED']  |
|  space  | 空间 | 否   | Space  |
|  lockId  | 文章锁ID | 是   | String  |
|  alias  | 文章别名 | 是   | String  |
|  featureImage  | 文章特征图像地址| 是   | String  |
|  tags  | 文章标签| 否   | List<Tag&gt;  |

|  方法名称  |说明| 返回值  | 返回值是否可能为null  |    
|  -  |  -  |  -  |  -  |  -  |
| hasLock()   | 文章是否受到锁保护   | boolean   | 否| 
| hasTag(String tag)   | 文章是否有某个标签   | boolean   | 否| 
| getTag(String name)  | 获取文章的某个标签   | Optional<String&gt;   | 否| 
| getTagStr()   | 输出文章的所有标签，以,分隔   | boolean   | 否| 
 
 ### Space
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |  
|  name  | 空间名称   | 否   |  string  |
|  alias  | 空间别名   | 否   |  string  |
|  lockId  | 空间锁ID   | 是   |  string  |
|  isPrivate  | 是否是私人空间   | 否   |  [true,false]  |

 ### Tag
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  name  | 标签名称   | 否   |  string  |