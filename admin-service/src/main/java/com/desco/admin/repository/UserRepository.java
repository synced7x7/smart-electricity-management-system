package com.desco.admin.repository;

import com.desco.admin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    long countByIsActive(Boolean isActive);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);


    @Query(value = """
            SELECT COALESCE(CAST(area AS text), 'UNASSIGNED') AS area, COUNT(*) AS total
            FROM users
            GROUP BY area
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> countGroupedByArea();

    @Query(value = "SELECT COUNT(*) FROM users WHERE role = CAST(:role AS user_role)",
           nativeQuery = true)
    long countByRole(@Param("role") String role);

    @Query(value = """
            SELECT COUNT(*) FROM users
            WHERE role = CAST(:role AS user_role) AND is_active = TRUE
            """, nativeQuery = true)
    long countActiveByRole(@Param("role") String role);
}
