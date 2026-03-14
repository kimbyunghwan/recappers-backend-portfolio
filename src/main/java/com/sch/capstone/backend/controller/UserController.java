package com.sch.capstone.backend.controller;

import com.sch.capstone.backend.dto.user.UserDTO;
import com.sch.capstone.backend.entity.User;
import com.sch.capstone.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// 사용자 관련 컨트롤러
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "이메일로 사용자 조회")
    @GetMapping("/email")
    public ResponseEntity<UserDTO> findByEmail(
            @Parameter(description = "조회할 이메일 주소") @RequestParam String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.map(u -> ResponseEntity.ok(toDTO(u))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "모든 사용자 목록 조회")
    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        List<User> users = userService.findAll();
        List<UserDTO> dtoList = users.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok().body(dtoList);
    }
    
    @Operation(summary = "내 계정 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        // 인증 객체가 없거나, 인증되지 않았거나, 익명 사용자라면 401 Unauthorized
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        // JWT에서 사용자 이메일 추출
        String emailFromAuth = authentication.getName();
        // 계정 삭제
        userService.deleteSelf(emailFromAuth);
        return ResponseEntity.noContent().build(); // 204
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
