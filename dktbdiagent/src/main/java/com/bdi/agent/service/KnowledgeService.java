package com.bdi.agent.service;

import com.bdi.agent.model.Knowledge;
import com.bdi.agent.model.Emotion;
import com.bdi.agent.repository.EmotionRepository;
import com.bdi.agent.repository.KnowledgeRepository;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static java.awt.SystemColor.text;

@Service
public class KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final EmotionRepository emotionRepository;
    private final String knowledgeFile = "files/knowledge_en.csv";
    private static final String termsFile = "files/emotion_en.csv";


    @Autowired
    public KnowledgeService(KnowledgeRepository knowledgeRepository, EmotionRepository emotionRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.emotionRepository = emotionRepository;
    }

    public void initializeKnowledge() {
        try {
            readFromCsv();
        } catch (IOException | CsvException e) {
            System.err.println("Could not initialize knowledge");
        }
        List<Knowledge> knowledgeList = knowledgeRepository.findAll();
        knowledgeList.size();
    }

    public Knowledge getBySubjectAndAttribute(String subject, String attribute) {
        return knowledgeRepository.findBySubjectAndAttribute(subject, attribute);
    }

    public String getResponse(Knowledge knowledge, Emotion emotion) throws IOException, CsvException {
        List<String> res = knowledge.getValues();
        float emotionValue = emotion.getValue();
        Random rand = new Random();
        String response = res.get(rand.nextInt(res.size()));

        System.out.printf(response, emotionValue);

        return addTerms(response, emotionValue);
    }

    public String addTerms(String knowledge, float emotionValue) throws IOException, CsvException {
        String emoticon = getEmoticon(emotionValue);
        String term = processTextInput(emotionValue);
        String space = " ";
        String output = knowledge;

        if (emotionValue >= 0.6) {
            double rand = Math.random();
            if (rand < 0.33) {
                output = emoticon + space + term + space + output;
            } else if (rand < 0.66) {
                output = term + space + emoticon + space + output;
            } else {
                output = output + space + term + space + emoticon;
            }
        }

        output = addExclamation(output, emotionValue);

        return output;
    }

    private static String addExclamation(String knowledge, float emotionValue) {
        int numDots = knowledge.split("\\.").length;
        int numExclamations = knowledge.split("!").length;

        if (emotionValue >= 0.6) {
            if (numExclamations == 0) {
                if (numDots > 0) {
                    knowledge = knowledge.replace(".", "!");
                } else {
                    knowledge = knowledge.replaceAll("\\s+$", "") + "!";
                }
            }

            String[] parts = knowledge.split("!");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                sb.append(parts[i]);
                for (int j = 0; j < numExclamations * emotionValue; j++) {
                    sb.append("!");
                }
                sb.append("!");
            }
            sb.append(parts[parts.length - 1]);
            knowledge = sb.toString();
        }
        return knowledge;
    }

    private String getEmoticon(float frustrationScore) {
        String emo = "";
        if (frustrationScore >= 0.8) {
            emo = ":(((((";
        } else if (frustrationScore >= 0.7 & frustrationScore < 0.8) {
            emo = ":(((";
        } else if (frustrationScore >= 0.6 & frustrationScore < 0.7) {
            emo = ":(";
        }
        return emo;
    }

    public static String processTextInput(float value) throws IOException, CsvException {
        if (value >= 0.6 & value < 0.7) {
            return readTermsFromCsv(0);
        } else if (value >= 0.7 & value < 0.8) {
            return readTermsFromCsv(1);
        } else if (value >= 0.8) {
            return readTermsFromCsv(2);
        } else {
            return null;
        }
    }

    private static String readTermsFromCsv(int line) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(termsFile));
        List<String[]> lines = reader.readAll();
        reader.close();

        List<String> records = new ArrayList<>();

        if (line < lines.size()) {
            records.addAll(Arrays.asList(lines.get(line)));
        }

        if (records.isEmpty()) {
            return null;
        }

        Random random = new Random();
        return records.get(random.nextInt(records.size()));
    }

    private void readFromCsv() throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(knowledgeFile));
        List<String[]> records = reader.readAll();

        for (String[] record : records) {
            Knowledge k = new Knowledge();
            k.setSubject(record[0]);
            k.setAttribute(record[1]);

            List<String> values = new ArrayList<>();
            values.add(record[2]);
            values.add(record[3]);
            values.add(record[4]);
            values.add(record[5]);
            values.add(record[6]);
            k.setValues(values);
            knowledgeRepository.save(k);
        }

        reader.close();
    }
}
