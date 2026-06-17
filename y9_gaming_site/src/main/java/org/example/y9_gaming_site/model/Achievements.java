package org.example.y9_gaming_site.model;

import org.example.y9_gaming_site.user.User;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "achievements")
public class Achievements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "points_reward", nullable = false)
    private int pointsReward = 0;

    // Which users have earned this achievement
    @ManyToMany(mappedBy = "achievements")
    private List<User> users;

    // ---- Getters & Setters ----

    public Long getId()                        { return id; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getIconUrl()                 { return iconUrl; }
    public void setIconUrl(String iconUrl)     { this.iconUrl = iconUrl; }

    public int getPointsReward()               { return pointsReward; }
    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }

    public List<User> getUsers()               { return users; }
    public void setUsers(List<User> users)     { this.users = users; }
}