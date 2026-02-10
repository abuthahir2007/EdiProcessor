package com.example.ediprocessor.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InvoiceService {
    public void parseInvoiceFlatFiles(File file, File outputInvoiceEDIFileDir) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
            String[] headers = lines.get(0).split(",");


            List<Map<String, String>> rows = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                Map<String, String> row = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    row.put(headers[j].trim(), values[j].trim());
                }
                rows.add(row);
            }


            Map<String, List<Map<String, String>>> rowsByBuyer = new HashMap<>();

            for (Map<String, String> row : rows) {
                String buyer = row.get("buyer");
                rowsByBuyer
                        .computeIfAbsent(buyer, k -> new ArrayList<>())
                        .add(row);
            }

            for (Map.Entry<String, List<Map<String, String>>> entry : rowsByBuyer.entrySet()) {

                String buyer = entry.getKey();
                List<Map<String, String>> buyerRows = entry.getValue();

                Map<String, String> firstRow = buyerRows.get(0);

                StringBuilder edi = new StringBuilder();
                int segmentCount = 0;
                int lineNumber = 1;
                double totalAmount = 0.0;

                String receiverId = buyer; // or map buyer → receiverId

                edi.append("ISA*00*          *00*          *ZZ*SENDERID       *ZZ*")
                        .append(String.format("%-15s", receiverId))
                        .append("*260201*1200*U*00401*000000001*0*T*>~\n");

                edi.append("GS*IN*SENDER*")
                        .append(receiverId)
                        .append("*20260201*1200*1*X*004010~\n");

                edi.append("ST*810*0001~\n"); segmentCount++;

                edi.append("BIG*")
                        .append(firstRow.get("invoiceDate"))
                        .append("*")
                        .append(firstRow.get("invoiceNumber"))
                        .append("~\n"); segmentCount++;

                edi.append("N1*BY*").append(buyer).append("~\n"); segmentCount++;
                edi.append("N1*SE*").append(firstRow.get("seller")).append("~\n"); segmentCount++;

                for (Map<String, String> row : buyerRows) {

                    double quantity = Double.parseDouble(row.get("quantity"));
                    double unitPrice = Double.parseDouble(row.get("unitPrice"));
                    double lineTotal = quantity * unitPrice;

                    edi.append("IT1*")
                            .append(lineNumber)
                            .append("*")
                            .append(quantity)
                            .append("*EA*")
                            .append(unitPrice)
                            .append("**VP*")
                            .append(row.get("item"))
                            .append("~\n");

                    segmentCount++;
                    totalAmount += lineTotal;
                    lineNumber++;
                }

                edi.append("TDS*").append((int)(totalAmount * 100)).append("~\n");
                segmentCount++;

                edi.append("SE*").append(segmentCount + 1).append("*0001~\n");
                edi.append("GE*1*1~\n");
                edi.append("IEA*1*000000001~\n");

                String outputFileName = receiverId + "_invoice.edi";

                Files.write(
                        Paths.get(outputInvoiceEDIFileDir.getPath(), outputFileName),
                        edi.toString().getBytes()
                );

                log.info("Generated EDI 810 for buyer {}", buyer);
            }

        }catch (IOException e){
            log.error("Error in Parsing Invoice Flat {} file", file.getName());
        }
    }
}
