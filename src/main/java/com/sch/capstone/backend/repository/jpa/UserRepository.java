package com.sch.capstone.backend.repository.jpa;

import com.sch.capstone.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 비밀번호 찾을 때 사용자 이메일로 찾기
    boolean existsByEmail(String email);
}
