## 文章查询提示

### 完整写法
```
<data name="articles" query="" max=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| query   | 查询内容   | 是   | string   |    |    
|  max  |  最多返回条数  |  否  | int   |  5  | 

### 默认DataName
articles

### 返回值
`List<Article>` 不为null

### 返回值常用属性和方法

#### Article
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  id  |  文章ID  |  否  | int   |
|  title  |  文章标题  |  否  | string   |
|  alias  |  文章别名  |  是  | string   |

 ### Space
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  alias  | 空间别名   | 否   |  string  |