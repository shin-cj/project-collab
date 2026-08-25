package projectcollab.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="users",uniqueConstraints = {@UniqueConstraint(name="uk_users_email",columnNames ="email")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 50)
    private String name;

    @Column(nullable = false,length = 100)
    private String email;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    protected User(){
    }

    public User(String name,String email){
        this.name=name;
        this.email=email;
    }

    @PrePersist
    void prePersist(){
        createdAt = LocalDateTime.now();
    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
}
