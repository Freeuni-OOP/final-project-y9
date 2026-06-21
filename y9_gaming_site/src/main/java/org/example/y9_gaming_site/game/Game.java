package org.example.y9_gaming_site.game;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    private String description;

    @Column(name = "max_players")
    private int maxPlayers = 2;//will be useful for multiplayer games

    @Column(name = "icon_url")
    private String iconUrl; //icons for games from external sources

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "source_url", length = 555)
    private String sourceUrl; //opensource external games

    @Column(name = "min_players")
    private int minPlayers = 1;//this will also be useful for multiplayer games

    @Column(name = "game_type")
    private String gameType = "OPENSOURCE";
    @Column(name = "category")
    private String category = "ARCADE";

    public String getCategory() { return category; }
    public void setCategory(String s) { this.category = s; }


    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}

    public String getTitle(){return title;}
    public void setTitle(String t){title = t;}

    public String getDescription(){return description;}
    public void setDescription(String d){description=d;}

    public int getMaxPlayers(){return maxPlayers;}
    public void setMaxPlayers(int i){maxPlayers = i;}

    public String getIconUrl(){return iconUrl;}
    public void setIconUrl(String s){iconUrl = s;}

    public LocalDateTime getCreatedAt(){return createdAt;}

    public String getSourceUrl(){return  sourceUrl;}
    public void setSourceUrl(String s){sourceUrl=s;}

    public int getMinPlayers(){return minPlayers;}
    public void setMinPlayers(int i){minPlayers = i;}

    public String getGameType(){return gameType;}
    public void setGameType(String s){gameType = s;}
}