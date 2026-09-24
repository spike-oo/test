# 04 开发规范与 Git 协作

## 一、分支模型

```text
main       可演示、可交付的稳定版本，只接受来自 dev 的合并
 └── dev   集成分支，四人日常提交汇总到这里
      ├── feature/user-ai-order        成员1
      ├── feature/merchant-dish        成员2
      ├── feature/admin-dashboard      成员3
      └── feature/order-statemachine   成员4
```

规则：

1. 任何人不得直接向 `main` 提交。
2. 一个 feature 分支只做一件事，完成后向 `dev` 提 PR，由另一位成员评审后合并。
3. 合并前先 `git pull --rebase origin dev`，避免无意义的合并提交。
4. 阶段性演示前，由成员4 将 `dev` 合并到 `main` 并打 tag，例如 `v0.1-需求与骨架`。

## 二、提交信息规范

格式：`<类型>(<范围>): <说明>`

| 类型 | 含义 |
| --- | --- |
| feat | 新功能 |
| fix | 缺陷修复 |
| docs | 文档 |
| refactor | 重构（不改外部行为） |
| style | 格式调整 |
| test | 测试 |
| chore | 构建、依赖、配置 |

示例：

```text
feat(order): 新增订单状态机与流转日志
fix(cart): 修复数量为 0 时未从购物车移除
docs(readme): 补充本地启动步骤
```

## 三、代码规范

### 后端

- 包结构按模块划分：`modules/<模块>/{controller,service,mapper,entity,dto}`。
- 命名：类 `UpperCamelCase`，方法与变量 `lowerCamelCase`，常量全大写，数据库表/字段 `snake_case`。
- Controller 只做参数校验与响应封装，业务逻辑全部在 Service。
- 事务注解 `@Transactional(rollbackFor = Exception.class)` 加在 Service 方法上，不加在 Controller。
- 禁止在 Controller 或 Service 中拼接 SQL；条件查询用 MyBatis-Plus 的 `LambdaQueryWrapper`。
- 对外返回统一使用 `Result<T>`，禁止直接返回 Entity。
- 敏感信息（手机号、身份证号）返回前端前必须脱敏。
- 大模型 API Key 只存服务端配置，禁止写进前端或提交到仓库。

### 前端

- 组件文件 `PascalCase.vue`，目录小写中划线。
- 接口调用统一走 `src/api/*.ts`，禁止在组件里直接写 axios。
- 接口地址统一用相对路径 `/api/xxx`，由 `vite.config.ts` 代理到后端。
- 页面按端分区放在 `views/{student,merchant,rider,admin}` 下，跨端公共组件放 `components`。
- 列表页必须有加载中、空状态、错误提示三种状态。

## 四、数据库变更

1. 表结构只通过 `database/schema.sql` 变更，谁的数据责任域谁提交。
2. 已建表结构变更需在 PR 描述中说明：改了哪张表、加了什么字段、是否影响他人。
3. 所有表必须包含：主键、`create_time`、`update_time`，需要逻辑删除的表加 `deleted`。
4. 金额字段统一 `DECIMAL(10,2)`，状态字段统一 `TINYINT`，主键统一 `VARCHAR(32)`。
5. 提交前用 `database/schema.sql` 在本地重建一次库，确认脚本可直接执行。

## 五、本地开发注意事项

- 个人配置（数据库密码、大模型 Key）写在 `application-local.yml` 或 `frontend/.env.local`，这两类文件已在 `.gitignore` 中忽略。
- 提交前检查 `git status`，确认没有把密钥、日志、`node_modules`、`target` 带进提交。
- 拉取代码后如遇依赖变更：后端 `mvn clean install -DskipTests`，前端 `npm install`。

## 六、协作检查清单（每次提 PR 前自查）

- [ ] 代码能在本地跑通，无编译错误与未处理的警告
- [ ] 接口已在 `docs/03-接口规范与协作约定.md` 登记
- [ ] 新增表结构已更新 `database/schema.sql` 并验证可执行
- [ ] 无密钥、无本地绝对路径、无调试残留代码
- [ ] 提交信息符合规范，改动范围与分支主题一致
