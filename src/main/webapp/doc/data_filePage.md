## filePage

### 完整写法
```
<data name="filePage" extensions="" pageSize="" currentPage="" type="" ignorePaging="" fileName="" path=""/>
```

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |    
|  -  |  -  |  -  |  -  |  -  |
| extensions | 文件后缀集合，多个后缀以,分隔   | 否   | string| |
| pageSize   | 每页文件数目   | 否   | int | |
| currentPage   | 当前页  | 否   | int | |
| type   | 文件类型  | 否   | enum['DIRECTORY','FILE'] | |
| ignorePaging   | 是否忽略分页  | 否   | [true,false] |false |
| fileName   | 文件名称  | 否   | string | |
| path   | 文件路径  | 否   | string | |

### 默认DataName
filePage

### 返回值
`PageResult<BlogFile>` 不为null

### 返回值常用属性和方法

#### BlogFile
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |      
|  type  |  文件类型  |  否  | enum['DIRECTORY','FILE']   | 
|  createDate  | 文件创建日期  |  否  | Timestamp  |
|  cf  | CommonFile  | 如果文件类型为文件夹，则为null，如果类型为文件，那么不为null  | CommonFile  |
|  path  | 文件名  |  否  | string  |

### CommonFile
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  size  | 文件大小   | 否   |  long  |   
|  extension  | 文件后缀   | 否   |  string  |     
|  originalFilename  | 文件原始名称   | 否   |  string  |  
|  url  | 文件访问地址   | 否   |  string  |  
|  thumbnailUrl  | 缩略图文件访问地址   | 是   |  ThumbnailUrl  |


### ThumbnailUrl
|  属性名称  |说明| 是否可能为null   | 类型  |    
|  -  |  -  |  -  |  -  |  -  |
|  small  | 小尺寸文件访问地址   | 否   |  string  |
|  middle  | 中尺寸文件访问地址   | 否   |  string  |
|  large  | 大尺寸文件访问地址   | 是   |  string  |
 

|  方法名称  |说明| 返回值  | 返回值是否可能为null  |    
|  -  |  -  |  -  |  -  |  -  |
| getThumbUrl(int width, int height, boolean keepRatio)   | 根据指定的宽和高以及是否保持纵横比来构造文件的访问地址   | string   | 否| 
| getThumbUrl(int size)  | 根据指定的尺寸构造文件的访问地址   | string   | 否|