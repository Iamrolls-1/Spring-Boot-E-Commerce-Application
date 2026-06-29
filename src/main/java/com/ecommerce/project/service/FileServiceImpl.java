package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    public String uploadImage(String path, MultipartFile image) throws IOException {
        //Get the file name of the original file
        String originalFileName = image.getOriginalFilename();

        //Generate a Unique file name
        String randomId = UUID.randomUUID().toString();
        //mat.jpg -> 1234(UUID) -> 1234.jpg
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;

        //Check if path exist and create
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }

        //Upload the image to server
        Files.copy(image.getInputStream(), Paths.get(filePath));

        //return file name
        return fileName;
    }
}
