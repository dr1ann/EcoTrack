package com.example.ecotrack2;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ChallengeAdapterTest {

    private List<Challenge> source;
    private ChallengeAdapter adapter;

    @Before
    public void prepare() {
        source = new ArrayList<>();
        source.add(new Challenge(1, "Refill & Reuse", "Waste Reduction", "Demo", 5, 3));
        source.add(new Challenge(2, "Rain Saver", "Water Conservation", "Demo", 20, 4));
        source.add(new Challenge(3, "Power Down", "Energy Saving", "Demo", 10, 4));

        source.get(0).setStatus("Completed");
        source.get(0).setBookmarked(true);
        source.get(1).setStatus("In Progress");
        source.get(1).setBookmarked(true);

        adapter = new ChallengeAdapter(source, new ChallengeAdapter.OnChallengeClickListener() {
            @Override
            public void onChallengeClick(Challenge challenge) {
            }

            @Override
            public void onBookmarkClick(Challenge challenge) {
            }
        });
    }

    @Test
    public void categoryFilterIsReversible() {
        adapter.setCurrentCategory("water conservation");
        assertEquals(1, adapter.getItemCount());
        adapter.setCurrentCategory("all");
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void searchCategoryStatusAndSavedConditionsCombineWithAnd() {
        adapter.setCurrentSearchText("  REFILL  ");
        adapter.setCurrentCategory("Waste Reduction");
        adapter.setCurrentStatus("Completed");
        adapter.setSavedOnly(true);
        assertEquals(1, adapter.getItemCount());
        adapter.setCurrentStatus("Not Started");
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void clearingSearchRestoresApplicableRows() {
        adapter.setCurrentStatus("Not Started");
        adapter.setCurrentSearchText("Rain");
        assertEquals(0, adapter.getItemCount());
        adapter.setCurrentSearchText("");
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void updateListRetainsActiveFiltersAndUsesChangedBookmarks() {
        adapter.setSavedOnly(true);
        adapter.setCurrentStatus("Completed");
        assertEquals(1, adapter.getItemCount());
        source.get(0).setBookmarked(false);
        adapter.updateList(source);
        assertEquals(0, adapter.getItemCount());
        source.get(0).setBookmarked(true);
        adapter.updateList(source);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void sourceListStructureIsCopied() {
        source.clear();
        adapter.setCurrentCategory("all");
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void clearingFiltersRestoresAllChoices() {
        adapter.setCurrentSearchText("Missing");
        adapter.setCurrentSearchText("");
        adapter.setCurrentCategory("all");
        adapter.setCurrentStatus("all");
        assertEquals(3, adapter.getItemCount());
    }
}
