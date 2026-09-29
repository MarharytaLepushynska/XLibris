package com.group.xlibris.user.internal;


import com.group.xlibris.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
