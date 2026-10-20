package com.example.ecotrack2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    public interface OnChallengeClickListener {
        void onChallengeClick(Challenge challenge);

        void onBookmarkClick(Challenge challenge);
    }

    private List<Challenge> challenges;
    private List<Challenge> challengesFull;

    private String currentSearchText = "";
    private String currentCategory = "all";
    private String currentStatus = "all";
    private boolean savedOnly = false;

    private final OnChallengeClickListener listener;

    public ChallengeAdapter(
            List<Challenge> challenges,
            OnChallengeClickListener listener
    ) {
        this.challenges = new ArrayList<>(challenges);
        this.challengesFull = new ArrayList<>(challenges);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.challenge_row, parent, false);

        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = challenges.get(position);

        holder.tvTitle.setText(challenge.getTitle());
        holder.tvCategory.setText(challenge.getCategory());
        holder.tvStatus.setText(challenge.getStatus());
        holder.tvTime.setText(challenge.getEstimatedTimeLabel());
        holder.tvImpact.setText("Impact " + challenge.getImpactScore() + "/5 (demo)");

        int statusColor = R.color.muted;
        int statusBackground = R.drawable.bg_status_idle;

        if (challenge.getStatus().equalsIgnoreCase("In Progress")) {
            statusColor = R.color.gold;
            statusBackground = R.drawable.bg_status_active;
        } else if (challenge.getStatus().equalsIgnoreCase("Completed")) {
            statusColor = R.color.forest;
            statusBackground = R.drawable.bg_status_complete;
        }

        holder.tvStatus.setBackgroundResource(statusBackground);
        holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), statusColor)
        );

        if (challenge.isBookmarked()) {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
            holder.btnBookmark.setContentDescription("Remove saved challenge: " + challenge.getTitle());
        } else {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark);
            holder.btnBookmark.setContentDescription("Save challenge: " + challenge.getTitle());
        }

        // Pass the object, not the position: positions change after filtering.
        holder.itemView.setOnClickListener(view -> listener.onChallengeClick(challenge));
        holder.btnBookmark.setOnClickListener(view -> listener.onBookmarkClick(challenge));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public void updateList(List<Challenge> latestChallenges) {
        challengesFull = new ArrayList<>(latestChallenges);
        applyFilter();
    }

    public void setCurrentSearchText(String text) {
        currentSearchText = text.toLowerCase().trim();
        applyFilter();
    }

    public void setCurrentCategory(String category) {
        currentCategory = category.trim();
        applyFilter();
    }

    public void setCurrentStatus(String status) {
        currentStatus = status.trim();
        applyFilter();
    }

    public void setSavedOnly(boolean savedOnly) {
        this.savedOnly = savedOnly;
        applyFilter();
    }

    private void applyFilter() {
        List<Challenge> filteredList = new ArrayList<>();

        // Always use the full list so clearing a filter can restore its hidden rows.
        for (Challenge challenge : challengesFull) {
            boolean matchesSearch = currentSearchText.isEmpty()
                    || challenge.getTitle().toLowerCase().contains(currentSearchText);

            boolean matchesCategory = currentCategory.equalsIgnoreCase("all")
                    || challenge.getCategory().equalsIgnoreCase(currentCategory);

            boolean matchesStatus = currentStatus.equalsIgnoreCase("all")
                    || challenge.getStatus().equalsIgnoreCase(currentStatus);

            boolean matchesSaved = !savedOnly || challenge.isBookmarked();

            if (matchesSearch && matchesCategory && matchesStatus && matchesSaved) {
                filteredList.add(challenge);
            }
        }

        challenges = filteredList;
        notifyDataSetChanged();
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvStatus;
        private final TextView tvImpact;
        private final TextView tvTime;
        private final ImageButton btnBookmark;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvImpact = itemView.findViewById(R.id.tvImpact);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
        }
    }
}
