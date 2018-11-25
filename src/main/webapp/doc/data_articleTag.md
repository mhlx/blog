## 文章标签引用数目

### 完整写法
```
<data name="articleTags"/>
```

### 默认DataName
articleTags

### 返回值
`List<TagCount>` 不为null

### 返回值常用属性和方法

#### TagCount
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  tag  | 标签  |  否  | Tag   |
|  count  | 标签被引用的数量  |  否  | int   |

#### Tag
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  id  | 标签ID  |  否  | int   |
|  name  | 标签名  |  否  | name   |
|  create  | 标签创建日期 |  否  | Timestamp   |
