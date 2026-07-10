package org.example.y9_gaming_site.user;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    private String avatarUrl;

    private String salt;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    private boolean isBanned;

    @Column(nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int points = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean getBanned(){return isBanned;}
    public void setBanned(boolean isBanned){this.isBanned = isBanned;}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl;}

    public String getAvatarUrl(){return this.avatarUrl;}

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }


    public int getAge() {
        if (this.birthDate == null) {
            return 0;
        }
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }
}

