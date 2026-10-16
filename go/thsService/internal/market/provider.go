package market

import (
	"context"
	"fmt"
	"io"
	"math/big"
	"net/http"
	"strings"
	"time"

	"golang.org/x/text/encoding/simplifiedchinese"
	"golang.org/x/text/transform"
)

type Provider struct{ client *http.Client }

var marketLocation = func() *time.Location {
	location, err := time.LoadLocation("Asia/Shanghai")
	if err != nil {
		return time.FixedZone("CST", 8*60*60)
	}
	return location
}()

func NewProvider(timeout time.Duration) *Provider {
	return &Provider{client: &http.Client{Timeout: timeout}}
}

func (p *Provider) Fetch(ctx context.Context, source string, instruments []Instrument) (map[uint64]QuoteCache, error) {
	switch source {
	case "tencent":
		return p.fetchTencent(ctx, instruments)
	case "sina":
		return p.fetchSina(ctx, instruments)
	default:
		return nil, fmt.Errorf("unsupported market source %q", source)
	}
}

func (p *Provider) fetchTencent(ctx context.Context, instruments []Instrument) (map[uint64]QuoteCache, error) {
	return p.fetchText(ctx, "https://qt.gtimg.cn/q="+joinSourceSymbols(instruments), "", instruments, parseTencentLine)
}

func (p *Provider) fetchSina(ctx context.Context, instruments []Instrument) (map[uint64]QuoteCache, error) {
	return p.fetchText(ctx, "https://hq.sinajs.cn/list="+joinSourceSymbols(instruments), "https://finance.sina.com.cn/", instruments, parseSinaLine)
}

type lineParser func(string, map[string]Instrument) (QuoteCache, bool)

func (p *Provider) fetchText(ctx context.Context, endpoint, referer string, instruments []Instrument, parse lineParser) (map[uint64]QuoteCache, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, endpoint, nil)
	if err != nil {
		return nil, err
	}
	if referer != "" {
		req.Header.Set("Referer", referer)
	}
	resp, err := p.client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("market provider returned HTTP %d", resp.StatusCode)
	}
	body, err := io.ReadAll(transform.NewReader(resp.Body, simplifiedchinese.GBK.NewDecoder()))
	if err != nil {
		return nil, err
	}

	bySourceSymbol := make(map[string]Instrument, len(instruments))
	for _, instrument := range instruments {
		bySourceSymbol[instrument.SourceSymbol] = instrument
	}
	quotes := make(map[uint64]QuoteCache, len(instruments))
	for _, line := range strings.Split(string(body), ";") {
		if quote, ok := parse(strings.TrimSpace(line), bySourceSymbol); ok {
			quotes[quote.InstrumentID] = quote
		}
	}
	if len(quotes) == 0 {
		return nil, fmt.Errorf("market provider returned no usable quotes")
	}
	return quotes, nil
}

func parseTencentLine(line string, instruments map[string]Instrument) (QuoteCache, bool) {
	variable, payload, ok := splitProviderLine(line)
	if !ok || !strings.HasPrefix(variable, "v_") {
		return QuoteCache{}, false
	}
	sourceSymbol := strings.TrimPrefix(variable, "v_")
	instrument, ok := instruments[sourceSymbol]
	if !ok || payload == "" {
		return QuoteCache{}, false
	}

	if strings.HasPrefix(sourceSymbol, "hf_") {
		fields := strings.Split(payload, ",")
		if len(fields) < 14 || !positiveDecimal(fields[0]) {
			return QuoteCache{}, false
		}
		previous := fields[8]
		return newQuote(instrument.ID, fields[0], previous, decimalSubtract(fields[0], previous, 6), fields[1], parseDateTime(fields[12], fields[6]), line), true
	}

	fields := strings.Split(payload, "~")
	if len(fields) < 6 || !positiveDecimal(fields[3]) {
		return QuoteCache{}, false
	}
	if strings.HasPrefix(sourceSymbol, "s_") {
		return newQuote(instrument.ID, fields[3], "", fields[4], fields[5], time.Now().In(marketLocation), line), true
	}
	if len(fields) < 33 {
		return QuoteCache{}, false
	}
	return newQuote(instrument.ID, fields[3], fields[4], fields[31], fields[32], parseTencentTime(fields[30]), line), true
}

func parseSinaLine(line string, instruments map[string]Instrument) (QuoteCache, bool) {
	variable, payload, ok := splitProviderLine(line)
	if !ok || !strings.HasPrefix(variable, "var hq_str_") {
		return QuoteCache{}, false
	}
	sourceSymbol := strings.TrimPrefix(variable, "var hq_str_")
	instrument, ok := instruments[sourceSymbol]
	if !ok {
		return QuoteCache{}, false
	}
	fields := strings.Split(payload, ",")
	if len(fields) < 18 || !positiveDecimal(fields[8]) || !positiveDecimal(fields[10]) {
		return QuoteCache{}, false
	}
	change := decimalSubtract(fields[8], fields[10], 6)
	percent := decimalPercent(change, fields[10], 4)
	return newQuote(instrument.ID, fields[8], fields[10], change, percent, parseDateTime(fields[17], fields[1]), line), true
}

func splitProviderLine(line string) (string, string, bool) {
	left, right, ok := strings.Cut(line, "=\"")
	if !ok {
		return "", "", false
	}
	return strings.TrimSpace(left), strings.TrimSuffix(strings.TrimSpace(right), "\""), true
}

func newQuote(id uint64, price, previous, change, percent string, quoteTime time.Time, raw string) QuoteCache {
	var previousPointer *string
	if previous != "" {
		previousPointer = &previous
	}
	return QuoteCache{InstrumentID: id, LatestPrice: price, PreviousClose: previousPointer, ChangeAmount: change, ChangePercent: percent, QuoteTime: quoteTime, DataStatus: "live", RawData: &raw}
}

func joinSourceSymbols(instruments []Instrument) string {
	values := make([]string, 0, len(instruments))
	for _, instrument := range instruments {
		values = append(values, instrument.SourceSymbol)
	}
	return strings.Join(values, ",")
}

func positiveDecimal(value string) bool {
	number, ok := new(big.Rat).SetString(value)
	return ok && number.Sign() > 0
}

func decimalSubtract(left, right string, precision int) string {
	a, okA := new(big.Rat).SetString(left)
	b, okB := new(big.Rat).SetString(right)
	if !okA || !okB {
		return "0"
	}
	return new(big.Rat).Sub(a, b).FloatString(precision)
}

func decimalPercent(change, previous string, precision int) string {
	a, okA := new(big.Rat).SetString(change)
	b, okB := new(big.Rat).SetString(previous)
	if !okA || !okB || b.Sign() == 0 {
		return "0"
	}
	return new(big.Rat).Mul(new(big.Rat).Quo(a, b), big.NewRat(100, 1)).FloatString(precision)
}

func parseTencentTime(value string) time.Time {
	for _, layout := range []string{"20060102150405", "2006/01/02 15:04:05", "2006-01-02 15:04:05"} {
		if parsed, err := time.ParseInLocation(layout, value, marketLocation); err == nil {
			return parsed
		}
	}
	return time.Now().In(marketLocation)
}

func parseDateTime(date, clock string) time.Time {
	for _, layout := range []string{"2006-01-02 15:04:05", "2006-01-02 150405"} {
		if parsed, err := time.ParseInLocation(layout, date+" "+clock, marketLocation); err == nil {
			return parsed
		}
	}
	return time.Now().In(marketLocation)
}
