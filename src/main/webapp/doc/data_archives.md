## archives

### 完整写法
```
<data name="archives" currentPage="" pageSize="" queryPrivate="" ymd="" ignorePaging=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| currentPage   | 当前页   | 否   | int   |  1  |    
|  pageSize  |  每页显示数量  |  否  | int   |    |    
|  queryPrivate  | 是否查询私人文章，只有在用户获得登录授权后这个属性才会生效   | 否   |  [true,false]  |    |      
|  ymd  |  当前页的开始日期  |  否  | yyyy-MM-dd格式的字符串   |    |
|  ignorePaging  |  是否忽略分页，查询全部  |  否  |[true,false]   | true   |

### 默认DataName
archives

### 返回值
`PageResult<ArticleArchive>`，**不为null**

### 返回值常用属性和方法

#### ArticleArchive
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  ymd  |  年月日  |  否  | yyyy-MM-dd格式的字符串   |    |
|  articles  | 文章集合  |  否  | List<Article&gt;   |    |

### Article
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |  
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


#### ArticleArchivePageQueryParam
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  queryPrivate  | 是否查询私人文章   | 否   |  [true,false]  |    |      
|  ymd  |  当前页的开始日期  |  否  | yyyy-MM-dd格式的字符串   |    |
|  ignorePaging  |  是否忽略分页，查询全部  |  否  |[true,false]   | true   |

#### PageResult
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
| datas   | 获取分页数据   | 否   | List<ArticleArchive&gt;| 
| totalPage   | 获取总页码数   | 否   |int| 
| currentPage   | 获取当前页码   | 否   |int|  
| listbegin   | 获取分页起始页码   | 否   |int| 
| listend   | 获取分页结束页码   | 否   |int|     
| pageSize   | 获取每页显示数目  | 否   |int|     
| totalRow   | 获取总数据数  | 否   |int|     
| param   | 获取查询条件  | 否   |ArticleArchivePageQueryParam| 

|  方法名称  |说明| 返回值  | 返回值是否可能为null  |    
|  -  |  -  |  -  |  -  |  -  |
| hasNext()   | 是否有下一页   | boolean   | 否| 
| hasPrevious()   | 是否有上一页   | boolean   | 否|