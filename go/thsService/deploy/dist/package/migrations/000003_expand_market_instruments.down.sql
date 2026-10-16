DELETE FROM market_instruments
WHERE (category = 'global' AND symbol IN ('HSTECH.HK', 'NDX.US', 'CL', 'SI'))
   OR (category = 'a_stock' AND symbol IN ('000858.SZ', '601899.SH', '600900.SH', '000333.SZ', '601166.SH'))
   OR (category = 'hk_stock' AND symbol IN ('01211.HK', '00939.HK', '01299.HK', '00388.HK', '09888.HK'))
   OR (category = 'us_stock' AND symbol IN ('GOOGL.US', 'META.US', 'AMD.US', 'NFLX.US', 'JPM.US'))
   OR (category = 'etf' AND symbol IN ('512880.SH', '512480.SH', '512690.SH', '159941.SZ', '159949.SZ'))
   OR (category = 'future' AND symbol IN ('AL0', 'ZN0', 'I0', 'RM0', 'TA0'))
   OR (category = 'gold' AND symbol IN ('518880.SH', '518800.SH', '159937.SZ', '159934.SZ', '600547.SH', '601899.SH'))
   OR (category = 'convertible_bond' AND symbol IN ('113056.SH', '127038.SZ', '123119.SZ', '118034.SH', '110085.SH'))
   OR (category = 'other' AND symbol IN ('HSTECH.HK', 'NDX.US', 'SPX.US', 'GC', 'XAU', 'HSI.HK'));

UPDATE market_instruments
SET source_symbol = 's_usSPX'
WHERE category = 'global' AND symbol = 'SPX.US';
