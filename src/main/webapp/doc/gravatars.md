## gravatars

### 常用方法

1. `String getUrl(String emailMd5)`根据邮箱的md5值构造完整的gravatar头像访问地址
2. `String getOptionalUrl(String emailMd5).orElse(url)`根据邮箱的md5值构造完整的gravatar头像访问地址，如果邮箱的md5值为空，那么则通过`toElse(url)`返回一个地址