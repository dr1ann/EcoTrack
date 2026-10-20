package com.example.ecotrack2;

public class Challenge {

    private final int id;
    private final String title;
    private final String category;
    private final String description;
    private final int estimatedMinutes;
    private final int impactScore;

    private String status;
    private boolean bookmarked;

    public Challenge(
            int id,
            String title,
            String category,
            String description,
            int estimatedMinutes,
            int impactScore
    ) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.estimatedMinutes = estimatedMinutes;
        this.impactScore = impactScore;
        this.status = "Not Started";
        this.bookmarked = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public String getEstimatedTimeLabel() {
        if (estimatedMinutes % 1440 == 0) {
            int days = estimatedMinutes / 1440;
            return days + (days == 1 ? " day" : " days");
        }

        if (estimatedMinutes % 60 == 0) {
            int hours = estimatedMinutes / 60;
            return hours + (hours == 1 ? " hour" : " hours");
        }

        return estimatedMinutes + " min";
    }

    public int getImpactScore() {
        return impactScore;
    }

    public String getStatus() {
        return status;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
}
