package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.repository.BeliefRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class BeliefService {

    private final boolean localMode = true;     //TODO: change to false when using Azure

    float minValue = (float) 0;
    float maxValue = (float) 1;
    private final BeliefRepository beliefRepository;
    String beliefIncrease = "Belief: ↑";
    String beliefDecrease = "Belief: ↓";

    String beliefsFile = "files/beliefs_en.csv";

    // configuration for Azure Blob Storage
    String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;EndpointSuffix=core.windows.net";

    @Autowired
    public BeliefService(BeliefRepository beliefRepository) {
        this.beliefRepository = beliefRepository;
    }

    public Set<Belief> getByAgent(Long agentId) {
        return beliefRepository.findByAgentIdOrderByPhaseAsc(agentId);
    }

    public Belief getByAgentIdAndName(Long agentId, String name) {
        return beliefRepository.findByAgentIdAndName(agentId, name);
    }

    public void addBelief(Belief belief) {
        beliefRepository.save(belief);
    }

    public void addBeliefs(Set<Belief> beliefs) {
        for (Belief belief : beliefs) {
            beliefRepository.save(belief);
        }
    }

    public void increaseBeliefValue(Agent agent, String name, Float value) {
        Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), name);

        if (belief.getValue() >= maxValue) {
            return;
        }

        agent.addLog(String.format("%-20s %s",beliefIncrease, belief.getFullName()));
        System.out.printf("%-20s %s%n",beliefIncrease, belief.getFullName());

        belief.setValue(belief.getValue() + value);
        beliefRepository.save(belief);


    }

    public void decreaseBeliefValue(Agent agent, String name, Float value) {
        Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), name);

        if (belief.getValue() <= minValue) {
            return;
        }

        belief.setValue(belief.getValue() - value);
        beliefRepository.save(belief);
        agent.addLog(String.format("%-20s %s",beliefDecrease, belief.getFullName()));
        System.out.printf("%-20s %s%n",beliefDecrease, belief.getFullName());

    }

    public void setBeliefValue(Agent agent, String name, Float value) {
        Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), name);
        belief.setValue(value);
        beliefRepository.save(belief);
    }

    public float averageBeliefValue(Long agentId, String[] beliefNames) {
        float sum = 0;
        for (String beliefName : beliefNames) {
            sum += getByAgentIdAndName(agentId, beliefName).getValue();
        }

        return sum / beliefNames.length;
    }

    public HashSet<Belief> readBeliefsFromCsv(Agent agent) {

        HashSet<Belief> result = new HashSet<>();

        try {

//            if (!localMode) {
//                beliefsFile = getBeliefsFromBlobStorage();
//            }

            CSVReader reader = new CSVReader(new FileReader(beliefsFile));
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                Belief b = new Belief();
                b.setAgent(agent);
                b.setName(record[0]);
                b.setFullName(record[1]);
                b.setPhase(record[2]);
                b.setValue(Float.valueOf(record[3]));
                result.add(b);
            }

            reader.close();

        } catch (IOException | CsvException e) {
            System.err.println("readBeliefsFromCsv: could not initialize beliefs");
        }

        return result;
    }

    public HashSet<Belief> readBeliefsFromCsv() {
        HashSet<Belief> result = new HashSet<>();

        try {

//            if (!localMode) {
//                beliefsFile = getBeliefsFromBlobStorage();
//            }

            CSVReader reader = new CSVReader(new FileReader(beliefsFile));
            List<String[]> records = reader.readAll();

            for (String[] record : records) {
                Belief b = new Belief();
                b.setName(record[0]);
                b.setFullName(record[1]);
                b.setPhase(record[2]);
                b.setValue(Float.valueOf(record[3]));
                result.add(b);
            }

            reader.close();

        } catch (IOException | CsvException e) {
            System.err.println("readBeliefsFromCsv: could not initialize beliefs");
        }

        return result;
    }

    public float getBeliefValue(Set<Belief> beliefs, String name) {
        for (Belief b : beliefs) {
            if (b.getName().equals(name)) {
                return b.getValue();
            }
        }

        return 0;
    }

//    private String getBeliefsFromBlobStorage() {
//
//        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
//        String containerName = "bdi";
//        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
//
//        String fileName = "beliefs_en.csv";
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

/**
 * This method computes the BDI outcome of the agent. Used as part of the experiment of the thesis.
 * **/
    public float calculateScore(Agent agent) {
        String[] beliefNames = {"B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B14", "B17"};

        float result = 0;
        for (String beliefName : beliefNames) {
            Belief belief = beliefRepository.findByAgentIdAndName(agent.getId(), beliefName);

            if (beliefName.equals("B8") || beliefName.equals("B17")) {
                result += (1 - belief.getValue());
            } else {
                result += belief.getValue();
            }
        }

        System.out.println("BDI result: " + result);
        return result;
    }


}
