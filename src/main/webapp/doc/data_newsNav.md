## 上一条下一条动态

### 完整写法
```
<data name="newsNav" queryLock="" id="" ref-news=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| id   | 动态   | 是   | int   |  |
| queryLock   | 是否查询被锁保护的动态   | 否   | [true,false] |false| 
| ref-news   | 上下问中存在的动态   | 否   | |   

### 默认DataName
newsNav

### 返回值
`NewsNav`，如果动态没有上下动态，返回null，否则不为null

### 返回值常用属性和方法

#### Space
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  previous  |  上一条动态  |  是  | News   | 
|  next  |  下一条动态  |  是  | News   | 

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