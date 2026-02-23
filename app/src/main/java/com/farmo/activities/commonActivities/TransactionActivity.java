package com.farmo.activities.commonActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.farmo.R;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";

    // ─────────────────────────────────────────────────────────────────────────
    //  UI REFERENCES
    // ─────────────────────────────────────────────────────────────────────────
    private LinearLayout transactionContainer;
    private LinearLayout noDataView;
    private MaterialButton btn7, btn14, btnMonth;

    // ─────────────────────────────────────────────────────────────────────────
    //  DATA
    // ─────────────────────────────────────────────────────────────────────────
    private List<TransactionDay> allTransactions = new ArrayList<>();

    // Reference "today" — change to Calendar.getInstance() when using live API
    private static final int CURRENT_YEAR  = 2026;
    private static final int CURRENT_MONTH = 2;   // February
    private static final int CURRENT_DAY   = 23;

    // =========================================================================
    //  DATA MODELS
    // =========================================================================

    static class TransactionItem {
        String title, amount, type, subtitle, timestamp;

        TransactionItem(String title, String amount, String type,
                        String subtitle, String timestamp) {
            this.title     = title;
            this.amount    = amount;
            this.type      = type;
            this.subtitle  = subtitle;
            this.timestamp = timestamp;
        }
    }

    static class TransactionDay {
        String date, monthYear, closingBalance;
        int    year, month, day;   // numeric values parsed from JSON strings
        List<TransactionItem> transactions = new ArrayList<>();
    }

    // =========================================================================
    //  LIFECYCLE
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initViews();
        setupButtons();

        // ── LOAD DATA ─────────────────────────────────────────────────────────
        // To switch to API later: replace loadFromJson() with loadFromApi()
        // onDataLoaded() / onDataError() stay the same for both sources.
        loadFromJson();
    }

    private void initViews() {
        transactionContainer = findViewById(R.id.transactionContainer);
        noDataView           = findViewById(R.id.noDataView);
        btn7                 = findViewById(R.id.btn7Days);
        btn14                = findViewById(R.id.btn14Days);
        btnMonth             = findViewById(R.id.btn1Month);
    }

    private void setupButtons() {
        if (btn7     != null) btn7    .setOnClickListener(v -> applyFilter(7,  btn7));
        if (btn14    != null) btn14   .setOnClickListener(v -> applyFilter(14, btn14));
        if (btnMonth != null) btnMonth.setOnClickListener(v -> applyFilter(30, btnMonth));

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    // =========================================================================
    //  DATA LOADING  ← only this block changes when switching to API
    // =========================================================================

    /**
     * Loads transactions from  src/main/assets/transaction.json
     *
     * HOW TO REPLACE WITH API CALL:
     * ─────────────────────────────
     * Delete this method and add:
     *
     *   private void loadFromApi() {
     *       showLoading(true);
     *       String url = "https://your-backend.com/api/transactions";
     *
     *       // --- Volley example ---
     *       JsonArrayRequest req = new JsonArrayRequest(
     *           Request.Method.GET, url, null,
     *           response -> onDataLoaded(response.toString()),
     *           error    -> onDataError("Network error: " + error.getMessage())
     *       );
     *       Volley.newRequestQueue(this).add(req);
     *
     *       // --- Retrofit example ---
     *       // apiService.getTransactions().enqueue(new Callback<String>() {
     *       //     public void onResponse(...) { onDataLoaded(body); }
     *       //     public void onFailure(...)  { onDataError(t.getMessage()); }
     *       // });
     *   }
     *
     * Then call loadFromApi() instead of loadFromJson() in onCreate().
     * Everything below (onDataLoaded → parseTransactions → applyFilter → UI)
     * is identical for both sources — no other changes needed.
     */
    private void loadFromJson() {
        showLoading(true);

        // Debug: list all files Android can see in assets
        try {
            String[] assetFiles = getAssets().list("");
            Log.d(TAG, "=== ASSETS FOLDER CONTENTS ===");
            if (assetFiles == null || assetFiles.length == 0) {
                Log.e(TAG, "Assets folder is EMPTY — check src/main/assets/");
            } else {
                for (String f : assetFiles) {
                    Log.d(TAG, "  Asset: [" + f + "]");
                }
            }
            Log.d(TAG, "==============================");
        } catch (Exception e) {
            Log.e(TAG, "Cannot list assets: " + e.getMessage());
        }

        // Load the JSON file
        try {
            InputStream is     = getAssets().open("transaction.json");
            int         size   = is.available();
            byte[]      buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "JSON loaded OK — length=" + json.length());

            onDataLoaded(json);   // hand off to the shared pipeline

        } catch (Exception e) {
            Log.e(TAG, "Asset load FAILED: " + e.getMessage());
            onDataError("Could not load transaction.json\n" + e.getMessage());
        }
    }

    // =========================================================================
    //  SHARED DATA PIPELINE  (JSON and API both feed into here)
    // =========================================================================

    /** Called by any data source once raw JSON is available. */
    private void onDataLoaded(String json) {
        showLoading(false);
        allTransactions = parseTransactions(json);

        if (allTransactions.isEmpty()) {
            Log.w(TAG, "Parsed 0 days — verify JSON structure matches the model");
            showNoData();
        } else {
            Log.d(TAG, "Parsed " + allTransactions.size() + " transaction day(s)");
            applyFilter(7, btn7);   // default view: last 7 days
        }
    }

    /** Called by any data source on failure. */
    private void onDataError(String message) {
        showLoading(false);
        Log.e(TAG, "onDataError: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showNoData();
    }

    // =========================================================================
    //  JSON PARSING
    // =========================================================================

    private List<TransactionDay> parseTransactions(String json) {
        List<TransactionDay> result = new ArrayList<>();
        try {
            JSONArray daysArray = new JSONArray(json);
            Log.d(TAG, "JSON top-level array length: " + daysArray.length());

            for (int i = 0; i < daysArray.length(); i++) {
                JSONObject obj = daysArray.getJSONObject(i);

                TransactionDay td    = new TransactionDay();
                td.date              = obj.optString("date",           "0");
                td.monthYear         = obj.optString("monthYear",      "");
                td.closingBalance    = obj.optString("closingBalance", "Rs. 0.00");

                // Parse numeric date components for accurate range filtering
                td.day = safeParseInt(td.date.trim(), 0);
                int[] ym = parseMonthYear(td.monthYear);
                td.year  = ym[0];
                td.month = ym[1];

                Log.d(TAG, "Day parsed: " + td.day + " " + td.monthYear
                        + "  dayCount=" + toDayCount(td.year, td.month, td.day));

                JSONArray txns = obj.optJSONArray("transactions");
                if (txns != null) {
                    for (int j = 0; j < txns.length(); j++) {
                        JSONObject t = txns.getJSONObject(j);
                        td.transactions.add(new TransactionItem(
                                t.optString("title",     ""),
                                t.optString("amount",    ""),
                                t.optString("type",      "debit"),
                                t.optString("subtitle",  ""),
                                t.optString("timestamp", "")
                        ));
                    }
                }

                result.add(td);
            }

        } catch (Exception e) {
            Log.e(TAG, "JSON parse error: " + e.getMessage());
        }
        return result;
    }

    // =========================================================================
    //  FILTER + UI RENDERING
    // =========================================================================

    private void applyFilter(int daysBack, MaterialButton selectedButton) {
        if (transactionContainer == null) return;

        updateButtonUI(selectedButton);
        transactionContainer.removeAllViews();

        int today   = toDayCount(CURRENT_YEAR, CURRENT_MONTH, CURRENT_DAY);
        int cutoff  = today - daysBack;
        boolean hasData = false;

        Log.d(TAG, "Filter: daysBack=" + daysBack
                + "  today=" + today + "  cutoff=" + cutoff);

        for (TransactionDay td : allTransactions) {
            int entryDay = toDayCount(td.year, td.month, td.day);
            Log.d(TAG, "  Entry " + td.day + " " + td.monthYear
                    + " → dayCount=" + entryDay
                    + " → " + (entryDay >= cutoff && entryDay <= today ? "SHOW" : "skip"));

            if (entryDay >= cutoff && entryDay <= today) {
                hasData = true;
                addDateHeader(td.date, td.monthYear, td.closingBalance);
                for (TransactionItem txn : td.transactions) {
                    addTransactionCard(
                            txn.title, txn.amount,
                            txn.type,  txn.subtitle, txn.timestamp);
                }
            }
        }

        if (hasData) {
            transactionContainer.setVisibility(View.VISIBLE);
            if (noDataView != null) noDataView.setVisibility(View.GONE);
        } else {
            showNoData();
        }
    }

    // =========================================================================
    //  UI HELPERS
    // =========================================================================

    private void showLoading(boolean show) {
        // Uncomment if you add a ProgressBar (id=loadingIndicator) to your XML:
        // View loading = findViewById(R.id.loadingIndicator);
        // if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNoData() {
        if (transactionContainer != null)
            transactionContainer.setVisibility(View.GONE);
        if (noDataView != null)
            noDataView.setVisibility(View.VISIBLE);
    }

    private void updateButtonUI(MaterialButton activeBtn) {
        MaterialButton[] buttons = {btn7, btn14, btnMonth};
        for (MaterialButton btn : buttons) {
            if (btn == null) continue;
            if (btn == activeBtn) {
                btn.setBackgroundColor(Color.parseColor("#2E7D32"));
                btn.setTextColor(Color.WHITE);
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundColor(Color.WHITE);
                btn.setTextColor(Color.parseColor("#2E7D32"));
                btn.setStrokeColorResource(android.R.color.darker_gray);
                btn.setStrokeWidth(2);
            }
        }
    }

    private void addDateHeader(String date, String monthYear, String balance) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.item_date_header, transactionContainer, false);
        ((TextView) v.findViewById(R.id.tvDayNumber))     .setText(date);
        ((TextView) v.findViewById(R.id.tvMonthYear))     .setText(monthYear);
        ((TextView) v.findViewById(R.id.tvClosingBalance)).setText(balance);
        transactionContainer.addView(v);
    }

    private void addTransactionCard(String title, String amt,
                                    String type, String sub, String time) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.item_transaction_card, transactionContainer, false);
        TextView tvAmt = v.findViewById(R.id.tvTxnAmount);
        ((TextView) v.findViewById(R.id.tvTxnTitle))    .setText(title);
        ((TextView) v.findViewById(R.id.tvTxnSubtitle)) .setText(sub);
        ((TextView) v.findViewById(R.id.tvTxnTimestamp)).setText(time);
        tvAmt.setText(amt);
        tvAmt.setTextColor("credit".equalsIgnoreCase(type)
                ? Color.parseColor("#2E7D32")
                : Color.parseColor("#C62828"));
        transactionContainer.addView(v);
    }

    // =========================================================================
    //  DATE UTILITIES
    // =========================================================================

    /**
     * Converts year/month/day into an integer day-count from a fixed epoch.
     * Used only for relative comparisons — not calendar display.
     */
    private int toDayCount(int year, int month, int day) {
        int[] dim = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) dim[2] = 29;
        int total = year * 365 + year / 4 - year / 100 + year / 400;
        for (int m = 1; m < month; m++) total += dim[m];
        return total + day;
    }

    /**
     * Parses "Feb 2026" → {2026, 2}
     * Falls back to CURRENT_YEAR / CURRENT_MONTH on any error.
     */
    private int[] parseMonthYear(String monthYear) {
        if (monthYear == null || monthYear.trim().isEmpty())
            return new int[]{CURRENT_YEAR, CURRENT_MONTH};

        String[] parts = monthYear.trim().split("\\s+");
        if (parts.length < 2) return new int[]{CURRENT_YEAR, CURRENT_MONTH};

        int year  = safeParseInt(parts[1], CURRENT_YEAR);
        int month = CURRENT_MONTH;

        String abbr = parts[0].toLowerCase();
        if (abbr.length() >= 3) abbr = abbr.substring(0, 3);

        switch (abbr) {
            case "jan": month = 1;  break;
            case "feb": month = 2;  break;
            case "mar": month = 3;  break;
            case "apr": month = 4;  break;
            case "may": month = 5;  break;
            case "jun": month = 6;  break;
            case "jul": month = 7;  break;
            case "aug": month = 8;  break;
            case "sep": month = 9;  break;
            case "oct": month = 10; break;
            case "nov": month = 11; break;
            case "dec": month = 12; break;
        }
        return new int[]{year, month};
    }

    private int safeParseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }
}