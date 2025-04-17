package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateDTO {

    private Long certificateId;
    private String certificateName; // Tên chứng chỉ
    private String filePath; // Đường dẫn file
}