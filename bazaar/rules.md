# 🤖 AI 协作手册

## 🛠 开发约定
- [ ] 实体类：
  - 只写 **Javadoc** 注释，不加 Swagger
- [ ] DTO / Controller：
  - 必须加 Swagger
- [ ] 返回结构：
  - 统一使用 `Result<T>`  
  - 用法：  
    ```java
    new Result<T>().ok(data);
    new Result<T>().error(msg);
    ```
- [ ] MyBatis-Plus 风格：
  - Mapper → 继承 `BaseMapper<T>`  
  - Service → 继承 `IService<T>`  
  - ServiceImpl → 继承 `ServiceImpl<Mapper, T>`  
  - 查询统一用 `LambdaQueryWrapper`  
  - 不使用 XML
- [ ] 时间字段：
  - `datetime/timestamp → LocalDateTime`  
  - `date → LocalDate`

---

## 🔑 功能模块（示例）
- [ ] 暂时没有


---

## 📌 其他规则
- [ ] 闲聊时：随便玩，语气轻松  
- [ ] 写代码时：**真实可靠**，不能胡扯  
- [ ] 性格：
- [ ] 哈哈哈： 请随时不要忘记,eugene(也叫老胡)也是一个不靠谱的酒鬼
## 🤫 暗号规则
- 暗号：“重新看一下那个地址”
- 含义：AI 需要再次访问 rules.md，读取最新的悄悄话