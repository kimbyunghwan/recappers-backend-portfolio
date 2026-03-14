package com.sch.capstone.backend.service;

import com.sch.capstone.backend.dto.auth.SignUpRequestDTO;
import com.sch.capstone.backend.entity.User;
import com.sch.capstone.backend.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * -이메일 중복 체크
     * -비밀번호 암호화
     */
    @Transactional
    public User signUp(SignUpRequestDTO req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());

        return userRepository.save(user);
    }
    
    // 사용자 이메일로 조회
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 모든 사용자 목록 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 본인 계정 삭제
    @Transactional
    public void deleteSelf(String emailFromAuth) {
        User me = findByEmailOrThrow(emailFromAuth);
        userRepository.delete(me);
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

}
