package com.example.ecotrack2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity
        implements ChallengeAdapter.OnChallengeClickListener {

    private ChallengeManager manager;
    private ChallengeAdapter adapter;

    private Toolbar searchToolbar;
    private Toolbar filterToolbar;
    private BottomNavigationView bottomNavigationView;
    private RadioGroup categoryGroup;
    private SearchView searchView;
    private MenuItem searchItem;

    private String searchText = "";
    private String selectedStatus = "all";
    private int selectedCategoryId = R.id.categoryAll;
    private boolean savedOnly = false;
    private boolean searchExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        applyWindowInsets();

        manager = ChallengeManager.getInstance();
        restoreFilterState(savedInstanceState);
        bindViews();
        setupRecyclerView();
        setupCategoryButtons();
        setupSearchToolbar();
        setupFilterToolbar();
        setupBottomNavigation();
        applyCurrentFilters();

        findViewById(R.id.btnResetFilters).setOnClickListener(view -> resetFilters());
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
            );

            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void bindViews() {
        searchToolbar = findViewById(R.id.searchToolbar);
        filterToolbar = findViewById(R.id.filterToolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        categoryGroup = findViewById(R.id.categoryGroup);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerChallenges);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChallengeAdapter(manager.getAllChallenges(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupCategoryButtons() {
        categoryGroup.check(selectedCategoryId);
        categoryGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedCategoryId = checkedId;
            adapter.setCurrentCategory(getSelectedCategory());
            updateListSummary();
        });
    }

    private String getSelectedCategory() {
        if (selectedCategoryId == R.id.categoryAll) {
            return "all";
        }

        RadioButton selectedButton = findViewById(selectedCategoryId);
        return selectedButton.getText().toString();
    }

    private void setupSearchToolbar() {
        searchToolbar.inflateMenu(R.menu.search_menu);

        searchItem = searchToolbar.getMenu().findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search challenges");

        // Give search the extra room normally used by the summary card.
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                findViewById(R.id.homeHero).setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                findViewById(R.id.homeHero).setVisibility(View.VISIBLE);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                adapter.setCurrentSearchText(newText);
                updateListSummary();
                return true;
            }
        });

        if (searchExpanded || !searchText.isEmpty()) {
            String restoredSearch = searchText;
            searchItem.expandActionView();
            searchView.setQuery(restoredSearch, false);
            searchView.clearFocus();
        }
    }

    private void setupFilterToolbar() {
        // Independent toolbars have independent menus and listeners.
        filterToolbar.inflateMenu(R.menu.filter_menu);
        filterToolbar.setOnMenuItemClickListener(this::handleFilterClick);
        updateCheckedMenuItems();
    }

    private boolean handleFilterClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.filter_all) {
            selectedStatus = "all";
            savedOnly = false;
        } else if (id == R.id.filter_not_started) {
            selectedStatus = "Not Started";
        } else if (id == R.id.filter_in_progress) {
            selectedStatus = "In Progress";
        } else if (id == R.id.filter_completed) {
            selectedStatus = "Completed";
        } else if (id == R.id.filter_saved) {
            savedOnly = !savedOnly;
        } else if (id == R.id.filter_reset) {
            resetFilters();
            return true;
        } else {
            return false;
        }

        adapter.setCurrentStatus(selectedStatus);
        adapter.setSavedOnly(savedOnly);
        updateCheckedMenuItems();
        updateListSummary();
        return true;
    }

    private void updateCheckedMenuItems() {
        int statusItemId = R.id.filter_all;

        if (selectedStatus.equals("Not Started")) {
            statusItemId = R.id.filter_not_started;
        } else if (selectedStatus.equals("In Progress")) {
            statusItemId = R.id.filter_in_progress;
        } else if (selectedStatus.equals("Completed")) {
            statusItemId = R.id.filter_completed;
        }

        filterToolbar.getMenu().findItem(statusItemId).setChecked(true);
        filterToolbar.getMenu().findItem(R.id.filter_saved).setChecked(savedOnly);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navChallenges);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navAchievements) {
                Intent intent = new Intent(this, AchievementsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);

                // The other Activity selects Achievements on its own navigation view.
                return false;
            }

            return item.getItemId() == R.id.navChallenges;
        });
    }

    private void applyCurrentFilters() {
        adapter.setCurrentSearchText(searchText);
        adapter.setCurrentCategory(getSelectedCategory());
        adapter.setCurrentStatus(selectedStatus);
        adapter.setSavedOnly(savedOnly);
        updateListSummary();
    }

    private void resetFilters() {
        searchText = "";
        selectedStatus = "all";
        savedOnly = false;
        selectedCategoryId = R.id.categoryAll;

        searchView.setQuery("", false);
        searchView.clearFocus();
        categoryGroup.check(R.id.categoryAll);
        updateCheckedMenuItems();
        applyCurrentFilters();
    }

    private void updateListSummary() {
        int count = adapter.getItemCount();
        String statusLabel = selectedStatus.equals("all") ? "All statuses" : selectedStatus;

        filterToolbar.setTitle(count + (count == 1 ? " challenge" : " challenges"));
        filterToolbar.setSubtitle(savedOnly ? statusLabel + " • Saved only" : statusLabel);
        findViewById(R.id.emptyState).setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void refreshChallenges() {
        // updateList retains the Adapter's current search/category/status/saved filters.
        adapter.updateList(manager.getAllChallenges());
        updateListSummary();

        int completed = manager.getCompletedCount();
        int total = manager.getTotalCount();

        TextView progressLabel = findViewById(R.id.tvHomeProgress);
        ProgressBar progressBar = findViewById(R.id.homeProgress);

        progressLabel.setText(completed + " of " + total + " challenges completed");
        progressBar.setMax(total);
        progressBar.setProgress(completed);
    }

    @Override
    public void onChallengeClick(Challenge challenge) {
        showChallengeSheet(challenge);
    }

    @Override
    public void onBookmarkClick(Challenge challenge) {
        toggleBookmark(challenge);
    }

    private void showChallengeSheet(Challenge challenge) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_challenge, null);

        TextView title = content.findViewById(R.id.tvSheetTitle);
        TextView category = content.findViewById(R.id.tvSheetCategory);
        TextView description = content.findViewById(R.id.tvSheetDescription);
        TextView time = content.findViewById(R.id.tvSheetTime);
        TextView impact = content.findViewById(R.id.tvSheetImpact);
        RadioGroup radioStatus = content.findViewById(R.id.radioStatus);
        Button saveStatus = content.findViewById(R.id.btnSaveStatus);
        Button bookmark = content.findViewById(R.id.btnSheetBookmark);

        title.setText(challenge.getTitle());
        category.setText(challenge.getCategory());
        description.setText(challenge.getDescription());
        time.setText(challenge.getEstimatedTimeLabel() + " (demo)");
        impact.setText(challenge.getImpactScore() + " / 5 (demo)");
        bookmark.setText(challenge.isBookmarked() ? "Remove from saved" : "Save challenge");
        selectCurrentStatus(radioStatus, challenge.getStatus());

        saveStatus.setOnClickListener(view -> saveStatus(challenge, radioStatus, sheet));
        bookmark.setOnClickListener(view -> {
            toggleBookmark(challenge);
            sheet.dismiss();
        });
        content.findViewById(R.id.btnCloseSheet).setOnClickListener(view -> sheet.dismiss());

        sheet.setContentView(content);
        sheet.show();
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void selectCurrentStatus(RadioGroup radioStatus, String status) {
        if (status.equalsIgnoreCase("Completed")) {
            radioStatus.check(R.id.statusCompleted);
        } else if (status.equalsIgnoreCase("In Progress")) {
            radioStatus.check(R.id.statusInProgress);
        } else {
            radioStatus.check(R.id.statusNotStarted);
        }
    }

    private void saveStatus(Challenge challenge, RadioGroup radioStatus, BottomSheetDialog sheet) {
        int checkedId = radioStatus.getCheckedRadioButtonId();
        RadioButton selectedButton = radioStatus.findViewById(checkedId);

        if (selectedButton == null) {
            Snackbar.make(radioStatus, "Choose a progress status", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String status = selectedButton.getText().toString();

        if (manager.updateStatus(challenge.getId(), status)) {
            sheet.dismiss();
            refreshChallenges();
            showMessage("Status updated to " + status);
        } else {
            showMessage("Unable to update this challenge");
        }
    }

    private void toggleBookmark(Challenge challenge) {
        boolean bookmarked = !challenge.isBookmarked();

        if (manager.setBookmarked(challenge.getId(), bookmarked)) {
            refreshChallenges();
            showMessage(bookmarked ? "Challenge saved" : "Removed from saved");
        } else {
            showMessage("Unable to update this bookmark");
        }
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT)
                .setAnchorView(bottomNavigationView)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigationView.getMenu().findItem(R.id.navChallenges).setChecked(true);
        refreshChallenges();
    }

    private void restoreFilterState(Bundle state) {
        if (state == null) {
            return;
        }

        searchText = state.getString("searchText", "");
        selectedStatus = state.getString("selectedStatus", "all");
        selectedCategoryId = state.getInt("selectedCategoryId", R.id.categoryAll);
        savedOnly = state.getBoolean("savedOnly");
        searchExpanded = state.getBoolean("searchExpanded");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("searchText", searchText);
        state.putString("selectedStatus", selectedStatus);
        state.putInt("selectedCategoryId", selectedCategoryId);
        state.putBoolean("savedOnly", savedOnly);
        state.putBoolean("searchExpanded", searchItem.isActionViewExpanded());

        super.onSaveInstanceState(state);
    }
}
