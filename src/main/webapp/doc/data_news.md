## news

### 完整写法
```
<data name="news" ignoreException="" id=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| id   | 动态ID   | 是   | string   |    |    
|  ignoreException  |  当动态不存在时，是否忽略异常  |  否  | [true,false]   |  false  |  

### 默认DataName
news

### 返回值
`News`如果 ignoreException属性值为true，那么返回值可能为null,否则不为null

### 返回值常用属性和方法

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