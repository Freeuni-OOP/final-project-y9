package org.example.y9_gaming_site.quiz;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    //quiz class

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private List<String> images = new ArrayList<>();

    private String description;

    @Column(nullable = false)
    private String category; // GEOGRAPHY, SCIENCE, ENTERTAINMENT

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "time_limit_seconds")
    private int timeLimitSeconds = 300; //countdown

    private java.util.List<String> questions = new java.util.ArrayList<>();


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }

    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }

    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String u) { this.iconUrl = u; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int i) { this.timeLimitSeconds = i; }

    public java.util.List<String> getQuestions() { return questions; }
    public void setQuestions(java.util.List<String> questions) { this.questions = questions; }

    public List<String> getImages() {return images;}
    public void setImages(List<String> images){this.images = images;}
}