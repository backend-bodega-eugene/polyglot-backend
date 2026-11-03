# 电费计费字段

App 原接口 `GET /api/app/electricity/bills` 每条账单新增 `billing`。既有字段保留，`amount`、`total_usage` 仍为 JSON 数字；`billing` 中金额、电量、单价为十进制字符串，避免浮点精度丢失。

后台仍使用现有账单增删改查接口：读接口同时返回 `billing`，新增/修改将计费信息放进 `detail.billing`。后台电费账单弹窗提供对应表单，不需要编辑 JSON。

```json
{
  "region": "深圳市",
  "usage_type": "居民生活用电",
  "pricing_mode": "tiered",
  "tariff_name": "深圳居民单一制阶梯电价",
  "tiers": [
    {"level": 1, "usage": "260.00", "unit_price": "0.66286875", "amount": "172.35"},
    {"level": 2, "usage": "108.90", "unit_price": "0.71286875", "amount": "77.63"},
    {"level": 3, "usage": "0.00", "unit_price": "0.96286875", "amount": "0.00"}
  ],
  "electricity_fee": "249.98",
  "average_price": "0.67763622",
  "payment_status": "paid",
  "due_date": "2026-10-24",
  "late_fee_start_date": "2026-10-25",
  "late_fee_as_of_date": "",
  "arrears_principal": "0.00",
  "late_fee_daily_rate": "0.001",
  "late_fee": "0.00",
  "payable_amount": "249.98"
}
```

- 自动生成：夏季5—10月按260、600度分界，其他月份按200、400度分界。三个档位分别计算、四舍五入到分后合计。当前生成模板采用0.66286875、0.71286875、0.96286875元/kWh，属于可编辑初始值，不是覆盖所有历史账期的价格引擎。
- 保存行为：**不校验电量/金额合计，不校验金额等于电量乘单价，不校验缴费状态与欠费金额关系，不校验缴费日期先后关系**。手工输入值原样保存；仅限制字段类型、格式、长度和数值范围。平均价、电费合计、应付合计均可独立填写。
- `payment_status` 为 `paid`、`unpaid`、`overdue` 或空字符串；日违约金率0.001表示千分之一。查询不重新计算或增加违约金。
- 历史账单没有 `detail.billing` 时，返回原金额、空日期、空档位数组、`pricing_mode=flat`；不会补写数据库、推算逾期日期或改动历史金额。可在后台编辑补充，或明确重新生成会员的20条账单。
- 一键拆分只填充初始值，点击后仍可独立修改任何字段。自动生成的日期是展示模板值，可修改，不代表真实供电合同的缴费约定。
- PDF 取数保存手填金额，不重新按单价覆盖；草稿每行 `calculation=manual`。若改为 `auto`，才按电量和单价计算。导出编辑不回写账单。
- 使用现有 `electricity_bills.detail` JSON 列持久化，本次无新增数据库迁移。
- 将来真实数据接入，映射到同一结构即可；是否存在特定电价方案、扩展费用或新字段仍需按真实接口确认。
