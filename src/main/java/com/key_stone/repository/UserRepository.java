package com.key_stone.repository;
import com.key_stone.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long>{ Optional<User> findByEmailIgnoreCase(String email); }