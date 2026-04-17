package com.alshifa.mlms_al_shifa.repository;

import com.alshifa.mlms_al_shifa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u ORDER BY u.fullName ASC")
    List<User> findAllOrdered();

    @Query("""
        SELECT DISTINCT u FROM User u
        WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
        OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY u.fullName ASC
        """)
    List<User> search(@Param("q") String query);
}