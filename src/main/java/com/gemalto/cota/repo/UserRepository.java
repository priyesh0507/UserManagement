package com.gemalto.cota.repo;

import com.gemalto.cota.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
