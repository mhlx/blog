## transaction标签

### 完整写法
```
<transaction:begin isolationLevel=""/>
//查询
<transaction:end/>
```

### 说明
用于开启一个只读事务

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  | - |
|  isolationLevel  | **事务隔离级别**  <br> -1:数据库默认  1:TRANSACTION_READ_UNCOMMITTED  2:TRANSACTION_READ_COMMITTED   4:TRANSACTION_REPEATABLE_READ  8:TRANSACTION_SERIALIZABLE | 否   |  [-1,1,2,4,8]  |  -1  |    

### 动态属性
否

### 其他
1. **一次只能打开一个事务，即transaction:begin不能嵌套使用**
2. transaction:end标签不是必须的，页面渲染结束后，会自动提交存在的事务