## 最近被访问文章

### 完整写法
```
<data name="recentlyViewdArticles" num=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| num | 最多返回数目  | 否   | int| |

### 默认DataName
recentlyViewdArticles

### 返回值
`List<HitsHistory>` 不为null

### 返回值常用属性和方法


### HitsHistory

**继承自Article**

|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |  
|  pubDate  | 文章发布日期   | 否   |  Timestamp  |   
|  lastModifyDate  | 文章最后修改日期   | 是   |  Timestamp  |     
|  title  | 文章标题   | 否   |  string  |         
|  isPrivate  | 是否是私人文章   | 否   |  boolean  |  
|  hits  | 文章点击量  | 否   |  int  | 
|  comments  | 文章评论数 | 否   |  int  | 
|  level  | 文章置顶级别 | 是   |  Integer  |
|  editor  | 文章编辑器 | 否   |  enum [ 'MD','HTML']  |
|  from  | 文章来源 | 否   | enum [ 'ORIGINAL','COPIED']  |
|  space  | 空间 | 否   | Space  |
|  lockId  | 文章锁ID | 是   | String  |
|  alias  | 文章别名 | 是   | String  |
|  featureImage  | 文章特征图像地址| 是   | String  |
|  tags  | 文章标签| 否   | List<Tag&gt;  |
|  ip  | 访问人IP| 否   | string |
|  time  | 访问时间| 否   | Timestamp |

|  方法名称  |说明| 返回值  | 返回值是否可能为null  |    
|  -  |  -  |  -  |  -  |  -  |
| hasLock()   | 文章是否受到锁保护   | boolean   | 否| 
| hasTag(String tag)   | 文章是否有某个标签   | boolean   | 否| 
| getTag(String name)  | 获取文章的某个标签   | Optional<String&gt;   | 否| 
| getTagStr()   | 输出文章的所有标签，以,分隔   | boolean   | 否| 