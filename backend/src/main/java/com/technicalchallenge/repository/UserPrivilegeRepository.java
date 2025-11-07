package com.technicalchallenge.repository;

import com.technicalchallenge.model.UserPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD and lookup operations on {@link UserPrivilege} entities.
 *
 * <p>This repository manages the link between users and privileges.
 *
 * <p>The repository supports two primary ways to check whether a user has a specific privilege:</p>
 * <ul>
 *     <li>By <b>privilege ID</b> – directly checks the foreign key values in {@code user_privilege} table.</li>
 *     <li>By <b>privilege name</b> – navigates the {@code @ManyToOne} relationship to the {@code Privilege} entity and checks the {@code name} field.</li>
 * </ul>
 *
 * <p>Both methods return {@code true} if a matching record exists, {@code false} otherwise.</p>
 *

 */
@Repository
public interface UserPrivilegeRepository extends JpaRepository<UserPrivilege, Long> {
    /**
     * Checks whether a given user has a specific privilege, using privilege IDs.
     *
     * <p>This method uses fields directly from the {@link UserPrivilege} entity
     * ({@code userId} and {@code privilegeId}) and does not perform any JOIN operations.
     * It’s the most efficient option when both IDs are already known.</p>
     */
    boolean existsByUserIdAndPrivilege_Name(Long userId, String privilegeName);

}
