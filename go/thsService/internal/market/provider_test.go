package market

import "testing"

func TestParseTencentStockLine(t *testing.T) {
	instrument := Instrument{ID: 7, SourceSymbol: "sh600519"}
	fields := make([]string, 33)
	fields[3] = "1274.89"
	fields[4] = "1288.00"
	fields[30] = "20261013161452"
	fields[31] = "-13.11"
	fields[32] = "-1.02"
	line := `v_sh600519="` + joinFields(fields, "~") + `"`

	quote, ok := parseTencentLine(line, map[string]Instrument{"sh600519": instrument})
	if !ok {
		t.Fatal("expected Tencent stock quote to parse")
	}
	if quote.InstrumentID != 7 || quote.LatestPrice != "1274.89" || quote.ChangePercent != "-1.02" {
		t.Fatalf("unexpected quote: %+v", quote)
	}
}

func TestParseSinaFutureLine(t *testing.T) {
	instrument := Instrument{ID: 9, SourceSymbol: "nf_CU0"}
	line := `var hq_str_nf_CU0="铜连续,233259,110950.000,111020.000,110670.000,0.000,110710.000,110720.000,110720.000,0.000,110900.000,1,8,195266.000,13203,沪,铜,2026-10-13,1"`

	quote, ok := parseSinaLine(line, map[string]Instrument{"nf_CU0": instrument})
	if !ok {
		t.Fatal("expected Sina future quote to parse")
	}
	if quote.LatestPrice != "110720.000" || quote.ChangeAmount != "-180.000000" || quote.ChangePercent != "-0.1623" {
		t.Fatalf("unexpected quote: %+v", quote)
	}
}

func joinFields(values []string, separator string) string {
	result := ""
	for index, value := range values {
		if index > 0 {
			result += separator
		}
		result += value
	}
	return result
}
