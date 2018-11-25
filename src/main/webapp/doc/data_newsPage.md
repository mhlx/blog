## 动态列表

### 完整写法
```
<data name="newsPage" currentPage="" pageSize="" queryPrivate="" ymd="" asc=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| currentPage   | 当前页   | 否   | int   |  1  |    
|  pageSize  |  每页显示数量  |  否  | int   |    |    
|  queryPrivate  | 是否查询私人文章，只有在用户获得登录授权后这个属性才会生效   | 否   |  [true,false]  |    |      
|  ymd  |  当前页的开始日期  |  否  | yyyy-MM-dd格式的字符串   |    |
|  asc  |  是否按照时间正序排列  |  否  |[true,false]   | false   |

### 默认DataName
newsPage

### 返回值
`PageResult<NewsArchive>`，**不为null**

### 返回值常用属性和方法

#### NewsArchive
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  ymd  |  年月日  |  否  | yyyy-MM-dd格式的字符串   |    |
|  newses  | 动态集合  |  否  | List<News&gt;   |    |

#### News
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  content  |  动态内容  |  否  | string  | 
|  write  | 动态发布日期  |  否  | Timestamp  |
|  update  | 动态修改日期  |  是  | Timestamp  |
|  isPrivate  | 是否是私人动态  |  否  | boolean  |
|  comments  | 动态评论数目 |  否  | int  |
|  allowComment  | 动态是否允许评论 |  否  | boolean  |
|  hits  | 动态点击数量 |  否  | int  |
|  lockId  | 动态锁ID |  是  | string  |


#### NewsArchivePageQueryParam
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  queryPrivate  | 是否查询私人文章   | 否   |  [true,false]  |      
|  ymd  |  当前页的开始日期  |  否  | yyyy-MM-dd格式的字符串   | 
|  content  |  动态内容  |  是  |string   |  
|  asc  |  是否按照时间动态排序  |  否  |boolean   | 

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