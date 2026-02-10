package com.example.ediprocessor.Scheduler;

import com.example.ediprocessor.Service.EDIFileParserService;
import com.example.ediprocessor.Service.InvoiceService;
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
public class InvoiceScheduler {
    private static final Logger log = LoggerFactory.getLogger(InvoiceScheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");

    private File inputFlatFileDir;
    private File processedFlatFilesDir;
    private File outputInvoiceEDIFileDir;
    @Autowired
    InvoiceService invoiceService;

    public InvoiceScheduler() {
        inputFlatFileDir = new File("inputFlatFileDir");
        processedFlatFilesDir = new File("processedFlatFilesDir");
        outputInvoiceEDIFileDir = new File("outputInvoiceEDIFileDir");

        if(!inputFlatFileDir.exists()){
            inputFlatFileDir.mkdir();
        }
        if(!processedFlatFilesDir.exists()){
            processedFlatFilesDir.mkdir();
        }
        if(!outputInvoiceEDIFileDir.exists()){
            outputInvoiceEDIFileDir.mkdir();
        }
    }

    @Scheduled(fixedRate = 10000)
    public void findFlatFile() {
        log.info("Checking for the Invoice Flat files {}", dateFormat.format(new Date()));
        File[] files = inputFlatFileDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if(file.getName().endsWith(".csv")){
                    log.info("CSV File found:" + file.getName());

                    invoiceService.parseInvoiceFlatFiles(file, outputInvoiceEDIFileDir);

                } else {
                    log.error("File format is not valid:" + file.getName());
                }

                try {
                    //Move the processed files
                    Path sourcePath = file.toPath();
                    Path targetPath = processedFlatFilesDir.toPath().resolve(file.getName());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }catch(IOException e){
                    log.error("Error in moving the processed file: {}", file.getName());
                }
            }
        }
    }
}
