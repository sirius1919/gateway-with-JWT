# gateway-with-JWT
本微服务模块基于gateway的全局过滤器，实现过滤请求头中的token值，并对token值进行解析及ttl校验。

如果token校验成功，则获取用户信息（如：用户id），并将用户信息存入ServerWebExchange，随后放行过滤器链。

如果token校验不成功，则不放行过滤器链，并返回401

## 核心代码
### AuthGlobalFilter
用于认证的全局过滤器
### AuthProperties
路径的黑白名单配置类
### JwtProperties
JWT及token ttl相关的配置类
### SecurityConfig
密码加解密及jwt密钥对相关的配置类
### DynamicRouteLoader
动态路由加载器，用于从nacos读取动态路由配置，并实现路由自动加载
### JwtTool
用于解析token的工具类