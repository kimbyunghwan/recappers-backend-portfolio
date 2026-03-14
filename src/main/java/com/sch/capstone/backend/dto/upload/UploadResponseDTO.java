package com.sch.capstone.backend.dto.upload;

import com.sch.capstone.backend.dto.user.UserDTO;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UploadResponseDTO {
    private Long id;
    private String fileName;
    private String uploadTime;
    private String status;
    private String downloadUrl;
    private UserDTO user;
}
