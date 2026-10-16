UPDATE market_instruments
SET source_symbol = 's_usINX'
WHERE category = 'global' AND symbol = 'SPX.US';

INSERT INTO market_instruments
    (category, symbol, source, source_symbol, name, market, currency, sort_order, fallback_price, fallback_change_amount, fallback_change_percent)
VALUES
    ('global', 'HSTECH.HK', 'tencent', 's_hkHSTECH', '恒生科技指数', 'HK', 'HKD', 70, 4136.63, 0, 0),
    ('global', 'NDX.US', 'tencent', 's_usNDX', '纳斯达克100', 'US', 'USD', 80, 30445.91, 0, 0),
    ('global', 'CL', 'tencent', 'hf_CL', '纽约原油', 'GLOBAL', 'USD', 90, 94.13, 0, 0),
    ('global', 'SI', 'tencent', 'hf_SI', '纽约白银', 'GLOBAL', 'USD', 100, 59.49, 0, 0),

    ('a_stock', '000858.SZ', 'tencent', 'sz000858', '五粮液', 'CN', 'CNY', 60, 70.46, 0, 0),
    ('a_stock', '601899.SH', 'tencent', 'sh601899', '紫金矿业', 'CN', 'CNY', 70, 30.40, 0, 0),
    ('a_stock', '600900.SH', 'tencent', 'sh600900', '长江电力', 'CN', 'CNY', 80, 29.15, 0, 0),
    ('a_stock', '000333.SZ', 'tencent', 'sz000333', '美的集团', 'CN', 'CNY', 90, 82.79, 0, 0),
    ('a_stock', '601166.SH', 'tencent', 'sh601166', '兴业银行', 'CN', 'CNY', 100, 18.15, 0, 0),

    ('hk_stock', '01211.HK', 'tencent', 'hk01211', '比亚迪股份', 'HK', 'HKD', 60, 73.25, 0, 0),
    ('hk_stock', '00939.HK', 'tencent', 'hk00939', '建设银行', 'HK', 'HKD', 70, 9.96, 0, 0),
    ('hk_stock', '01299.HK', 'tencent', 'hk01299', '友邦保险', 'HK', 'HKD', 80, 73.05, 0, 0),
    ('hk_stock', '00388.HK', 'tencent', 'hk00388', '香港交易所', 'HK', 'HKD', 90, 379.60, 0, 0),
    ('hk_stock', '09888.HK', 'tencent', 'hk09888', '百度集团-W', 'HK', 'HKD', 100, 81.65, 0, 0),

    ('us_stock', 'GOOGL.US', 'tencent', 'usGOOGL', '谷歌-A', 'US', 'USD', 60, 355.70, 0, 0),
    ('us_stock', 'META.US', 'tencent', 'usMETA', 'Meta Platforms', 'US', 'USD', 70, 762.51, 0, 0),
    ('us_stock', 'AMD.US', 'tencent', 'usAMD', '超威半导体', 'US', 'USD', 80, 588.27, 0, 0),
    ('us_stock', 'NFLX.US', 'tencent', 'usNFLX', '奈飞', 'US', 'USD', 90, 71.12, 0, 0),
    ('us_stock', 'JPM.US', 'tencent', 'usJPM', '摩根大通', 'US', 'USD', 100, 336.04, 0, 0),

    ('etf', '512880.SH', 'tencent', 'sh512880', '证券ETF', 'CN', 'CNY', 60, 1.035, 0, 0),
    ('etf', '512480.SH', 'tencent', 'sh512480', '半导体ETF', 'CN', 'CNY', 70, 0.886, 0, 0),
    ('etf', '512690.SH', 'tencent', 'sh512690', '酒ETF', 'CN', 'CNY', 80, 0.415, 0, 0),
    ('etf', '159941.SZ', 'tencent', 'sz159941', '纳指ETF', 'CN', 'CNY', 90, 1.759, 0, 0),
    ('etf', '159949.SZ', 'tencent', 'sz159949', '创业板50ETF', 'CN', 'CNY', 100, 1.375, 0, 0),

    ('future', 'AL0', 'sina', 'nf_AL0', '沪铝主连', 'CN', 'CNY', 60, 23350.00, 0, 0),
    ('future', 'ZN0', 'sina', 'nf_ZN0', '沪锌主连', 'CN', 'CNY', 70, 25680.00, 0, 0),
    ('future', 'I0', 'sina', 'nf_I0', '铁矿石主连', 'CN', 'CNY', 80, 675.00, 0, 0),
    ('future', 'RM0', 'sina', 'nf_RM0', '菜粕主连', 'CN', 'CNY', 90, 2387.00, 0, 0),
    ('future', 'TA0', 'sina', 'nf_TA0', 'PTA主连', 'CN', 'CNY', 100, 6584.00, 0, 0),

    ('gold', '518880.SH', 'tencent', 'sh518880', '黄金ETF华安', 'CN', 'CNY', 50, 8.599, 0, 0),
    ('gold', '518800.SH', 'tencent', 'sh518800', '黄金ETF国泰', 'CN', 'CNY', 60, 9.014, 0, 0),
    ('gold', '159937.SZ', 'tencent', 'sz159937', '黄金ETF博时', 'CN', 'CNY', 70, 8.580, 0, 0),
    ('gold', '159934.SZ', 'tencent', 'sz159934', '黄金ETF易方达', 'CN', 'CNY', 80, 8.985, 0, 0),
    ('gold', '600547.SH', 'tencent', 'sh600547', '山东黄金', 'CN', 'CNY', 90, 26.99, 0, 0),
    ('gold', '601899.SH', 'tencent', 'sh601899', '紫金矿业', 'CN', 'CNY', 100, 30.40, 0, 0),

    ('convertible_bond', '113056.SH', 'tencent', 'sh113056', '重银转债', 'CN', 'CNY', 60, 124.826, 0, 0),
    ('convertible_bond', '127038.SZ', 'tencent', 'sz127038', '国微转债', 'CN', 'CNY', 70, 110.178, 0, 0),
    ('convertible_bond', '123119.SZ', 'tencent', 'sz123119', '康泰转2', 'CN', 'CNY', 80, 116.601, 0, 0),
    ('convertible_bond', '118034.SH', 'tencent', 'sh118034', '晶能转债', 'CN', 'CNY', 90, 113.481, 0, 0),
    ('convertible_bond', '110085.SH', 'tencent', 'sh110085', '通22转债', 'CN', 'CNY', 100, 111.205, 0, 0),

    ('other', 'HSTECH.HK', 'tencent', 's_hkHSTECH', '恒生科技指数', 'HK', 'HKD', 50, 4136.63, 0, 0),
    ('other', 'NDX.US', 'tencent', 's_usNDX', '纳斯达克100', 'US', 'USD', 60, 30445.91, 0, 0),
    ('other', 'SPX.US', 'tencent', 's_usINX', '标普500', 'US', 'USD', 70, 7788.27, 0, 0),
    ('other', 'GC', 'tencent', 'hf_GC', '纽约黄金', 'GLOBAL', 'USD', 80, 4185.62, 0, 0),
    ('other', 'XAU', 'tencent', 'hf_XAU', '伦敦金', 'GLOBAL', 'USD', 90, 4164.02, 0, 0),
    ('other', 'HSI.HK', 'tencent', 's_hkHSI', '恒生指数', 'HK', 'HKD', 100, 24345.22, 0, 0);
