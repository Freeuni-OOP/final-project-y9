package org.example.y9_gaming_site.admin;

import jakarta.persistence.*;
import org.example.y9_gaming_site.user.Role;
@Entity
@Table(name = "banned_users")
public class BannedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String salt;

    private String reason;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getReason() {return reason;}
    public void setReason(String reason){this.reason = reason;}

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

}

