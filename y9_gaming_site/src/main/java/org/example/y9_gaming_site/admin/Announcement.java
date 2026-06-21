package org.example.y9_gaming_site.admin;


import jakarta.persistence.*;


import java.time.LocalDateTime;


@Entity
@Table(name = "announcements")
public class Announcement {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long admin_id;


    private String title;


    @Column(length = 1000)
    private String content;
    private LocalDateTime createdAt;




    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Long getAdmin_id() { return admin_id;}
    public void setAdmin_id(Long admin_id){this.admin_id = admin_id;}


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }


    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

