## articleNav

### 完整写法
```
<data name="articleNav" queryLock="" idOrAlias="" ref-article=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| idOrAlias   | 文章当ID或者别名   | 是   | string   |  |
| queryLock   | 是否查询被锁保护的文章   | 否   | [true,false] |false| 
| ref-article   | 上下问中存在的文章   | 否   | |   

### 默认DataName
articleNav

### 返回值
`ArticleNav` 如果文章没有上一片下一篇文章，返回null,否则不为null

### 返回值常用属性和方法

### ArticleNav
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  previous  | 上一篇文章   | 是   |  Article  |  
|  next  | 下一篇文章  | 是   |  Article  | 


### Article
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |      
|  title  | 文章标题   | 否   |  string  |   
|  space  | 空间 | 否   | Space  |
|  lockId  | 文章锁ID | 是   | String  |
|  alias  | 文章别名 | 是   | String  |
|  featureImage  | 文章特征图像地址| 是   | String  |
 
 ### Space
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  id  | 文章ID   | 否   |  int  |  
|  name  | 空间名称   | 否   |  string  |
|  alias  | 空间别名   | 否   |  string  |

