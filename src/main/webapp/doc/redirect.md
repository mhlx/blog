## redirect标签

### 完整写法
```
<redirect url="" permanently="" code="" argumentSpliter="" arguments="" defaultMsg=""/>
```

### 说明
用于页面之间的跳转，如果携带了信息，可以在目标页面通过`redirect_page_msg`来获取携带的信息

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |     
|  -  |  -  |  -  |  -  | - |
|  url  | 跳转地址   | 是   |  string  |  | 
| permanently   | 是否301跳转   | 否   |  [true,false]  |  |  
|  code  |  信息key值  |  否  |  string  |  |  
|  argumentSpliter  | 参数分割符   | 否   |  string  | ， | 
|  defaultMsg  | 默认信息   | 否   |  string  |  |  

### 动态属性
是

### 其他
1. redirect标签应该被尽早的解析
2. 301跳转时**无法显示携带的信息**