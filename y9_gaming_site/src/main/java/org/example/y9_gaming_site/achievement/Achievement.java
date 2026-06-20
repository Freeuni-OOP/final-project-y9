package org.example.y9_gaming_site.achievement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="achievements")
@Getter
@Setter
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; //unique identifier
    private String name; //name of achievement, status
    private String description; //what to do to get the status
    private String icon; //name of icon file. used for frontend

}
