package org.example.federation.users.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "rights", schema = "privfastsm")

public class UserRightsEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rightId;

    @Column(name = "target_object_name")
    private String keyName;
    @Column(name = "condition")
    private String valueName;
    @Column(name = "modification_date")
    private Timestamp modificationDate;
    @Column(name = "version")
    private int version;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "userrole", nullable = false)
    private UserRoleEntity roleEntity;

}
