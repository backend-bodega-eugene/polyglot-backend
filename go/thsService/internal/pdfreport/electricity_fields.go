package pdfreport

func init() {
	for i := range Schemas {
		if Schemas[i].Kind == "electricity" {
			Schemas[i].Fields = append(Schemas[i].Fields,
				f("dueDate", "缴费截止日", "date", false),
				f("lateFeeStartDate", "违约金起始日", "date", false),
				f("lateFeeAsOfDate", "违约金计至日期", "date", false),
				f("arrearsPrincipal", "欠费本金（元）", "money", false),
				f("lateFee", "违约金（元）", "money", false))
			Schemas[i].Fields = append(Schemas[i].Fields, f("electricityFee", "电费合计（可独立填写）", "money", false), f("payableAmount", "应付合计（可独立填写）", "money", false))
			for j := range Schemas[i].Tables {
				if Schemas[i].Tables[j].Key == "fees" {
					Schemas[i].Tables[j].Columns = append(Schemas[i].Tables[j].Columns, f("calculation", "计算方式：manual手填 / auto自动", "text", false))
				}
			}
		}
	}
}
