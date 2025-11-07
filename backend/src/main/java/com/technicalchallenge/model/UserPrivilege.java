package com.technicalchallenge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the relationship between a user and a privilege.
 * Each record links one user (userId) to one privilege (privilegeId).
 *
 * <p>This entity uses a composite key defined in {@link UserPrivilegeId},
 * consisting of both userId and privilegeId.</p>
 *
 * <p>The {@code @ManyToOne} mapping allows navigation from this entity to the
 * corresponding {@link Privilege} entity, so Spring Data JPA can perform
 * derived queries like {@code existsByUserIdAndPrivilege_Name()} automatically.</p>
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_privilege")
@IdClass(UserPrivilegeId.class)
public class UserPrivilege {
    /** The ID of the user who owns the privilege. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** The ID of the privilege assigned to the user. */
    @Id
    @Column(name = "privilege_id")
    private Long privilegeId;

    /**
     * Relationship to the Privilege entity.
     *
     * <p>This tells JPA that the privilege_id column in this table is a foreign key
     * referencing the id column in the Privilege table.</p>
     *
     * <p>The 'insertable = false, updatable = false' means we are not modifying
     * the Privilege record through this entity — we only reference it.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privilege_id", insertable = false, updatable = false)
    private Privilege privilege;

}
