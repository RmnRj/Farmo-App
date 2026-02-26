package com.farmo.activities.commonActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.farmo.R;

public class AboutUsActivity extends AppCompatActivity {
    
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        
        TextView btnReadMore = findViewById(R.id.btn_read_more);
        LinearLayout layoutReadMore = findViewById(R.id.layout_read_more);

        if (btnReadMore != null && layoutReadMore != null) {
            btnReadMore.setOnClickListener(v -> {
                if (isExpanded) {
                    layoutReadMore.setVisibility(View.GONE);
                    btnReadMore.setText("Read More");
                } else {
                    layoutReadMore.setVisibility(View.VISIBLE);
                    btnReadMore.setText("Read Less");
                }
                isExpanded = !isExpanded;
            });
        }
    }
}
