package org.example.federation.users.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(schema = "privfastsm", name = "account_role")
public class UserAccountRolesEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountRoleId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", nullable = false)
    private UserEntity userAccount;

}
