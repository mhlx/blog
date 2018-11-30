## 评论统计

### 完整写法
```
<data name="commentStatistics"/>
```


### 默认DataName
commentStatistics

### 返回值
`CommentStatistics` 不为null

### 返回值常用属性和方法

#### CommentStatistics
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  modules  | 模块详情集合  |  否  | List<CommentModuleStatistics&gt;   |
|  total  | 评论总数  |  否  | int   |

### CommentModuleStatistics
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  type  | 模块类型  |  否  | string   |
|  count  | 评论数量  |  否  | int   |
|  name  | 模块名称  |  否  | Message   |
