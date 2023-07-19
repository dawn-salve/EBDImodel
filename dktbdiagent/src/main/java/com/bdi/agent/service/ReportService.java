package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;

@Service
public class ReportService {

    private final boolean localMode = true;     //TODO: change to false if using Azure Blob Storage

    private final BeliefService beliefService;
    int percentage = 100;
    String fontStyle = "Lato";
    String localPath = "./reports/";

    // configuration for Azure Blob Storage
    private final String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;EndpointSuffix=core.windows.net";
    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    String containerName = "reports";
    BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

    @Autowired
    public ReportService(BeliefService beliefService) {

        this.beliefService = beliefService;
    }

    public String createReport(Agent agent) {
        try {

            String fileName = agent.getUserId().concat(".docx");

            File file = new File(localPath + fileName);
            FileOutputStream out = new FileOutputStream(file);

            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph t1 = doc.createParagraph();
            t1.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun t1run = t1.createRun();
            t1run.setFontFamily(fontStyle);
            t1run.setBold(true);
            t1run.setText("FEEDBACK CONVERSATION");
            t1.setSpacingAfter(200);

            XWPFParagraph p1 = doc.createParagraph();
            p1.setSpacingAfter(300);
            XWPFRun r1 = p1.createRun();
            r1.setFontFamily("Lato");
            r1.setText(String.format("Here is a transcript of your conversation with the simulated child, including his thoughts during the conversation. Lilobot has a set of beliefs and intentions that are continuously updated based on what you say to him. " +
                    "In the table below, you can see Lilobot's beliefs at the beginning and end of the conversation. " +
                    "The transcript of the conversation shows which beliefs change based on your messages. The symbol ↑ indicates an increasing belief, while ↓ indicates a decreasing belief. " +
                    "The transcript also shows Lilobot's intentions at different points in the conversation. All these notations are displayed in italics within your conversation. \n" +
                    "Your code for this session is %s.", agent.getUserId()));

            XWPFTable beliefTable = doc.createTable(18, 5);
            beliefTable.setWidth("100%");
            XWPFTableRow headerRow = beliefTable.getRow(0);
            headerRow.getCell(0).setText("Belief");
            headerRow.getCell(1).setText("Five-Phase Model");
            headerRow.getCell(2).setText("Beginning");
            headerRow.getCell(3).setText("End");
            headerRow.getCell(4).setText("Difference");

            HashSet<Belief> initialBeliefs = beliefService.readBeliefsFromCsv();
            Belief[] beliefArray = new Belief[agent.getBeliefs().size()];
            beliefService.getByAgent(agent.getId()).toArray(beliefArray);

            for (int i = 0; i < beliefArray.length; i++) {
                Belief b = beliefArray[i];
                float initialValue = beliefService.getBeliefValue(initialBeliefs, b.getName());

                XWPFTableRow currentRow = beliefTable.getRow(i+1);
                currentRow.getCell(0).setText(String.format("%s", b.getFullName()));
                currentRow.getCell(1).setText(String.format("%s", b.getPhase()));
                currentRow.getCell(2).setText(String.format("%s", floatToPercentage(initialValue)));
                currentRow.getCell(3).setText(String.format("%s", floatToPercentage(b.getValue())));
                currentRow.getCell(4).setText(String.format("%s", calculateDifference(initialValue, b.getValue())));
            }

            XWPFParagraph t2 = doc.createParagraph();
            t2.setAlignment(ParagraphAlignment.CENTER);
            t2.setSpacingBefore(200);
            t2.setSpacingAfter(200);
            XWPFRun t2run = t2.createRun();
            t2run.setFontFamily(fontStyle);
            t2run.setBold(true);
            t2run.setText("TRANSCRIPT");


            for (String log : agent.getLog()) {
                System.out.println(String.format("%s%n", log));
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                p.setSpacingBefore(200);
                r.setText(String.format("%s%n", log));
            }

            doc.write(out);
            out.close();
            doc.close();

            if (!localMode) {
                BlobClient blobClient = containerClient.getBlobClient(fileName);
                System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());
                if (!blobClient.exists()) {
                    blobClient.uploadFromFile(localPath + fileName);
                }
                return generateSasToken(blobClient);
            }

            return file.getAbsolutePath();

        } catch (IOException e) {
            System.err.println("createReport: could not create report");
        }

        return null;

    }


    private String floatToPercentage(float value) {
        int result = Math.round(value * percentage);
        return Integer.toString(result).concat("%");
    }

    private String calculateDifference(float begin, float end) {
        int result = Math.round((end-begin) * percentage);
        return Integer.toString(result).concat("%");
    }


    private String generateSasToken(BlobClient blobClient) {

        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission blobSasPermission =  new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues serviceSasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission);
        String sasToken = blobClient.generateSas(serviceSasValues);

        String urlWithToken = blobClient.getBlobUrl() + "?" + sasToken;

        System.out.println("file url: "  + blobClient.getBlobUrl() + "?" + sasToken);

        return urlWithToken;
    }

}
