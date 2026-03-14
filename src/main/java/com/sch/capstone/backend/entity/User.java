package com.sch.capstone.backend.entity;

import com.sch.capstone.backend.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {

    /** 사용자 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이름 */
    @Column(length = 255, nullable = false)
    private String name;

    /** 사용자 비밀번호 (암호화 저장 예정) */
    @Column(length = 255, nullable = false)
    private String password;

    /**
     * 사용자 이메일 (로그인 ID)
     * - nullable: null 값 허용 안 함
     * - unique: 중복 가입 방지
     */
    @Column(length = 191, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

}
