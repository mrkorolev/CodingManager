package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RLManager {

    private String fileContent;
    private int encodedFileSize;
    private double entropy = 0;
    private double avgWordLength = 0;
    private int totalBinaryWords = 0;
    private Map<String, Integer> frequencies = new HashMap<>();

    public RLManager(BufferedReader reader) throws IOException {

        StringBuilder fileContentBuilder = new StringBuilder("");
        String tmp = reader.readLine();

        while (tmp != null) {
            fileContentBuilder.append(tmp);
            tmp = reader.readLine();
        }

        this.fileContent = fileContentBuilder.toString();
    }

    private void calculateEntropy() {
        for (String binaryCharacter : frequencies.keySet()) {
            double binaryCharProbability = (double)frequencies.get(binaryCharacter) / totalBinaryWords;
            this.entropy += (binaryCharProbability * Math.log10(1 / binaryCharProbability) / Math.log10(2));
        }
    }

    public void encode(BufferedWriter writer) throws IOException {
        StringBuilder encodedMessage = new StringBuilder("");
        int counter = 1;
        char lastChar = fileContent.charAt(0);
        int totalBitsInFile = 0;

        if (Main.arguments.debug) {
            System.out.println("[LOG] --- GENERATING RUN-LENGTH CODE (AND WRITING TO FILE)");
            System.out.println("[LOG] --- ");
            System.out.print("[LOG] --- ");
        }
        for (int i = 1; i < fileContent.length(); i++) {
            if (lastChar == fileContent.charAt(i)) {
                counter++;
            } else {
                if (Main.arguments.debug) {
                    System.out.printf("%d%c ", counter, lastChar);
                }
                String binaryReps = Integer.toBinaryString(counter);
                String character = Integer.toBinaryString(lastChar);
                encodedMessage.append(binaryReps);
                encodedMessage.append("_");
                encodedMessage.append(character);
                encodedMessage.append("_");
                this.frequencies.put(binaryReps + character, this.frequencies.containsKey(binaryReps + character) ? this.frequencies.get(binaryReps + character) : 1);
                counter = 1;
                lastChar = fileContent.charAt(i);
                totalBitsInFile += binaryReps.length();
                totalBitsInFile += character.length();
                totalBitsInFile += (2 * Integer.toBinaryString('_').length());
            }
            if (i == fileContent.length() - 1) {
                String binaryReps = Integer.toBinaryString(counter);
                String binaryChar = Integer.toBinaryString(lastChar);
                encodedMessage.append(binaryReps);
                encodedMessage.append("_");
                encodedMessage.append(binaryChar);
                this.frequencies.put(binaryReps + binaryChar, this.frequencies.containsKey(binaryReps + binaryChar) ? this.frequencies.get(binaryReps + binaryChar) : 1);
                totalBitsInFile += Integer.toBinaryString(counter).length();
                totalBitsInFile += Integer.toBinaryString(lastChar).length();
                totalBitsInFile += (Integer.toBinaryString('_').length());
            }
        }
        this.encodedFileSize = totalBitsInFile;

        if (Main.arguments.debug) {
            System.out.println();
            System.out.println("[LOG] --- ===================================================");
        }

        writer.write(encodedMessage.toString());

        for (String binaryCharacter : frequencies.keySet()) {
            this.totalBinaryWords += frequencies.get(binaryCharacter);
        }
        calculateEntropy();
        calculateAvgWordLength();
    }

    private void calculateAvgWordLength() {
        for (String binaryChar : frequencies.keySet()) {
            double binaryCharProbability = (double)frequencies.get(binaryChar) / totalBinaryWords;
            this.avgWordLength += (binaryCharProbability * binaryChar.length());
        }
    }


    public static void decode(String message, BufferedWriter writer) throws IOException {
        StringBuilder decodedFileContent = new StringBuilder("");
        StringBuilder accumulator = new StringBuilder("");

        int repsNumber = -1;
        if (Main.arguments.debug) {
            System.out.println("[LOG] --- DECODING RUN-LENGTH FILE");
            System.out.println("[LOG] --- ");
        }
        for (int i = 0; i < message.length(); i++) {
            char stringCurrentChar = message.charAt(i);
            if (stringCurrentChar != '_') {
                accumulator.append(stringCurrentChar);
                if (i != message.length() - 1) {
                    continue;
                }
            } else {
                if (repsNumber == -1) {
                    repsNumber = Integer.parseInt(accumulator.toString(), 2);
                    if (Main.arguments.debug) {
                        System.out.printf("[LOG] --- Parsed: # of reps = %d (%s), ", repsNumber, accumulator);
                    }
                    accumulator = new StringBuilder("");
                    continue;
                }
            }

            // Info about char has been collected

            for (int j = 0; j < repsNumber; j++) {
                int characterForm = Integer.parseInt(accumulator.toString(), 2);
                decodedFileContent.append((char)characterForm);
            }

            if (Main.arguments.debug) {
                int currentChar = Integer.parseInt(accumulator.toString(), 2);
                System.out.printf("char: %s (%d) -> ", accumulator, currentChar);
                for(int j = 0; j < repsNumber; j++) {
                    System.out.print((char)currentChar);
                }
                System.out.println();
            }

            repsNumber = -1;
            accumulator = new StringBuilder("");
        }
        System.out.println("[LOG] --- ===================================================");
        writer.write(decodedFileContent.toString());
    }

    public int getEncodedFileSize() {
        return encodedFileSize;
    }

    public double getEntropy() {
        return entropy;
    }

    public double getAvgWordLength() {
        return avgWordLength;
    }
}
