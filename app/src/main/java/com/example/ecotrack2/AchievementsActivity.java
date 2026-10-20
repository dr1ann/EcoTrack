package com.example.ecotrack2;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private ChallengeManager manager;
    private BottomNavigationView bottomNavigationView;

    private TextView tvCompletionCount;
    private TextView tvCompletionPercent;
    private TextView tvBadges;
    private ProgressBar progressCompletion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_achievements);
        applyWindowInsets();

        manager = ChallengeManager.getInstance();

        tvCompletionCount = findViewById(R.id.tvCompletionCount);
        tvCompletionPercent = findViewById(R.id.tvCompletionPercent);
        tvBadges = findViewById(R.id.tvBadges);
        progressCompletion = findViewById(R.id.progressCompletion);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navAchievements);
        bottomNavigationView.setOnItemSelectedListener(this::handleNavigationClick);
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.achievementsRoot);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private boolean handleNavigationClick(MenuItem item) {
        if (item.getItemId() == R.id.navChallenges) {
            // Return to the existing Challenges Activity, preserving its filters.
            finish();
            overridePendingTransition(0, 0);
            return false;
        }

        return item.getItemId() == R.id.navAchievements;
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.getMenu().findItem(R.id.navAchievements).setChecked(true);
        refreshAchievements();
    }

    private void refreshAchievements() {
        int completed = manager.getCompletedCount();
        int total = manager.getTotalCount();
        int percent = manager.getCompletionPercentage();

        tvCompletionCount.setText(completed + " / " + total + " challenges completed");
        tvCompletionPercent.setText(percent + "%");
        progressCompletion.setProgress(percent);

        updateMilestone(R.id.milestoneThree, 3, completed);
        updateMilestone(R.id.milestoneSix, 6, completed);
        updateMilestone(R.id.milestoneTen, 10, completed);
        updateMilestone(R.id.milestoneFifteen, 15, completed);

        List<String> badges = manager.getEarnedBadges();

        if (badges.isEmpty()) {
            tvBadges.setText("No badges yet. Complete 3 challenges to earn Green Starter.");
            return;
        }

        String badgeText = "";

        for (String badge : badges) {
            badgeText += "• " + badge + "\n\n";
        }

        tvBadges.setText(badgeText.trim());
    }

    private void updateMilestone(int viewId, int target, int completed) {
        View milestone = findViewById(viewId);
        boolean earned = completed >= target;

        milestone.setSelected(earned);
        milestone.setContentDescription(target + " completed challenges: " + (earned ? "earned" : "locked"));
    }
}
