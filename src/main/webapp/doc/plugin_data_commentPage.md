## 评论列表

### 完整写法
```
<data name="commentPage" moduleType="" moduleId="" mode="" currentPage="" pageSize=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| moduleType | 模块类型  | 是   | string| |  
| moduleId | 模块ID  | 是   | Integer ||
| mode | 返回形式  | 否   | enum['LIST', 'TREE']||
| currentPage | 当前页  | 否   | int|1 | 
| pageSize | 每页显示数目  | 否   | int||

### 默认DataName
commentsPage

### 返回值
`CommentPageResult` 不为null

### 返回值常用属性和方法

#### CommentPageResult

**继承自PageResult<Comment&gt;**

|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  commentConfig  |  评论配置  |  否  | CommentConfig   | 
|  checkCount  | 正在审核的评论数量  |  否  | int   | 

### CommentConfig
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  editor  | 评论编辑器类型   | 否   |  enum['HTML','MD']  |  
|  check  | 是否需要审核评论   | 否   |  boolean  |     
|  pageSize  | 每页最多评论数   | 否   |  int  |       
|  nickname  | 管理员评论昵称   | 否   |  string  |   


### Comment
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |  
|  content  | 评论内容  | 否   |  string  |
|  admin  | 是否是管理员评论  | 否   |  boolean  |
|  ban  | IP是否被禁止评论  | 否   |  boolean  |
|  commentDate  | 评论日期  | 否   |  Timestamp  |
|  commentModule  | 评论模块  | 否   |  CommentModule  |
|  ip  | 评论IP  | 是，这个属性只有在获得登录授权的情况下才不为null  |是|
|  nickname  | 昵称  | 否   |  string  |
|  status  | 评论状态  | 否   |  enum(['NORMAL','CHECK'])  |
|  url  | 评论模块所在地址  | 否   |  string  |
|  website  | 评论人网址  | 是   |  string  |
|  email  | 评论人邮箱  | 是，这个属性只有在获得登录授权的情况下才不为null|  string  |
|  gravatar  | 评论人邮箱md5地址  | 是   |  string  |
|  parents  | 父评论ID集合  | 否   |  List<Integer&gt;  |

 ### CommentModule
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  module  | 模块类型   | 否   |  string  |
|  id  | 模块ID  | 否   |  Integer  |


#### CommentQueryParam
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
| mode | 返回形式  | 否   | enum['LIST', 'TREE']||
| asc | 是否按照评论时间正排序  | 否   | boolean||
| status | 评论状态  | 是   | enum['CHECK','NORMAL']||
| module | 评论模块  | 否   | CommentModule||

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