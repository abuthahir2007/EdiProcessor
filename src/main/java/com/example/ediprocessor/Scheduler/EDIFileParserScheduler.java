package com.example.ediprocessor.Scheduler;

import com.example.ediprocessor.Service.EDIFileParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class EDIFileParserScheduler {
    private static final Logger log = LoggerFactory.getLogger(EDIFileParserScheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");

    @Autowired
    private EDIFileParserService ediFileParserService;

    private File directory;
    private File processedFilesDir;
    private File outputCSVFilesDir;

    public EDIFileParserScheduler(){
        directory = new File("inputFilesDir");
        processedFilesDir = new File("processedFilesDir");
        outputCSVFilesDir = new File("outputCSVFilesDir");

        if (!processedFilesDir.exists()) {
            processedFilesDir.mkdirs(); // create folder if it doesn't exist
        }
        if (!directory.exists()) {
            directory.mkdirs(); // create folder if it doesn't exist
        }
        if(!outputCSVFilesDir.exists()){
            outputCSVFilesDir.mkdirs(); // create folder if it doesn't exist
        }
    }

    @Scheduled(fixedRate = 10000)
    public void findEDIFile() {
        log.info("Checking for the EDI files {}", dateFormat.format(new Date()));
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if(file.getName().endsWith(".edi")){
                    System.out.println("EDI File found:" + file.getName());
                    ediFileParserService.parseEDIFile(file, outputCSVFilesDir);

                } else {
                    System.out.println("File format is not valid:" + file.getName());
                }

                try {
                    //Move the processed files
                    Path sourcePath = file.toPath();
                    Path targetPath = processedFilesDir.toPath().resolve(file.getName());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }catch(IOException e){
                    log.error("Error in moving the processed file: {}", file.getName());
                }
            }
        }
    }
}
