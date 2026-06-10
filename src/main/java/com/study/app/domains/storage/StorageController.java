package com.study.app.domains.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
public class StorageController {
	
    @Autowired
    private uploadService uploadService;
    
    @PostMapping("/board/image")
    public String uploadImage(
    		@RequestParam("file") MultipartFile file,
    		@RequestParam("folder") String folder
    		)throws IOException {
    	
        return uploadService.upload(file, folder);
    }
    
    @PostMapping("/board/file")
    public String uploadFile(
    		@RequestParam("file") MultipartFile file,
    		@RequestParam("folder") String folder
    		)throws IOException{
    
    	return uploadService.upload(file, folder); 
    	
    }

}
