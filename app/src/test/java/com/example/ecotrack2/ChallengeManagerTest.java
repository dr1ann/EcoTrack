package com.example.ecotrack2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ChallengeManagerTest {

    private ChallengeManager manager;

    @Before
    public void prepare() {
        manager = ChallengeManager.getInstance();
        resetProgress();
    }

    @After
    public void cleanUp() {
        resetProgress();
    }

    private void resetProgress() {
        for (Challenge challenge : manager.getAllChallenges()) {
            manager.updateStatus(challenge.getId(), "Not Started");
            manager.setBookmarked(challenge.getId(), false);
        }
    }

    @Test
    public void seedHasFifteenUniqueIdsAndDefaultState() {
        Set<Integer> ids = new HashSet<>();

        for (Challenge challenge : manager.getAllChallenges()) {
            assertTrue(ids.add(challenge.getId()));
            assertEquals("Not Started", challenge.getStatus());
            assertFalse(challenge.isBookmarked());
            assertTrue(challenge.getImpactScore() >= 1 && challenge.getImpactScore() <= 5);
            assertTrue(challenge.getEstimatedMinutes() > 0);
            assertTrue(challenge.getDescription().startsWith("DEMO PLACEHOLDER:"));
        }

        assertEquals(15, ids.size());
        assertEquals(15, manager.getTotalCount());
        assertEquals(0, manager.getCompletedCount());
    }

    @Test
    public void everyCategoryHasThreeChallenges() {
        String[] categories = {
                "Waste Reduction", "Energy Saving", "Water Conservation",
                "Sustainable Living", "Community Action"
        };

        for (String category : categories) {
            int count = 0;

            for (Challenge challenge : manager.getAllChallenges()) {
                if (challenge.getCategory().equals(category)) {
                    count++;
                }
            }

            assertEquals(category, 3, count);
        }
    }

    @Test
    public void allThreeStatusesCanBeSelectedAndReset() {
        assertTrue(manager.updateStatus(1, "In Progress"));
        assertEquals("In Progress", manager.getAllChallenges().get(0).getStatus());
        assertTrue(manager.updateStatus(1, "Completed"));
        assertEquals(1, manager.getCompletedCount());
        assertEquals(6, manager.getCompletionPercentage());
        assertTrue(manager.updateStatus(1, "Not Started"));
        assertEquals(0, manager.getCompletedCount());
    }

    @Test
    public void invalidStatusesAndMissingIdsAreRejected() {
        assertFalse(manager.updateStatus(999, "Completed"));
        assertFalse(manager.updateStatus(1, "Unknown"));
        assertFalse(manager.updateStatus(1, null));
        assertFalse(manager.setBookmarked(999, true));
        assertEquals(0, manager.getCompletedCount());
    }

    @Test
    public void bookmarkIsIndependentFromStatus() {
        assertTrue(manager.setBookmarked(9, true));
        Challenge challenge = manager.getAllChallenges().get(8);
        assertTrue(challenge.isBookmarked());
        assertEquals("Not Started", challenge.getStatus());
        manager.updateStatus(9, "Completed");
        manager.setBookmarked(9, false);
        assertFalse(challenge.isBookmarked());
        assertEquals("Completed", challenge.getStatus());
    }

    @Test
    public void repeatedCompletionNeverIncreasesCountTwice() {
        manager.updateStatus(15, "Completed");
        manager.updateStatus(15, "Completed");
        assertEquals(1, manager.getCompletedCount());
        assertEquals("Not Started", manager.getAllChallenges().get(0).getStatus());
    }

    @Test
    public void badgesUnlockAtExactTeamDefinedThresholds() {
        int[] expectedBadges = {0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4};

        for (int completed = 0; completed <= 15; completed++) {
            if (completed > 0) {
                manager.updateStatus(completed, "Completed");
            }

            assertEquals("Completed: " + completed, expectedBadges[completed], manager.getEarnedBadges().size());
        }

        assertEquals(100, manager.getCompletionPercentage());
        manager.updateStatus(15, "Not Started");
        assertEquals(3, manager.getEarnedBadges().size());
    }

    @Test
    public void returnedListDoesNotExposeManagerListStructure() {
        List<Challenge> copy = manager.getAllChallenges();
        copy.clear();
        assertEquals(15, manager.getTotalCount());
        assertSame(manager, ChallengeManager.getInstance());
    }

    @Test
    public void timeLabelsKeepExactDurationsReadable() {
        assertEquals("7 days", new Challenge(99, "Title", "Category", "Demo", 10080, 3).getEstimatedTimeLabel());
        assertEquals("1 hour", new Challenge(99, "Title", "Category", "Demo", 60, 3).getEstimatedTimeLabel());
        assertEquals("2 hours", new Challenge(99, "Title", "Category", "Demo", 120, 3).getEstimatedTimeLabel());
        assertEquals("90 min", new Challenge(99, "Title", "Category", "Demo", 90, 3).getEstimatedTimeLabel());
    }
}
