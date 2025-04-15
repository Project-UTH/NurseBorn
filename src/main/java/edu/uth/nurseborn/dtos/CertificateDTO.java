package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateDTO {

    private Long certificateId;
    private String fileName;
    private String filePath;
    private Integer nurseProfileId;
}