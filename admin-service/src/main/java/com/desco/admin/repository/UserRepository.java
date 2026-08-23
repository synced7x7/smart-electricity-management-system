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

    /**
     * Native query: "area" is a PostgreSQL enum, so grouping is done on its text
     * representation. A derived query would bind the parameter as varchar and fail
     * with `operator does not exist: area_name = character varying`.
     */
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

    /**
     * Counts only ADMINs who can still log in. Used by the "don't lock everyone out"
     * guard — counting inactive admins there would let the last usable admin be
     * deactivated as long as some disabled admin row existed.
     */
    @Query(value = """
            SELECT COUNT(*) FROM users
            WHERE role = CAST(:role AS user_role) AND is_active = TRUE
            """, nativeQuery = true)
    long countActiveByRole(@Param("role") String role);
}
