# newyearparty
易联达年会抽奖程序</br>
前端引用-[fouber/lottery](https://github.com/fouber/lottery)
## Background
年会参与人数越来越多，纸箱抽奖已经不适用
## Install
mvn install<br>
### 提示
此项目依赖微信公众号实现，可以通过[微信测试公众号](https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login)进行开发测试<br>
抽奖主页面URL：http://host:port/index.html<br>
二维码生成URL(ps：参数无括号)：https://open.weixin.qq.com/connect/oauth2/authorize?appid=(公众号后台查询)&redirect_uri=(URL编码http://host:port/login_qrcode.html)&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect
抽奖前权限验证：http://host:port/admin.html<br>
## 环境依赖
jdk1.8<br>
tomcat1.8<br>
redis
## redis数据结构
### 人员准入信息(是否允许签到)
#### 数据类型:hash

Key  | Filed  | Comment
---- | ----- | ------ 
allwo:姓名_证件后6位  | name | 姓名
.  | idLast | 证件后6位 

### 签到地点信息(允许签到地点)
#### 数据类型:hash

Key  | Filed  | Comment
---- | ----- | ------ 
local_地点名称  | longitude | 经度
.  | latitude | 纬度 

### 签到地点信息(签到人员的信息)
#### 数据类型:hash

Key  | Filed  | Comment
---- | ----- | ------ 
login:姓名_证件后6位  | name | 姓名
.  | idLast | 证件号 
.  | openid | 微信ID
.  | nickname | 微信昵称 
.  | headimgurl | 头像URL 
.  | loginTime | 签到时间(yyyy-mm-dd hh24:mi:ss) 
.  | localName | 签到地点名称 

### 签到人员集合(签到人员的列表)
#### 数据类型:list

Key  | Value  | Comment
---- | ----- | ------ 
login_user_list  | 签到人员信息KEY | login:姓名_证件后6位

### 开奖记录(开奖记录信息)
#### 数据类型:hash

Key  | Filed  | Comment
---- | ----- | ------ 
game:时间戳  | pnum | 中奖人数
.  | lvl | 开奖等级(特等奖、一等奖...) 
.  | openid | 微信ID
.  | time | 时间戳 (yyyy-mm-dd hh24:mi:ss)
.  | nbGuysId | 中奖人员ID(login:姓名_证件后6位，|#|分割) 


### 开奖记录集合(开奖的列表)
#### 数据类型:list

Key  | Value  | Comment
---- | ----- | ------ 
game_list  | 开奖记录KEY | game:时间戳 

### 中奖人员记录(中过奖的人员记录)
#### 数据类型:list

Key  | Value  | Comment
---- | ----- | ------ 
ok_list  | 中奖人员ID | login:姓名_证件后6位 

### 已经使用过的openid(防止同一个微信签到多人)
#### 数据类型:set

Key  | Value  | Comment
---- | ----- | ------ 
used_openid  | 微信ID | 微信ID 

### 其他需要提前录入的标数
#### 数据类型:string

Key  | Value  | Comment
---- | ----- | ------ 
adminCode  | 授权码 | 登录校验 
login_stop_flag  | 任意值 | 存在即关闭签到入口 
wechat_appsecret  | 微信提供 | 微信后台查询 
wechat_appid  | 微信提供 | 微信后台查询 

