## formats

### 常用方法

1.  `String readByte(long bytes, boolean si)`将字节转为可读的文件大小，如果si为true，那么以1000为单位，否则以1024为单位
2.  `String readByte(long bytes)` 等同于 `readByte(long bytes, true)`
