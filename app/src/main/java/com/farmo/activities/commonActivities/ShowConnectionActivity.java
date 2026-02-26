package com.farmo.activities.commonActivities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farmo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ShowConnectionActivity extends AppCompatActivity
        implements ConnectionAdapter.OnConnectionActionListener {

    private static final String TAG = "ShowConnectionActivity";

    // Views
    private AppCompatImageButton btnBack;
    private TextView tvScreenTitle;
    private TextView tvUserCount;
    private Button btnTabConnections;
    private Button btnTabSent;
    private Button btnTabPending;
    private RecyclerView rvConnections;

    // Data
    private final List<Connection> allConnected = new ArrayList<>();
    private final List<Connection> allSent = new ArrayList<>();
    private final List<Connection> allPending = new ArrayList<>();
    private final List<Connection> displayedList = new ArrayList<>();
    private ConnectionAdapter adapter;

    // State
    private enum Tab {CONNECTIONS, SENT, PENDING}
    private Tab activeTab = Tab.CONNECTIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_show_connection);
        } catch (Exception e) {
            Toast.makeText(this, "Layout error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "setContentView failed", e);
            finish();
            return;
        }
        try {
            bindViews();
        } catch (Exception e) {
            Toast.makeText(this, "View error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "bindViews failed", e);
            finish();
            return;
        }
        try {
            setupRecyclerView();
        } catch (Exception e) {
            Toast.makeText(this, "RecyclerView error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "setupRecyclerView failed", e);
            finish();
            return;
        }
        try {
            loadAllDataFromJson();
        } catch (Exception e) {
            Toast.makeText(this, "Data error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "loadData failed", e);
        }
        try {
            switchToTab(Tab.CONNECTIONS);
        } catch (Exception e) {
            Toast.makeText(this, "UI error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "switchToTab failed", e);
        }
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btn_back);
        tvScreenTitle = findViewById(R.id.tv_screen_title);
        tvUserCount = findViewById(R.id.tv_user_count);
        btnTabConnections = findViewById(R.id.btn_tab_connections);
        btnTabSent = findViewById(R.id.btn_tab_sent);
        btnTabPending = findViewById(R.id.btn_tab_pending);
        rvConnections = findViewById(R.id.rv_connections);

        if (btnBack == null) throw new NullPointerException("btn_back not found");
        if (tvScreenTitle == null) throw new NullPointerException("tv_screen_title not found");
        if (tvUserCount == null) throw new NullPointerException("tv_user_count not found");
        if (btnTabConnections == null)
            throw new NullPointerException("btn_tab_connections not found");
        if (btnTabSent == null) throw new NullPointerException("btn_tab_sent not found");
        if (btnTabPending == null) throw new NullPointerException("btn_tab_pending not found");
        if (rvConnections == null) throw new NullPointerException("rv_connections not found");

        btnBack.setOnClickListener(v -> onBackPressed());
        btnTabConnections.setOnClickListener(v -> switchToTab(Tab.CONNECTIONS));
        btnTabSent.setOnClickListener(v -> switchToTab(Tab.SENT));
        btnTabPending.setOnClickListener(v -> switchToTab(Tab.PENDING));
    }

    private void setupRecyclerView() {
        adapter = new ConnectionAdapter(this, displayedList, this);
        rvConnections.setLayoutManager(new LinearLayoutManager(this));
        rvConnections.setHasFixedSize(false);
        rvConnections.setAdapter(adapter);
    }

    // ── DATA LAYER ─────────────────────────────────────────────────────────

    private void loadAllDataFromJson() {
        allConnected.clear();
        allSent.clear();
        allPending.clear();
        try {
            String json = readJsonFromAssets("connections.json");
            if (json != null && !json.isEmpty()) parseJson(json);
        } catch (Exception e) {
            Log.e(TAG, "JSON error", e);
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (allConnected.isEmpty() && allSent.isEmpty() && allPending.isEmpty()) {
            loadHardcodedData();
            Toast.makeText(this, "Loaded demo data (no JSON file found)", Toast.LENGTH_SHORT).show();
        }
    }

    private String readJsonFromAssets(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Cannot open asset '" + fileName + "': " + e.getMessage());
            return null;
        }
    }

    private void parseJson(String json) {
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject obj = array.getJSONObject(i);
                    Connection conn = new Connection(
                            obj.getString("user_id"),
                            obj.getString("full_name"),
                            obj.getString("profile_pic"),
                            statusFromString(obj.optString("status", "connected"))
                    );
                    bucketByStatus(conn);
                } catch (Exception ex) {
                    Log.w(TAG, "Skipping item " + i + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "parseJson error", e);
            throw new RuntimeException("JSON parse failed: " + e.getMessage());
        }
    }

    private Connection.Status statusFromString(String s) {
        if (s == null) return Connection.Status.CONNECTED;
        switch (s.toLowerCase().trim()) {
            case "pending":
                return Connection.Status.PENDING;
            case "sent":
                return Connection.Status.SENT;
            default:
                return Connection.Status.CONNECTED;
        }
    }

    private void bucketByStatus(Connection c) {
        switch (c.getStatus()) {
            case CONNECTED:
                allConnected.add(c);
                break;
            case SENT:
                allSent.add(c);
                break;
            case PENDING:
                allPending.add(c);
                break;
        }
    }

    // ── UI STATE ───────────────────────────────────────────────────────────

    private void switchToTab(Tab tab) {
        activeTab = tab;
        updateTabStyles();
        updateHeader();
        updateDisplayedList();
    }

    private void updateHeader() {
        int currentCount = displayedList.size();
        int totalCount = getTotalCount();

        // Update count text
        tvUserCount.setText(currentCount + " of " + totalCount + " users");
    }

    private int getTotalCount() {
        switch (activeTab) {
            case CONNECTIONS:
                return allConnected.size();
            case SENT:
                return allSent.size();
            case PENDING:
                return allPending.size();
            default:
                return 0;
        }
    }

    private void updateDisplayedList() {
        displayedList.clear();
        switch (activeTab) {
            case CONNECTIONS:
                displayedList.addAll(allConnected);
                break;
            case SENT:
                displayedList.addAll(allSent);
                break;
            case PENDING:
                displayedList.addAll(allPending);
                break;
        }
        adapter.notifyDataSetChanged();
        updateHeader();
    }

    private void updateTabStyles() {
        btnTabConnections.setBackgroundResource(R.drawable.bg_tab_inactive);
        btnTabSent.setBackgroundResource(R.drawable.bg_tab_inactive);
        btnTabPending.setBackgroundResource(R.drawable.bg_tab_inactive);
        btnTabConnections.setTextColor(0xFF3B82F6);
        btnTabSent.setTextColor(0xFF3B82F6);
        btnTabPending.setTextColor(0xFF3B82F6);
        switch (activeTab) {
            case CONNECTIONS:
                btnTabConnections.setBackgroundResource(R.drawable.bg_tab_active);
                btnTabConnections.setTextColor(0xFFFFFFFF);
                break;
            case SENT:
                btnTabSent.setBackgroundResource(R.drawable.bg_tab_active);
                btnTabSent.setTextColor(0xFFFFFFFF);
                break;
            case PENDING:
                btnTabPending.setBackgroundResource(R.drawable.bg_tab_active);
                btnTabPending.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    // ── Adapter Callbacks ──────────────────────────────────────────────────

    @Override
    public void onDelete(Connection connection, int position) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Connection")
                    .setMessage("Are you sure you want to delete " + connection.getFullName() + " from your connections?")
                    .setPositiveButton("Confirm", (d, w) -> {
                        try {
                            displayedList.remove(position);
                            allConnected.remove(connection);
                            adapter.notifyItemRemoved(position);
                            updateHeader();
                            Toast.makeText(this, connection.getFullName() + " deleted", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Delete error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccept(Connection connection, int position) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Accept Request")
                    .setMessage("Accept connection request from " + connection.getFullName() + "?")
                    .setPositiveButton("Confirm", (d, w) -> {
                        try {
                            allPending.remove(connection);
                            connection.setStatus(Connection.Status.CONNECTED);
                            allConnected.add(connection);
                            displayedList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateHeader();
                            Toast.makeText(this, "Connected with " + connection.getFullName(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Accept error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReject(Connection connection, int position) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Reject Request")
                    .setMessage("Reject connection request from " + connection.getFullName() + "?")
                    .setPositiveButton("Confirm", (d, w) -> {
                        try {
                            allPending.remove(connection);
                            displayedList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateHeader();
                            Toast.makeText(this, connection.getFullName() + " rejected", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Reject error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelRequest(Connection connection, int position) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Request")
                    .setMessage("Cancel connection request to " + connection.getFullName() + "?")
                    .setPositiveButton("Confirm", (d, w) -> {
                        try {
                            allSent.remove(connection);
                            displayedList.remove(position);
                            adapter.notifyItemRemoved(position);
                            updateHeader();
                            Toast.makeText(this, "Request to " + connection.getFullName() + " cancelled", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Cancel error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ── Hardcoded fallback ─────────────────────────────────────────────────

    private void loadHardcodedData() {
        String[][] connected = {
                {"001", "Alice Johnson", "https://i.pravatar.cc/150?img=1"},
                {"002", "Bob Smith", "https://i.pravatar.cc/150?img=2"},
                {"003", "Carol Williams", "https://i.pravatar.cc/150?img=3"},
                {"004", "David Brown", "https://i.pravatar.cc/150?img=4"},
                {"005", "Eva Martinez", "https://i.pravatar.cc/150?img=5"},
                {"006", "Frank Davis", "https://i.pravatar.cc/150?img=6"},
                {"007", "Grace Wilson", "https://i.pravatar.cc/150?img=7"},
                {"008", "Henry Moore", "https://i.pravatar.cc/150?img=8"},
                {"009", "Isabella Taylor", "https://i.pravatar.cc/150?img=9"},
                {"010", "James Anderson", "https://i.pravatar.cc/150?img=10"},
                {"011", "Karen Thomas", "https://i.pravatar.cc/150?img=11"},
                {"012", "Liam Jackson", "https://i.pravatar.cc/150?img=12"},
                {"013", "Mia White", "https://i.pravatar.cc/150?img=13"},
                {"014", "Noah Harris", "https://i.pravatar.cc/150?img=14"},
                {"015", "Olivia Martin", "https://i.pravatar.cc/150?img=15"},
                {"016", "Peter Garcia", "https://i.pravatar.cc/150?img=16"},
                {"017", "Quinn Rodriguez", "https://i.pravatar.cc/150?img=17"},
                {"018", "Rachel Lewis", "https://i.pravatar.cc/150?img=18"},
                {"019", "Samuel Lee", "https://i.pravatar.cc/150?img=19"},
                {"020", "Taylor Walker", "https://i.pravatar.cc/150?img=20"},
        };
        for (String[] r : connected)
            allConnected.add(new Connection(r[0], r[1], r[2], Connection.Status.CONNECTED));

        String[][] pending = {
                {"021", "Uma Hall", "https://i.pravatar.cc/150?img=21"},
                {"022", "Victor Allen", "https://i.pravatar.cc/150?img=22"},
                {"023", "Wendy Young", "https://i.pravatar.cc/150?img=23"},
                {"024", "Xander Hernandez", "https://i.pravatar.cc/150?img=24"},
                {"025", "Yara King", "https://i.pravatar.cc/150?img=25"},
                {"026", "Zoe Wright", "https://i.pravatar.cc/150?img=26"},
                {"027", "Aaron Scott", "https://i.pravatar.cc/150?img=27"},
                {"033", "Maya Patel", "https://i.pravatar.cc/150?img=33"},
                {"034", "Lena Cruz", "https://i.pravatar.cc/150?img=34"},
                {"035", "Omar Hassan", "https://i.pravatar.cc/150?img=35"},
        };
        for (String[] r : pending)
            allPending.add(new Connection(r[0], r[1], r[2], Connection.Status.PENDING));

        String[][] sent = {
                {"028", "Bella Torres", "https://i.pravatar.cc/150?img=28"},
                {"029", "Carlos Nguyen", "https://i.pravatar.cc/150?img=29"},
                {"030", "Diana Hill", "https://i.pravatar.cc/150?img=30"},
                {"031", "Ethan Flores", "https://i.pravatar.cc/150?img=31"},
                {"032", "Fiona Green", "https://i.pravatar.cc/150?img=32"},
                {"036", "Jake Reeves", "https://i.pravatar.cc/150?img=36"},
                {"037", "Nina Okafor", "https://i.pravatar.cc/150?img=37"},
                {"038", "Leo Zhang", "https://i.pravatar.cc/150?img=38"},
        };
        for (String[] r : sent)
            allSent.add(new Connection(r[0], r[1], r[2], Connection.Status.SENT));
    }
}