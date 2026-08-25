package projectcollab.project.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    protected Project(){}

    public Project(String name,String description){
        this.name = name;
        this.description=description;
    }

    @PrePersist
    void preUpdate(){
        updateAt=LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getDescription(){
        return description;
    }

    public String getName(){
        return name;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public LocalDateTime getUpdateAt(){
        return updateAt;
    }
}
