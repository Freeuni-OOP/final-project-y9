package org.example.y9_gaming_site.homePage;

import java.util.List;

public class HomeStatsDTO {

    private long totalUsers;
    private List<RankedPlayerDTO> topPlayers;
    private List<RecentAchievementDTO> recentAchievements;

    // ---- nested DTOs ----

    public static class RankedPlayerDTO {
        private String username;
        private String avatarUrl;
        private int rank;
        private int score;

        public RankedPlayerDTO(String username, String avatarUrl, int rank, int score) {
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.rank = rank;
            this.score = score;
        }

        public String getUsername()  { return username; }
        public String getAvatarUrl() { return avatarUrl; }
        public int    getRank()      { return rank; }
        public int    getScore()     { return score; }
    }

    public static class RecentAchievementDTO {
        private String username;
        private String achievementName;
        private String iconUrl;
        private String earnedAt;          // ISO-8601 string, formatted in service

        public RecentAchievementDTO(String username, String achievementName,
                                    String iconUrl, String earnedAt) {
            this.username        = username;
            this.achievementName = achievementName;
            this.iconUrl         = iconUrl;
            this.earnedAt        = earnedAt;
        }

        public String getUsername()        { return username; }
        public String getAchievementName() { return achievementName; }
        public String getIconUrl()         { return iconUrl; }
        public String getEarnedAt()        { return earnedAt; }
    }

    // ---- main getters / setters ----

    public long getTotalUsers()                           { return totalUsers; }
    public void setTotalUsers(long totalUsers)            { this.totalUsers = totalUsers; }

    public List<RankedPlayerDTO> getTopPlayers()                        { return topPlayers; }
    public void setTopPlayers(List<RankedPlayerDTO> topPlayers)         { this.topPlayers = topPlayers; }

    public List<RecentAchievementDTO> getRecentAchievements()                           { return recentAchievements; }
    public void setRecentAchievements(List<RecentAchievementDTO> recentAchievements)    { this.recentAchievements = recentAchievements; }
}