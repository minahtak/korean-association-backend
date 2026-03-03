package com.student.iksu.service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
public class FileService {

    // 1. 파일 업로드 메서드
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception {

        // UUID(랜덤문자열) 생성 (예: 123e4567-e89b...)
        UUID uuid = UUID.randomUUID();

        // 확장자 추출 (예: .jpg)
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        // 저장될 파일 이름 만들기 (예: 123e4567... .jpg)
        String savedFileName = uuid.toString() + extension;

        // 전체 경로 (C:/student-files/123e4567... .jpg)
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        // 실제 파일 저장 (하드디스크에 씀)
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();

        return savedFileName; // 저장된 이름 반환
    }

    // 2. 파일 삭제 메서드 (글 삭제할 때 사진도 지워야 하니까)
    public void deleteFile(String filePath) {
        File deleteFile = new File(filePath);
        if (deleteFile.exists()) {
            deleteFile.delete();
            System.out.println("파일을 삭제하였습니다.");
        } else {
            System.out.println("파일이 존재하지 않습니다.");
        }
    }
}