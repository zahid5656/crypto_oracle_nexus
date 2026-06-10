package com.example.ui

fun validateAutoClose(
    isLong: Boolean,
    currentPrice: Double?,
    target: Double?,
    tp1: Double?,
    tp2: Double?,
    tp3: Double?,
    sl1: Double?,
    sl2: Double?,
    options: List<String>,
    entryPrice: Double?
): Pair<Boolean, String?> {
    if (options.isEmpty()) return Pair(true, null)
    
    val refPrice = entryPrice ?: currentPrice

    if ("TARGET" in options && target != null && refPrice != null) {
        if (isLong && target <= refPrice) return Pair(false, "TARGET IS BELOW OR EQUAL TO ENTRY FOR LONG")
        if (!isLong && target >= refPrice) return Pair(false, "TARGET IS ABOVE OR EQUAL TO ENTRY FOR SHORT")
    }
    if ("TP1" in options && tp1 != null && refPrice != null) {
        if (isLong && tp1 <= refPrice) return Pair(false, "TP1 IS BELOW OR EQUAL TO ENTRY FOR LONG")
        if (!isLong && tp1 >= refPrice) return Pair(false, "TP1 IS ABOVE OR EQUAL TO ENTRY FOR SHORT")
    }
    if ("TP2" in options && tp2 != null && refPrice != null) {
        if (isLong && tp2 <= refPrice) return Pair(false, "TP2 IS BELOW OR EQUAL TO ENTRY FOR LONG")
        if (!isLong && tp2 >= refPrice) return Pair(false, "TP2 IS ABOVE OR EQUAL TO ENTRY FOR SHORT")
    }
    if ("TP3" in options && tp3 != null && refPrice != null) {
        if (isLong && tp3 <= refPrice) return Pair(false, "TP3 IS BELOW OR EQUAL TO ENTRY FOR LONG")
        if (!isLong && tp3 >= refPrice) return Pair(false, "TP3 IS ABOVE OR EQUAL TO ENTRY FOR SHORT")
    }
    if ("STOP LOSS" in options) {
        if (sl1 != null && refPrice != null) {
            if (isLong && sl1 >= refPrice) return Pair(false, "SL1 IS ABOVE OR EQUAL TO ENTRY FOR LONG")
            if (!isLong && sl1 <= refPrice) return Pair(false, "SL1 IS BELOW OR EQUAL TO ENTRY FOR SHORT")
        }
        if (sl2 != null && refPrice != null) {
            if (isLong && sl2 >= refPrice) return Pair(false, "SL2 IS ABOVE OR EQUAL TO ENTRY FOR LONG")
            if (!isLong && sl2 <= refPrice) return Pair(false, "SL2 IS BELOW OR EQUAL TO ENTRY FOR SHORT")
        }
        if (sl1 != null && sl2 != null) {
            if (isLong && sl2 > sl1) return Pair(false, "SL2 IS ABOVE SL1 FOR LONG")
            if (!isLong && sl2 < sl1) return Pair(false, "SL2 IS BELOW SL1 FOR SHORT")
        }
    }
    
    // Check progression
    if (tp1 != null && tp2 != null) {
        if (isLong && tp1 > tp2) return Pair(false, "TP1 IS ABOVE TP2 FOR LONG")
        if (!isLong && tp1 < tp2) return Pair(false, "TP1 IS BELOW TP2 FOR SHORT")
    }
    if (tp2 != null && tp3 != null) {
        if (isLong && tp2 > tp3) return Pair(false, "TP2 IS ABOVE TP3 FOR LONG")
        if (!isLong && tp2 < tp3) return Pair(false, "TP2 IS BELOW TP3 FOR SHORT")
    }

    return Pair(true, null)
}
