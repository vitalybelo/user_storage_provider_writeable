package org.example.federation.users.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NamedQueries({
        @NamedQuery(name="getUserByUsername", query="select u from UserEntity u where u.username = :username"),
        @NamedQuery(name="getUserByEmail", query="select u from UserEntity u where u.email = :email"),
        @NamedQuery(name="getUserCount", query="select count(u) from UserEntity u"),
        @NamedQuery(name="getAllUsers", query="select u from UserEntity u"),
        @NamedQuery(name="searchForUser", query="select u from UserEntity u where " +
                "( lower(u.username) like :search or u.email like :search ) order by u.username"),
})

@Entity
@Table(name = "accounts", schema = "privfastsm")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(name = "login")
    private String username;
    private String password;

    private String status;

    @Column(name = "last_name")
    private String lastName;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "middle_name")
    private String middleName;

    private String email;
    private String phone;
    private String department;
    private String position;

    @Column(name = "ip")
    private String ipAddress;
    @Column(name = "max_sessions")
    private int maxSessions;
    @Column(name = "max_idle_time")
    private int maxIdleTime;
    @Column(name = "blocking_start_date")
    private Timestamp blockingDate;
    @Column(name = "banner_viewed")
    private boolean bannerViewed;

    private Long created;

    @ManyToMany(mappedBy = "usersList", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<RoleEntity> rolesList = new HashSet<>();

    public void AddUserRole(RoleEntity role) {
        if (role != null) {
            rolesList.add(role);
            role.getUsersList().add(this);
        }
    }

}
