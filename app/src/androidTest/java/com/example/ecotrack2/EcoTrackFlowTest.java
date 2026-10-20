package com.example.ecotrack2;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EcoTrackFlowTest {

    private ActivityScenario<MainActivity> scenario;
    private ChallengeManager manager;

    @Before
    public void prepare() {
        manager = ChallengeManager.getInstance();
        resetProgress();
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void cleanUp() {
        scenario.close();
        resetProgress();
    }

    private void resetProgress() {
        for (Challenge challenge : manager.getAllChallenges()) {
            manager.updateStatus(challenge.getId(), "Not Started");
            manager.setBookmarked(challenge.getId(), false);
        }
    }

    private void assertRowCount(int expected) {
        scenario.onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerChallenges);
            assertEquals(expected, recyclerView.getAdapter().getItemCount());
        });
    }

    private void searchFor(String query) {
        onView(withId(R.id.action_search)).perform(click());
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(replaceText(query), closeSoftKeyboard());
    }

    private void chooseFilter(String label) {
        onView(withId(R.id.action_filter)).perform(click());
        onView(withText(label)).perform(click());
    }

    @Test
    public void allFiveCategoriesShowTheirThreeChallenges() {
        int[] buttons = {
                R.id.categoryWaste,
                R.id.categoryEnergy,
                R.id.categoryWater,
                R.id.categoryLiving,
                R.id.categoryCommunity
        };

        String[] firstTitles = {
                "Plastic-Free Week",
                "Power Down",
                "Five-Minute Fresh",
                "Steps Over Wheels",
                "Clean-Up Crew"
        };

        for (int i = 0; i < buttons.length; i++) {
            onView(withId(buttons[i])).perform(scrollTo(), click());
            assertRowCount(3);
            onView(withText(firstTitles[i])).check(matches(isDisplayed()));
        }

        onView(withId(R.id.categoryAll)).perform(scrollTo(), click());
        assertRowCount(15);
    }

    @Test
    public void filteredChallengeUpdatesTheCorrectIdAndAllStatusesCanBeReopened() {
        searchFor("Cup, Not Tap");
        assertRowCount(1);
        onView(allOf(withId(R.id.tvTitle), withText("Cup, Not Tap"))).perform(click());
        onView(withId(R.id.tvSheetDescription)).check(matches(withText(containsString("DEMO PLACEHOLDER:"))));
        onView(withId(R.id.statusInProgress)).perform(scrollTo(), click());
        onView(withId(R.id.btnSaveStatus)).perform(scrollTo(), click());
        assertEquals("In Progress", manager.getAllChallenges().get(8).getStatus());

        onView(allOf(withId(R.id.tvTitle), withText("Cup, Not Tap"))).perform(click());
        onView(withId(R.id.statusInProgress)).check(matches(isChecked()));
        onView(withId(R.id.statusCompleted)).perform(scrollTo(), click());
        onView(withId(R.id.btnSaveStatus)).perform(scrollTo(), click());
        assertEquals("Completed", manager.getAllChallenges().get(8).getStatus());
        assertEquals("Not Started", manager.getAllChallenges().get(0).getStatus());

        onView(withId(R.id.navAchievements)).perform(click());
        onView(withId(R.id.tvCompletionCount)).check(matches(withText("1 / 15 challenges completed")));
        onView(withId(R.id.tvCompletionPercent)).check(matches(withText("6%")));
        onView(withId(R.id.navChallenges)).perform(click());
        assertRowCount(1);

        onView(allOf(withId(R.id.tvTitle), withText("Cup, Not Tap"))).perform(click());
        onView(withId(R.id.statusCompleted)).check(matches(isChecked()));
        onView(withId(R.id.statusNotStarted)).perform(scrollTo(), click());
        onView(withId(R.id.btnSaveStatus)).perform(scrollTo(), click());
        assertEquals(0, manager.getCompletedCount());
    }

    @Test
    public void savedOnlyRefreshesImmediatelyWhenBookmarkIsRemoved() {
        searchFor("Refill");
        onView(withContentDescription("Save challenge: Refill & Reuse")).perform(click());
        chooseFilter("Saved only");
        assertRowCount(1);

        onView(withContentDescription("Remove saved challenge: Refill & Reuse")).perform(click());
        assertRowCount(0);
        onView(withId(R.id.tvEmpty)).check(matches(isDisplayed()));

        chooseFilter("All statuses");
        assertRowCount(1);
        assertFalse(manager.getAllChallenges().get(1).isBookmarked());
    }

    @Test
    public void searchCategoryStatusAndSavedFiltersCombineAndCanBeReset() {
        onView(withId(R.id.categoryWaste)).perform(click());
        searchFor("rEfIlL");
        assertRowCount(1);

        chooseFilter("Completed");
        assertRowCount(0);
        onView(withId(R.id.btnResetFilters)).perform(click());
        assertRowCount(15);

        chooseFilter("Saved only");
        assertRowCount(0);
        onView(withId(R.id.btnResetFilters)).perform(click());
        assertRowCount(15);
    }

    @Test
    public void badgeDisplayRecomputesAfterCompletionCountChanges() {
        scenario.onActivity(activity -> {
            for (int id = 1; id <= 3; id++) {
                manager.updateStatus(id, "Completed");
            }
        });

        onView(withId(R.id.navAchievements)).perform(click());
        onView(withId(R.id.tvCompletionPercent)).check(matches(withText("20%")));
        onView(withId(R.id.tvBadges)).check(matches(withText(containsString("Green Starter - 3 completed"))));
        onView(withId(R.id.navChallenges)).perform(click());

        scenario.onActivity(activity -> manager.updateStatus(3, "Not Started"));
        onView(withId(R.id.navAchievements)).perform(click());
        onView(withId(R.id.tvCompletionPercent)).check(matches(withText("13%")));
        onView(withId(R.id.tvBadges)).check(matches(withText(containsString("No badges yet"))));
    }

    @Test
    public void rotationAndNavigationKeepFiltersAndDoNotDuplicateSeedData() {
        onView(withId(R.id.bottomNavigationView)).check(matches(isAssignableFrom(BottomNavigationView.class)));
        onView(withId(R.id.navChallenges)).check(matches(isSelected()));
        scenario.onActivity(activity -> manager.setBookmarked(2, true));
        onView(withId(R.id.categoryWaste)).perform(click());
        searchFor("Refill");
        chooseFilter("Saved only");
        chooseFilter("Not Started");

        scenario.recreate();

        onView(withId(R.id.categoryWaste)).check(matches(isChecked()));
        onView(withId(androidx.appcompat.R.id.search_src_text)).check(matches(withText("Refill")));
        assertRowCount(1);

        for (int i = 0; i < 3; i++) {
            onView(withId(R.id.navAchievements)).perform(click());
            onView(withId(R.id.navAchievements)).check(matches(isSelected()));
            onView(withId(R.id.navChallenges)).perform(click());
            onView(withId(R.id.navChallenges)).check(matches(isSelected()));
        }

        assertRowCount(1);
        assertEquals(15, manager.getTotalCount());
        assertTrue(manager.getAllChallenges().get(1).isBookmarked());
    }

    @Test
    public void bottomSheetBookmarkAndCloseActionsWork() {
        onView(withText("Plastic-Free Week")).perform(click());
        onView(withId(R.id.btnSheetBookmark)).perform(scrollTo(), click());
        assertTrue(manager.getAllChallenges().get(0).isBookmarked());

        onView(withText("Plastic-Free Week")).perform(click());
        onView(withId(R.id.btnSheetBookmark)).check(matches(withText("Remove from saved")));
        onView(withId(R.id.btnCloseSheet)).perform(scrollTo(), click());
        assertRowCount(15);
    }
}
