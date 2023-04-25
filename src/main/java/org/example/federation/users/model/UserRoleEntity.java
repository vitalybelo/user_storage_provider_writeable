package org.example.federation.users.model;

import lombok.*;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "userroles", schema = "privfastsm")
@NamedQueries({
        @NamedQuery(name="getAllRoles", query="select r from UserRoleEntity r")
})
public class UserRoleEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    @ManyToMany(
            mappedBy = "roleList",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<UserEntity> userList = new LinkedHashSet<>();


}
