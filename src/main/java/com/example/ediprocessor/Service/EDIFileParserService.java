package com.example.ediprocessor.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class EDIFileParserService {
    private static final Logger log = LoggerFactory.getLogger(EDIFileParserService.class);

    public void parseEDIFile(File ediFile, File OutputCsv) {
        {


            String csvFileName = OutputCsv.getPath() + "/" + ediFile.getName().substring(0, ediFile.getName().length() - 4) + ".csv";

            try (BufferedReader reader = Files.newBufferedReader(ediFile.toPath());
                 BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFileName))) {

                // Write CSV header
                writer.write("LineNumber,BuyerPartNumber,Quantity,UnitOfMeasure");
                writer.newLine();

                StringBuilder ediContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    ediContent.append(line.trim());
                }

                // Split by segment terminator (~)
                String[] segments = ediContent.toString().split("~");

                String currentLIN = null;
                String currentBP = null;

                for (String segment : segments) {
                    if (segment.isBlank()) continue;

                    String[] elements = segment.split("\\*");

                    switch (elements[0]) {
                        case "LIN":
                            currentLIN = elements.length > 1 ? elements[1] : "";
                            currentBP = elements.length > 3 ? elements[3] : "";
                            break;

                        case "SN1":
                            String quantity = elements.length > 1 ? elements[1] : "";
                            String uom = elements.length > 2 ? elements[2] : "";
                            if (currentLIN != null) {
                                writer.write(String.join(",", currentLIN, currentBP, quantity, uom));
                                writer.newLine();
                                // reset LIN info after writing
                                currentLIN = null;
                                currentBP = null;
                            }
                            break;
                        default:
                            // ignore other segments
                    }
                }
                log.info("EDI file processed successfully and create CSV File:" + csvFileName);
            } catch(Exception e){
                log.error("Exception caught while parsing the EDI file 852 format");
            }


        }
    }


}
