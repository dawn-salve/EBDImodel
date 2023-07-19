package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Desire;
import com.bdi.agent.repository.DesireRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DesireService {

    private final boolean localMode = true;     //TODO: change to false when using Azure

    private DesireRepository desireRepository;
    private final ActionService actionService;

    String desiresFile = "files/desires_en.csv";

    // configuration for Azure Blob Storage
    String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;EndpointSuffix=core.windows.net";


    public DesireService(DesireRepository desireRepository, ActionService actionService) {
        this.desireRepository = desireRepository;
        this.actionService = actionService;
    }

    public void addDesire(Desire desire) {
        desireRepository.save(desire);
    }

    public Desire getById(Long id) {
        return desireRepository.getById(id);
    }

    public Desire getByAgentIdAndName(Long agentId, String name) {
        return desireRepository.findByAgentIdAndName(agentId, name);
    }

    public List<Desire> getByAgent(Long agentId) {
        return desireRepository.findByAgentId(agentId);
    }


    public void addDesires(Set<Desire> desires) {
        for (Desire desire : desires) {
            desireRepository.save(desire);
        }
    }

    public Desire getActiveGoal(Long agentId) {
        List<Desire> desires = getByAgent(agentId);
        List<String> activeDesires = new ArrayList<>();

        for (Desire desire : desires) {
            if (desire.isActive()) {
                activeDesires.add(desire.getName());
            }
        }

        if (activeDesires.isEmpty()) {
            return null;
        }

        int id = 0;
        String smallestString = activeDesires.get(0); // initialize smallestString to first element in list
        for (int i = 1; i < activeDesires.size(); i++) { // start loop from second element
            if (activeDesires.get(i).compareTo(smallestString) < 0) { // compare string at current index with smallestString
                smallestString = activeDesires.get(i);
            }
        }
        Desire activedesire = desireRepository.findByAgentIdAndName(agentId, smallestString);
        return activedesire;


    }

    public HashSet<Desire> readDesiresFromCsv(Agent agent) {
        HashSet<Desire> result = new HashSet<>();

        try {

//            if (!localMode) {
//                desiresFile = getDesiresFromBlobStorage();
//            }
            CSVReader reader = new CSVReader(new FileReader(desiresFile));
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                Desire d = new Desire();
                d.setAgent(agent);
                d.setName(record[0]);
                d.setFullName(record[1]);
                d.setActiveValue(Boolean.valueOf(record[2]));
                actionService.addActionsToDesire(d);
                result.add(d);
            }

            reader.close();

        } catch (IOException | CsvException e) {
            System.err.println("readDesiresFromCsv: could not initialize desires");
        }

        return result;
    }

//    private String getDesiresFromBlobStorage() {
//
//        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
//        String containerName = "bdi";
//        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
//
//        String fileName = "desires_en.csv";
//        String downloadFileName = fileName.replace(".csv", "DOWNLOAD.csv");
//        File downloadedFile = new File(downloadFileName);
//        System.out.println("\nDownloading blob to\n\t " + downloadFileName);
//
//        if (!downloadedFile.exists()) {
//            BlobClient blobClient = containerClient.getBlobClient(fileName);
//            blobClient.downloadToFile(downloadFileName);
//        }
//
//        return downloadedFile.getAbsolutePath();
//    }
}
