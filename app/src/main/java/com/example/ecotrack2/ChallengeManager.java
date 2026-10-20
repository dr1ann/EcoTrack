package com.example.ecotrack2;

import java.util.ArrayList;
import java.util.List;

public class ChallengeManager {

    private static ChallengeManager instance;

    private final List<Challenge> challenges;

    private ChallengeManager() {
        challenges = new ArrayList<>();
        seedChallenges();
    }

    public static ChallengeManager getInstance() {
        if (instance == null) {
            instance = new ChallengeManager();
        }

        return instance;
    }

    public List<Challenge> getAllChallenges() {
        // Copy the list so filtering cannot remove items from the manager.
        // Each Challenge is still shared, so both Activities see the same state.
        return new ArrayList<>(challenges);
    }

    public boolean updateStatus(int challengeId, String status) {
        if (!"Not Started".equals(status)
                && !"In Progress".equals(status)
                && !"Completed".equals(status)) {
            return false;
        }

        for (Challenge challenge : challenges) {
            // IDs stay the same even when search changes the row positions.
            if (challenge.getId() == challengeId) {
                challenge.setStatus(status);
                return true;
            }
        }

        return false;
    }

    public boolean setBookmarked(int challengeId, boolean bookmarked) {
        for (Challenge challenge : challenges) {
            if (challenge.getId() == challengeId) {
                challenge.setBookmarked(bookmarked);
                return true;
            }
        }

        return false;
    }

    public int getCompletedCount() {
        int count = 0;

        for (Challenge challenge : challenges) {
            if (challenge.getStatus().equals("Completed")) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCategory() {
        List<String> categories = new ArrayList<>();

        for (Challenge challenge : challenges) {
            if (!categories.contains(challenge.getCategory())) {
                categories.add(challenge.getCategory());
            }
        }

        return categories.size();
    }

    public int getCompletedCountByCategory(String category) {
        int count = 0;
        for (Challenge challenge : challenges) {
            if (challenge.getStatus().equals("Completed") && challenge.getCategory().equals(category)) {
                count++;
            }
        }
        return count;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();

        for (Challenge challenge : challenges) {
            if (!categories.contains(challenge.getCategory())) {
                categories.add(challenge.getCategory());
            }
        }

        return categories;
    }
    public List<String> displayCompletedPerCategory() {
        List<String> categories = new ArrayList<>(getCategories());

        List<String> result = new ArrayList<>();
        for (String category : categories) {
            result.add("• " + getCompletedCountByCategory(category) + " - " + category);
        }
        return result;
    }


    public int getTotalCount() {
        return challenges.size();
    }

    public int getCompletionPercentage() {
        int total = getTotalCount();

        if (total == 0) {
            return 0;
        }

        return (getCompletedCount() * 100) / total;
    }

    public List<String> getEarnedBadges() {
        List<String> badges = new ArrayList<>();
        int completed = getCompletedCount();

        // Team-defined practice milestones. Replace these if official thresholds are supplied.
        if (completed >= 3) {
            badges.add("Green Starter - 3 completed");
        }

        if (completed >= 6) {
            badges.add("Habit Builder - 6 completed");
        }

        if (completed >= 10) {
            badges.add("Planet Protector - 10 completed");
        }

        if (completed >= 15) {
            badges.add("Eco Champion - 15 completed");
        }

        return badges;
    }

    private void seedChallenges() {
        // DEMO PLACEHOLDERS: titles/categories match the PDF; all descriptions,
        // minutes, and impact scores below must be replaced from the official Resources .txt.
        challenges.add(new Challenge(
                1, "Plastic-Free Week", "Waste Reduction",
                "DEMO PLACEHOLDER: Avoid single-use plastics. Carry a reusable bag, bottle, and container.",
                10080, 5
        ));

        challenges.add(new Challenge(
                2, "Refill & Reuse", "Waste Reduction",
                "DEMO PLACEHOLDER: Refill a reusable bottle and reuse a food container instead of buying disposable ones.",
                10, 3
        ));

        challenges.add(new Challenge(
                3, "Smart Grocery Picks", "Waste Reduction",
                "DEMO PLACEHOLDER: Plan your groceries and choose products with less packaging.",
                30, 4
        ));

        challenges.add(new Challenge(
                4, "Power Down", "Energy Saving",
                "DEMO PLACEHOLDER: Switch off unused lights and unplug devices when safe.",
                10, 4
        ));

        challenges.add(new Challenge(
                5, "Bright Without Lights", "Energy Saving",
                "DEMO PLACEHOLDER: Use natural daylight instead of electric lights for a daytime activity.",
                60, 3
        ));

        challenges.add(new Challenge(
                6, "Charge Smart", "Energy Saving",
                "DEMO PLACEHOLDER: Unplug a fully charged device and turn on battery-saving settings.",
                5, 2
        ));

        challenges.add(new Challenge(
                7, "Five-Minute Fresh", "Water Conservation",
                "DEMO PLACEHOLDER: Take a five-minute shower and turn off the water while soaping.",
                5, 4
        ));

        challenges.add(new Challenge(
                8, "Rain Saver", "Water Conservation",
                "DEMO PLACEHOLDER: Collect rainwater in a covered container and use it to water plants.",
                20, 4
        ));

        challenges.add(new Challenge(
                9, "Cup, Not Tap", "Water Conservation",
                "DEMO PLACEHOLDER: Use a cup while brushing your teeth instead of leaving the tap running.",
                3, 2
        ));

        challenges.add(new Challenge(
                10, "Steps Over Wheels", "Sustainable Living",
                "DEMO PLACEHOLDER: Walk or cycle for a short trip along a safe, accessible route.",
                20, 5
        ));

        challenges.add(new Challenge(
                11, "Grow Green", "Sustainable Living",
                "DEMO PLACEHOLDER: Plant a seed or care for a plant suited to your local environment.",
                30, 4
        ));

        challenges.add(new Challenge(
                12, "Closet Refresh", "Sustainable Living",
                "DEMO PLACEHOLDER: Choose clothes to repair, swap, or donate instead of throwing them away.",
                60, 4
        ));

        challenges.add(new Challenge(
                13, "Clean-Up Crew", "Community Action",
                "DEMO PLACEHOLDER: Join a clean-up with friends. Wear gloves and leave hazardous waste to trained handlers.",
                60, 5
        ));

        challenges.add(new Challenge(
                14, "Spread the Green", "Community Action",
                "DEMO PLACEHOLDER: Share one practical eco-friendly habit with a friend or family member.",
                15, 3
        ));

        challenges.add(new Challenge(
                15, "Street Sweep", "Community Action",
                "DEMO PLACEHOLDER: Pick up litter along a safe section of your street and separate recyclables.",
                30, 4
        ));
    }
}
