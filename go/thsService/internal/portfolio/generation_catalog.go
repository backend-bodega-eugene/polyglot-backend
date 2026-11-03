package portfolio

import (
	"context"
	"errors"
	randv2 "math/rand/v2"
	"strings"
	"ths-service/internal/market"
	"time"
)

// These are generation-only reference prices, not a live quote feed. Keep the
// catalog separate so portfolio diversity does not change the market API.
var holdingCatalog = map[string]string{
	"a_stock":          `600036.SH|招商银行|40;601398.SH|工商银行|6;601939.SH|建设银行|8;601288.SH|农业银行|5;601988.SH|中国银行|5;601318.SH|中国平安|55;600030.SH|中信证券|28;600519.SH|贵州茅台|1400;000858.SZ|五粮液|135;000333.SZ|美的集团|75;000651.SZ|格力电器|42;600690.SH|海尔智家|28;300750.SZ|宁德时代|240;002594.SZ|比亚迪|110;600309.SH|万华化学|70;601012.SH|隆基绿能|18;600900.SH|长江电力|29;600025.SH|华能水电|9;601088.SH|中国神华|40;600028.SH|中国石化|6;601857.SH|中国石油|8;600887.SH|伊利股份|28;603288.SH|海天味业|40;600276.SH|恒瑞医药|48;000538.SZ|云南白药|56;002415.SZ|海康威视|30;000725.SZ|京东方A|4;600050.SH|中国联通|5;601728.SH|中国电信|6;600104.SH|上汽集团|16;601668.SH|中国建筑|6;600009.SH|上海机场|35;601111.SH|中国国航|8;600029.SH|南方航空|6;601899.SH|紫金矿业|25;600547.SH|山东黄金|30;002475.SZ|立讯精密|40;600438.SH|通威股份|22;002352.SZ|顺丰控股|40;601006.SH|大秦铁路|7`,
	"hk_stock":         `00700.HK|腾讯控股|500;09988.HK|阿里巴巴|120;03690.HK|美团|150;01810.HK|小米集团|40;09618.HK|京东集团|120;09999.HK|网易|160;09888.HK|百度集团|90;01024.HK|快手|60;02318.HK|中国平安|50;00939.HK|建设银行|7;01398.HK|工商银行|5;01288.HK|农业银行|4;03988.HK|中国银行|4;00005.HK|汇丰控股|85;00388.HK|香港交易所|320;00941.HK|中国移动|80;00728.HK|中国电信|5;00762.HK|中国联通|8;00883.HK|中国海洋石油|20;00857.HK|中国石油股份|6;00386.HK|中国石油化工股份|4;01211.HK|比亚迪股份|110;02015.HK|理想汽车|90;09866.HK|蔚来|40;00316.HK|东方海外国际|120;01929.HK|周大福|9;00669.HK|创科实业|95;00175.HK|吉利汽车|18;02313.HK|申洲国际|65;02382.HK|舜宇光学科技|55`,
	"us_stock":         `AAPL.US|苹果|220;MSFT.US|微软|420;NVDA.US|英伟达|130;AMZN.US|亚马逊|210;GOOGL.US|谷歌|180;META.US|Meta|600;TSLA.US|特斯拉|300;AMD.US|超威半导体|120;INTC.US|英特尔|25;QCOM.US|高通|160;AVGO.US|博通|190;ORCL.US|甲骨文|160;CRM.US|赛富时|280;ADBE.US|奥多比|400;NFLX.US|奈飞|100;DIS.US|迪士尼|110;KO.US|可口可乐|65;PEP.US|百事可乐|150;MCD.US|麦当劳|300;SBUX.US|星巴克|95;NKE.US|耐克|75;WMT.US|沃尔玛|90;COST.US|开市客|900;JPM.US|摩根大通|240;BAC.US|美国银行|42;WFC.US|富国银行|70;V.US|Visa|320;MA.US|万事达|500;PYPL.US|贝宝|75;JNJ.US|强生|160;PFE.US|辉瑞|28;MRK.US|默沙东|100;XOM.US|埃克森美孚|110;CVX.US|雪佛龙|150;CSCO.US|思科|60;UBER.US|优步|75`,
	"etf":              `510300.SH|沪深300ETF|4;510500.SH|中证500ETF|6;510050.SH|上证50ETF|3;159915.SZ|创业板ETF|2;588000.SH|科创50ETF|1;512100.SH|中证1000ETF|2;512880.SH|证券ETF|1;512800.SH|银行ETF|1;512660.SH|军工ETF|1;512690.SH|酒ETF|1;512010.SH|医药ETF|1;159928.SZ|消费ETF|1;512480.SH|半导体ETF|1;515030.SH|新能源车ETF|1;515790.SH|光伏ETF|1;516160.SH|新能源ETF|1;513100.SH|纳指ETF|2;513500.SH|标普500ETF|2;513050.SH|中概互联网ETF|1;159920.SZ|恒生ETF|1;510880.SH|红利ETF|3;159941.SZ|纳指ETF广发|2;518880.SH|黄金ETF华安|8;159934.SZ|黄金ETF易方达|8`,
	"future":           `RB0|螺纹钢主连|3500;HC0|热卷主连|3700;I0|铁矿石主连|800;J0|焦炭主连|1800;JM0|焦煤主连|1200;M0|豆粕主连|3000;Y0|豆油主连|8000;P0|棕榈油主连|8500;C0|玉米主连|2300;A0|豆一主连|4200;RM0|菜粕主连|2500;TA0|PTA主连|5000;MA0|甲醇主连|2500;PP0|聚丙烯主连|7500;L0|塑料主连|8000;V0|PVC主连|5500;SR0|白糖主连|6000;CF0|棉花主连|14000;RU0|橡胶主连|16000;FG0|玻璃主连|1300;SA0|纯碱主连|1500;AL0|沪铝主连|20000;ZN0|沪锌主连|24000;NI0|沪镍主连|120000`,
	"gold":             `518880.SH|黄金ETF华安|8;518800.SH|黄金ETF国泰|8;159937.SZ|黄金ETF博时|8;159934.SZ|黄金ETF易方达|8;518850.SH|黄金ETF华夏|8;518860.SH|黄金ETF建信|8;518660.SH|黄金ETF工银|8;159812.SZ|黄金ETF前海开源|8;600547.SH|山东黄金|30;601899.SH|紫金矿业|25;600489.SH|中金黄金|15;600988.SH|赤峰黄金|25;002155.SZ|湖南黄金|20;002237.SZ|恒邦股份|13;000975.SZ|山金国际|20;600459.SH|贵研铂业|18`,
	"convertible_bond": `110081.SH|闻泰转债|105;113050.SH|南银转债|130;127049.SZ|希望转2|110;123107.SZ|温氏转债|120;113044.SH|大秦转债|115;113056.SH|重银转债|120;127038.SZ|国微转债|115;123119.SZ|康泰转2|110;118034.SH|晶能转债|110;110085.SH|通22转债|108;113052.SH|兴业转债|125;110059.SH|浦发转债|115;110079.SH|杭银转债|125;113042.SH|上银转债|115;113065.SH|齐鲁转债|120;113066.SH|平煤转债|115;113053.SH|隆22转债|105;127045.SZ|牧原转债|120;127018.SZ|本钢转债|110;127032.SZ|苏行转债|125`,
}

// generationQuotes overlays available saved/live prices on the larger private
// catalog. Missing reference instruments remain explicitly marked mock.
func (s *Service) generationQuotes(ctx context.Context, category string, now time.Time) (market.QuoteListData, error) {
	supplied, err := s.quotes.List(ctx, category)
	if err != nil && !errors.Is(err, market.ErrNoInstruments) {
		return market.QuoteListData{}, err
	}
	data := catalogQuotes(category, now)
	bySymbol := map[string]market.QuoteItem{}
	for _, q := range supplied.Items {
		if _, ok := priceUnits(q.LatestPrice); ok {
			bySymbol[q.Symbol] = q
		}
	}
	for i, q := range data.Items {
		if latest, ok := bySymbol[q.Symbol]; ok && latest.Currency == q.Currency {
			data.Items[i] = latest
		}
	}
	return data, nil
}

func catalogQuotes(category string, now time.Time) market.QuoteListData {
	data := market.QuoteListData{Category: category, CategoryName: categoryLabels[category]}
	categories := []string{category}
	if category == "global" || category == "other" {
		categories = []string{"a_stock", "hk_stock", "us_stock", "etf"}
	}
	seen := map[string]bool{}
	for _, kind := range categories {
		for _, entry := range strings.Split(holdingCatalog[kind], ";") {
			parts := strings.Split(entry, "|")
			if len(parts) != 3 || seen[parts[0]] {
				continue
			}
			seen[parts[0]] = true
			currency, exchange := "CNY", "CN"
			if kind == "hk_stock" {
				currency, exchange = "HKD", "HK"
			}
			if kind == "us_stock" {
				currency, exchange = "USD", "US"
			}
			price, _ := priceUnits(parts[2])
			price = max(1, price*int64(94+randv2.IntN(13))/100)
			change := price * int64(randv2.IntN(501)-250) / 10000
			data.Items = append(data.Items, market.QuoteItem{Symbol: parts[0], Name: parts[1], Market: exchange, Currency: currency, LatestPrice: fixed(price, 4), ChangeAmount: fixed(change, 4), QuoteTime: now, DataStatus: "mock"})
		}
	}
	return data
}
