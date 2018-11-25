## 最近评论

### 完整写法
```
<data name="lastComments" moduleType="" limit="" queryAdmin=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| moduleType | 模块类型  | 是   | string| |  
| limit | 最多返回条数  | 是   | Integer |10|
| queryAdmin | 是否查询管理员的评论或回复  | 否   | boolean|false|

### 默认DataName
lastComments

### 返回值
`List<Comment>` 不为null

### 返回值常用属性和方法


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