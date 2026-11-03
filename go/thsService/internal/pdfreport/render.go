package pdfreport

import (
	"bytes"
	"embed"
	"fmt"
	"image"
	"image/color"
	"image/png"
	"math"
	"math/big"

	"codeberg.org/go-pdf/fpdf"
)

//go:embed assets/*
var resources embed.FS

type Asset struct {
	Slot int
	ID   uint64
	Data []byte
}
type renderer struct {
	p      *fpdf.Fpdf
	images map[int]string
}

func (r *renderer) font(size float64) { r.p.SetFont("report", "", size); r.p.SetTextColor(30, 40, 48) }
func (r *renderer) ensure(h float64) {
	if r.p.GetY()+h > 274 {
		r.p.AddPage()
	}
}
func (r *renderer) paragraph(text string, size float64) {
	r.font(size)
	lines := r.p.SplitText(text, 174)
	for _, line := range lines {
		r.ensure(7)
		r.p.MultiCell(174, 7, line, "", "L", false)
	}
}
func (r *renderer) section(title string) {
	r.ensure(15)
	r.p.Ln(4)
	r.font(11)
	r.p.SetFillColor(228, 242, 242)
	r.p.CellFormat(174, 9, title, "", 1, "L", true, 0, "")
	r.p.Ln(2)
}
func (r *renderer) image(slot int, x, y, w, h float64) {
	name := r.images[slot]
	if name == "" {
		return
	}
	info := r.p.GetImageInfo(name)
	iw, ih := info.Extent()
	scale := math.Min(w/iw, h/ih)
	nw, nh := iw*scale, ih*scale
	r.p.ImageOptions(name, x+(w-nw)/2, y+(h-nh)/2, nw, nh, false, fpdf.ImageOptions{ImageType: "PNG"}, 0, "")
}
func (r *renderer) table(headers []string, rows [][]string, widths []float64) {
	draw := func(cells []string, head bool) {
		r.font(8.5)
		height := 8.0
		for i, s := range cells {
			h := float64(len(r.p.SplitText(s, widths[i]-3)))*4 + 4
			if h > height {
				height = h
			}
		}
		x, y := 18.0, r.p.GetY()
		r.p.SetDrawColor(120, 135, 143)
		r.p.SetLineWidth(.15)
		for i, s := range cells {
			r.p.SetXY(x, y)
			if head {
				r.p.SetFillColor(230, 237, 241)
				r.p.Rect(x, y, widths[i], height, "DF")
			} else {
				r.p.Rect(x, y, widths[i], height, "D")
			}
			r.p.SetXY(x+1.5, y+2)
			r.p.MultiCell(widths[i]-3, 4, s, "", "L", false)
			x += widths[i]
		}
		r.p.SetXY(18, y+height)
	}
	r.ensure(20)
	draw(headers, true)
	for _, cells := range rows {
		r.font(8.5)
		height := 8.0
		for i, s := range cells {
			h := float64(len(r.p.SplitText(s, widths[i]-3)))*4 + 4
			if h > height {
				height = h
			}
		}
		if r.p.GetY()+height > 274 {
			r.p.AddPage()
			draw(headers, true)
		}
		draw(cells, false)
	}
	if len(rows) == 0 {
		r.font(9)
		r.p.CellFormat(174, 9, "未提供明细", "", 1, "L", false, 0, "")
	}
}
func safeImage(data []byte) ([]byte, error) {
	img, _, e := image.Decode(bytes.NewReader(data))
	if e != nil {
		return nil, e
	}
	b := img.Bounds()
	max := 1200
	if b.Dx() <= max && b.Dy() <= max {
		return data, nil
	}
	scale := math.Min(float64(max)/float64(b.Dx()), float64(max)/float64(b.Dy()))
	w, h := int(float64(b.Dx())*scale), int(float64(b.Dy())*scale)
	if w < 1 {
		w = 1
	}
	if h < 1 {
		h = 1
	}
	out := image.NewNRGBA(image.Rect(0, 0, w, h))
	for y := 0; y < h; y++ {
		for x := 0; x < w; x++ {
			out.SetNRGBA(x, y, color.NRGBAModel.Convert(img.At(b.Min.X+x*b.Dx()/w, b.Min.Y+y*b.Dy()/h)).(color.NRGBA))
		}
	}
	var buffer bytes.Buffer
	e = png.Encode(&buffer, out)
	return buffer.Bytes(), e
}
func Render(d Draft, assets []Asset) ([]byte, error) {
	if e := Validate(&d); e != nil {
		return nil, e
	}
	s, _ := Definition(d.Kind)
	p := fpdf.New("P", "mm", "A4", "")
	p.SetMargins(18, 18, 18)
	p.SetAutoPageBreak(false, 20)
	font, e := resources.ReadFile("assets/ReportSans-Regular.ttf")
	if e != nil {
		return nil, e
	}
	p.AddUTF8FontFromBytes("report", "", font)
	p.SetFont("report", "", 10)
	r := renderer{p: p, images: map[int]string{}}
	for _, a := range assets {
		data, e := safeImage(a.Data)
		if e != nil {
			return nil, fmt.Errorf("素材%d无法解码：%w", a.Slot, e)
		}
		name := fmt.Sprintf("asset-%d-%d", a.Slot, a.ID)
		p.RegisterImageOptionsReader(name, fpdf.ImageOptions{ImageType: "PNG"}, bytes.NewReader(data))
		r.images[a.Slot] = name
	}
	p.SetHeaderFunc(func() {
		p.SetXY(18, 10)
		p.SetFont("report", "", 8)
		p.SetTextColor(112, 127, 139)
		p.CellFormat(135, 6, "THS  /  "+s.Label, "", 0, "L", false, 0, "")
		p.SetFillColor(244, 236, 217)
		p.SetTextColor(139, 98, 39)
		p.CellFormat(39, 6, "演示报表", "", 0, "C", true, 0, "")
		p.SetDrawColor(205, 215, 220)
		p.Line(18, 18, 192, 18)
		p.SetXY(18, 24)
	})
	p.AliasNbPages("")
	p.SetFooterFunc(func() {
		p.SetY(282)
		p.SetX(18)
		p.SetFont("report", "", 7)
		p.SetTextColor(110, 120, 130)
		p.CellFormat(145, 5, "演示数据 · THS系统整理，非单位或机构出具的证明", "", 0, "L", false, 0, "")
		p.CellFormat(29, 5, fmt.Sprintf("%d / {nb}", p.PageNo()), "", 0, "R", false, 0, "")
	})
	p.SetTitle(s.Label, true)
	p.SetAuthor("THS", true)
	p.AddPage()
	switch d.Kind {
	case "work":
		r.work(d)
	case "electricity":
		r.electricity(d)
	case "social":
		r.social(d)
	}
	if e = p.Error(); e != nil {
		return nil, e
	}
	var out bytes.Buffer
	if e = p.Output(&out); e != nil {
		return nil, e
	}
	return out.Bytes(), nil
}
func chineseMoney(s string) string {
	n := Rounded(ParseAmount(s))
	if n == 0 {
		return "零元整"
	}
	digits := []rune("零壹贰叁肆伍陆柒捌玖")
	units := []string{"", "拾", "佰", "仟"}
	groups := []string{"", "万", "亿"}
	yuan := n / 100
	result := ""
	gap := false
	for g := 2; g >= 0; g-- {
		divisor := int64(1)
		for j := 0; j < g; j++ {
			divisor *= 10000
		}
		v := yuan / divisor % 10000
		if v == 0 {
			if result != "" {
				gap = true
			}
			continue
		}
		if result != "" && (gap || v < 1000) {
			result += "零"
		}
		gap = false
		part := ""
		zero := false
		for i := 3; i >= 0; i-- {
			pow := int64(1)
			for j := 0; j < i; j++ {
				pow *= 10
			}
			k := v / pow % 10
			if k == 0 {
				if part != "" {
					zero = true
				}
				continue
			}
			if zero {
				part += "零"
				zero = false
			}
			part += string(digits[k]) + units[i]
		}
		result += part + groups[g]
	}
	if result == "" {
		result = "零"
	}
	result += "元"
	j, f := n%100/10, n%10
	if j == 0 && f == 0 {
		return result + "整"
	}
	if j > 0 {
		result += string(digits[j]) + "角"
	}
	if j == 0 && f > 0 && n/100 > 0 {
		result += "零"
	}
	if f > 0 {
		result += string(digits[f]) + "分"
	}
	return result
}
func (r *renderer) work(d Draft) {
	p := r.p
	v := d.Fields
	r.font(21)
	p.SetY(38)
	p.CellFormat(174, 14, "工作信息报表", "", 1, "C", false, 0, "")
	p.Ln(13)
	end := v["endDate"]
	if end == "" {
		end = "在职"
	}
	r.paragraph(fmt.Sprintf("姓名：%s    性别：%s", v["name"], v["sex"]), 12)
	r.paragraph("证件号码："+v["idNumber"], 12)
	p.Ln(4)
	r.paragraph(fmt.Sprintf("单位：%s\n部门：%s\n职务：%s\n入职日期：%s    状态 / 离职日期：%s", v["companyName"], v["department"], v["post"], v["startDate"], end), 12)
	p.Ln(5)
	r.paragraph("月均收入：人民币 "+Money(Rounded(ParseAmount(v["monthlyIncome"])))+" 元", 12)
	r.paragraph("金额大写："+chineseMoney(v["monthlyIncome"]), 12)
	p.Ln(18)
	r.paragraph("本页整理所选会员的工作与收入信息。编辑内容仅用于本次导出，不回写业务数据。", 10)
	p.Ln(16)
	r.paragraph("单位地址："+v["companyAddress"], 11)
	r.ensure(45)
	y := p.GetY() + 5
	r.image(5, 122, y, 35, 35)
	p.SetY(y + 38)
	r.font(10)
	p.CellFormat(174, 8, "生成日期："+v["issuedDate"], "", 1, "R", false, 0, "")
}
func (r *renderer) electricity(d Draft) {
	p := r.p
	v := d.Fields
	r.image(4, 18, 25, 35, 17)
	r.image(3, 161, 25, 31, 17)
	r.font(19)
	p.SetY(28)
	p.CellFormat(174, 13, "用电账单报表", "", 1, "C", false, 0, "")
	p.SetY(47)
	r.font(9)
	pairs := [][]string{{"账单周期", v["startDate"] + " 至 " + v["endDate"]}, {"户号 / 户名", v["accountNo"] + " / " + v["name"]}, {"用电地址", v["address"]}, {"供电单位", v["supplier"]}, {"用电类别 / 电压", v["usageType"] + " / " + v["voltage"]}, {"电能表编号", v["meterNo"]}}
	r.table([]string{"基本信息", "内容"}, pairs, []float64{32, 142})
	rowsPayment := [][]string{{"缴费截止日 / 违约金起始日", v["dueDate"] + " / " + v["lateFeeStartDate"]}, {"欠费本金 / 违约金 / 应付（元）", v["arrearsPrincipal"] + " / " + v["lateFee"] + " / " + v["payableAmount"]}, {"违约金计至日期", v["lateFeeAsOfDate"]}}
	r.table([]string{"缴费信息", "内容"}, rowsPayment, []float64{58, 116})
	usage := v["totalUsage"]
	if len(d.Tables["readings"]) > 0 {
		sum := new(big.Rat)
		for _, row := range d.Tables["readings"] {
			sum.Add(sum, Usage(row))
		}
		usage = sum.FloatString(2)
	}
	var amount int64
	for _, row := range d.Tables["fees"] {
		amount += Fee(row)
	}
	if v["electricityFee"] != "" {
		amount = Rounded(ParseAmount(v["electricityFee"]))
	}
	r.section(fmt.Sprintf("本期电量 %s kWh     本期电费 %s 元", usage, Money(amount)))
	chartTop := p.GetY()
	if len(d.Tables["readings"]) > 0 {
		rows := [][]string{}
		for _, row := range d.Tables["readings"] {
			rows = append(rows, []string{row["label"], row["previous"], row["current"], row["multiplier"], Usage(row).FloatString(2)})
		}
		r.table([]string{"示数类型", "上期示数", "本期示数", "倍率", "电量"}, rows, []float64{27, 25, 25, 16, 23})
	}
	r.ensure(20)
	p.Ln(4)
	r.font(10)
	p.SetFillColor(228, 242, 242)
	p.CellFormat(116, 9, "电费明细 · 单位：千瓦时、元", "", 1, "L", true, 0, "")
	p.Ln(2)
	rows := [][]string{}
	for _, row := range d.Tables["fees"] {
		rows = append(rows, []string{row["label"], row["quantity"], row["price"], Money(Fee(row))})
	}
	rows = append(rows, []string{"合计", "", "", Money(amount)})
	r.table([]string{"费用组成", "计费电量", "计费标准", "电费"}, rows, []float64{44, 24, 24, 24})
	lastPage, lastY := p.PageNo(), p.GetY()
	p.SetPage(1)
	r.font(9)
	p.SetXY(139, chartTop)
	p.MultiCell(53, 5, "用能图表示意\n固定图片，非本期数据", "", "L", false)
	for i, item := range []string{"usage-chart.jpg", "tier-chart.jpg"} {
		b, _ := resources.ReadFile("assets/" + item)
		name := "chart" + item
		p.RegisterImageOptionsReader(name, fpdf.ImageOptions{ImageType: "JPG"}, bytes.NewReader(b))
		info := p.GetImageInfo(name)
		iw, ih := info.Extent()
		w, h := 49.0, math.Max(12, (243-chartTop-18)/2)
		scale := math.Min(w/iw, h/ih)
		p.ImageOptions(name, 141, chartTop+12+float64(i)*(h+4), iw*scale, ih*scale, false, fpdf.ImageOptions{ImageType: "JPG"}, 0, "")
	}
	r.font(9)
	p.SetXY(139, 248)
	p.MultiCell(53, 5, "抄表："+v["readDate"]+"\n打印："+v["issuedDate"], "", "L", false)
	r.image(7, 163, 260, 22, 14)
	p.SetPage(lastPage)
	p.SetXY(18, lastY)
}
func (r *renderer) social(d Draft) {
	p := r.p
	v := d.Fields
	r.font(19)
	p.SetY(30)
	p.CellFormat(174, 13, "社保缴费记录报表", "", 1, "C", false, 0, "")
	p.Ln(5)
	r.paragraph("姓名："+v["name"]+"    参保城市："+v["city"], 10)
	r.paragraph("证件号码："+v["idNumber"], 10)
	r.paragraph("个人管理码："+v["manageCode"], 10)
	r.section("一、参保基本情况")
	rows := [][]string{}
	for _, a := range d.Tables["coverage"] {
		rows = append(rows, []string{a["kind"], a["startMonth"], a["months"], a["status"]})
	}
	r.table([]string{"参保险种 / 口径", "开始年月", "缴费月数", "参保状态"}, rows, []float64{65, 34, 32, 43})
	r.section("二、参保缴费明细 · 金额单位：元")
	rows = [][]string{}
	companies := map[string]bool{}
	for _, a := range d.Tables["payments"] {
		rows = append(rows, []string{a["month"], a["companyNo"], a["base"], a["companyPay"], a["personalPay"]})
		companies[a["companyNo"]+"："+a["company"]] = true
	}
	r.table([]string{"缴费年月", "单位编号", "缴费基数", "单位缴费合计", "个人缴费合计"}, rows, []float64{25, 47, 32, 35, 35})
	r.paragraph("单位累计："+Money(Total(d.Tables["payments"], "companyPay"))+" 元    个人累计："+Money(Total(d.Tables["payments"], "personalPay"))+" 元", 10)
	if len(d.Tables["details"]) > 0 {
		r.section("三、补充分险种明细")
		rows = [][]string{}
		for _, a := range d.Tables["details"] {
			rows = append(rows, []string{a["month"], a["kind"], a["base"], a["companyPay"], a["personalPay"]})
		}
		r.table([]string{"缴费年月", "险种", "缴费基数", "单位缴费", "个人缴费"}, rows, []float64{25, 47, 32, 35, 35})
	}
	r.section("单位信息")
	printed := map[string]bool{}
	for _, a := range d.Tables["payments"] {
		key := a["companyNo"] + "：" + a["company"]
		if !printed[key] {
			r.paragraph(key, 9)
			printed[key] = true
		}
	}
	r.ensure(35)
	y := p.GetY() + 3
	r.image(6, 151, y, 28, 28)
	r.font(9)
	p.SetY(y + 14)
	p.CellFormat(120, 7, "生成日期："+v["issuedDate"], "", 1, "L", false, 0, "")
	p.SetY(y + 31)
}
