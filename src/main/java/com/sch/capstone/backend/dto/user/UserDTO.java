package com.sch.capstone.backend.dto.user;

import com.sch.capstone.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role; // STUDENT, PROFESSOR, ADMIN
}
