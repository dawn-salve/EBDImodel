package com.bdi.agent.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Emotion;
import com.bdi.agent.repository.EmotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmotionService {

    private final boolean localMode = true;

    float minValue = (float) 0;
    float maxValue = (float) 1;
    private final EmotionRepository emotionRepository;
    String emotionIncrease = "Emotion: ↑";
    String emotionDecrease = "Emotion: ↓";

    // configuration for Azure Blob Storage
//    String connectionString = "DefaultEndpointsProtocol=https;AccountName=dktblobstorage;AccountKey=JRaAWGN9SbJ+gvn5ec0brrpuvOPT3HS+VSTyLfJoE4/EQKf9eEVIPGqCeniJCiHUKA4JNYymNDtsl1/TDIjEKA==;EndpointSuffix=core.windows.net";

    @Autowired
    public EmotionService(EmotionRepository emotionRepository) {
        this.emotionRepository = emotionRepository;
    }
    public Emotion getByAgentId(Long id) {
        return emotionRepository.findByAgentId(id);
    }

    public void increaseEmotionValue(Agent agent, Float value) {
        Emotion emotion = emotionRepository.findByAgentId(agent.getId());

        if (emotion.getValue() >= maxValue) {
            return;
        }

        emotion.setValue(emotion.getValue() + value);
        emotionRepository.save(emotion);
        agent.addLog(String.format("%s",emotionIncrease));
        System.out.printf("%s %n",emotionIncrease);

    }

    public void decreaseEmotionValue(Agent agent, Float value) {
        Emotion emotion = emotionRepository.findByAgentId(agent.getId());

        if (emotion.getValue() <= minValue) {
            return;
        }

        emotion.setValue(emotion.getValue() - value);
        emotionRepository.save(emotion);
        agent.addLog(String.format("%s",emotionDecrease));
        System.out.printf("%s %n",emotionDecrease);

    }

    public Float getEmotionValue(Agent agent) {
        Emotion emotion = emotionRepository.findByAgentId(agent.getId());
        Float e = emotion.getValue();
        return e;
    }

    public Emotion setEmotionValue(Agent agent, Float value) {
        Emotion emotion = new Emotion();
        emotion.setValue(value);
        emotion.setAgent(agent);
        emotionRepository.save(emotion);
        return emotion;
    }

//    private String getEmotionFromBlobStorage() {
//
//        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
//        String containerName = "bdi";
//        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
//
//        String fileName = "emotion_en.csv";
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



