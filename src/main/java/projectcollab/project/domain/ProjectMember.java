package projectcollab.project.domain;

import jakarta.persistence.*;
import projectcollab.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members",uniqueConstraints = {@UniqueConstraint(name = "uk_project_members_project_user",
        columnNames = {"project_id","user_id"})})
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY,optional = false)
    @JoinColumn(name = "project_id",nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    protected ProjectMember(){
    }

    public ProjectMember(Project project,User user,Role role){
        this.project=project;
        this.user=user;
        this.role=role;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    @PrePersist
    void prePersist() {
        joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
