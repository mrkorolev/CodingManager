package org.example;

import java.io.*;
import java.util.*;

public class HuffmanManager {

    private String fileContents;
    private Map<Integer, Integer> frequencies;
    private int encodedFileSize;
    private double entropy = 0;
    private double avgWordLength = 0;
//    private BinTree huffmanTree;
    private Map<Integer, String> huffmanTable;

    public HuffmanManager(BufferedReader reader) throws IOException {
        StringBuilder fileContentBuilder = new StringBuilder("");
        String tmp = reader.readLine();

        while(tmp != null) {
            fileContentBuilder.append(tmp);
            tmp = reader.readLine();
        }

        this.fileContents = fileContentBuilder.toString();
        this.frequencies = generateFrequencies(fileContentBuilder);

        if (Main.arguments.debug){
            logFrequencyDescription();
        }

    }

    private void calculateEntropy() {
        for (int key : frequencies.keySet()) {
            double charProbability = (double)frequencies.get(key) / fileContents.length();
            double charEntropy = charProbability * (Math.log10(1 / charProbability) / Math.log10(2));
            this.entropy += charEntropy;
        }
    }

    private Map<Integer, Integer> generateFrequencies(StringBuilder builder) {
        Map<Integer, Integer> frequencies = new HashMap<>();
        for(int i = 0; i < builder.length(); i++) {
            int current = builder.charAt(i);
            frequencies.put(current, frequencies.containsKey(current) ? frequencies.get(current) + 1 : 1);
        }
        return frequencies;
    }

    public void logFrequencyDescription() {
        System.out.println("[LOG] --- GENERATING FREQUENCIES INFORMATION");
        System.out.println("[LOG] --- ");
        for(Map.Entry<Integer, Integer> entry: frequencies.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            System.out.printf("[LOG] --- %c, P(%c) = %.4f (dec: %d, bin: %s)%n",
                    key, key, (double)value / fileContents.length(),
                    key, Integer.toBinaryString(key));
        }
        System.out.println("[LOG] --- ===================================================");
    }

    public void generateHuffmanCode() {
        List<BinTree> nodes = new ArrayList<>();

        // Initialize the list and sort in desc. order
        for(Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
            nodes.add(new BinTree(Character.toString(entry.getKey()), entry.getValue()));
        }
        nodes.sort((n1, n2) -> n2.getWeight() - n1.getWeight());

        if (Main.arguments.debug) {
            System.out.println("[LOG] --- GENERATING HUFFMAN CODE");
            System.out.println("[LOG] --- ");
            System.out.print("[LOG] --- ");
            for (BinTree node : nodes) {
                System.out.printf("%s ( %d )  ", node.getData(), node.getWeight());
            }
            System.out.println();
            System.out.println("[LOG] --- Current list size: " + nodes.size());
            System.out.println("[LOG] --- ");
        }


        int i;
        while (nodes.size() > 1) {

            i = nodes.size() - 1;
            BinTree left = nodes.get(i - 1);
            BinTree right = nodes.get(i);

            BinTree combinedTree = new BinTree(
                    left.getData() + right.getData(),
                    left.getWeight() + right.getWeight(),
                    left, right);

            // After inserting the target at position moves any subsequent elements towards the end of list
            nodes.set(i - 1, combinedTree);
            nodes.remove(i);
            i--;

            if (nodes.size() > 1) {
                while(i >= 1 && nodes.get(i).getWeight() > nodes.get(i - 1).getWeight()) {
                    nodes.remove(i);
                    nodes.add(i - 1, combinedTree);
                    i--;
                }
            }

            if (Main.arguments.debug) {
                System.out.print("[LOG] --- ");
                for(BinTree tree : nodes) {
                    System.out.printf("%s ( %d )  ", tree.getData(), tree.getWeight());
                }
                System.out.println();
                System.out.println("[LOG] --- Current list size: " + nodes.size());
                System.out.println("[LOG] --- ");
            }
        }

//        this.huffmanTree = nodes.get(0);

        // Parse the tree to get the map that will represent the Huffman code:
        Map<Integer, String> huffmanMap = new HashMap<>();
        for (int searchChar : frequencies.keySet()) {
            BinTree baseLeft = nodes.get(0).getLeft();
            BinTree baseRight = nodes.get(0).getRight();
            StringBuilder accumulator = new StringBuilder("");
            String searchString = String.format("%c", searchChar);

            while (true) {
                if (baseLeft.getData().equals(searchString)) {
                    accumulator.append("0");
                    huffmanMap.put(searchChar, accumulator.toString());
                    break;
                } else if (baseRight.getData().equals(searchString)) {
                    accumulator.append("1");
                    huffmanMap.put(searchChar, accumulator.toString());
                    break;
                } else if (baseLeft.getData().contains(searchString)) {
                    accumulator.append("0");
                    baseRight = baseLeft.getRight();
                    baseLeft = baseLeft.getLeft();
                } else if (baseRight.getData().contains(searchString)) {
                    accumulator.append("1");
                    baseLeft = baseRight.getLeft();
                    baseRight = baseRight.getRight();
                }
            }
        }

        if (Main.arguments.debug) {
            System.out.print("[LOG] --- Generated: ");
            for (int key : huffmanMap.keySet()) {
                System.out.printf("%c(%s)  ", key, huffmanMap.get(key));
            }
            System.out.println();
            System.out.println("[LOG] --- ===================================================");
        }
        this.huffmanTable = huffmanMap;
    }

    public void encodeToFile(BufferedWriter writer) throws IOException {

        StringBuilder encFileContents = new StringBuilder("");
        String totalNumbers = Integer.toBinaryString(huffmanTable.keySet().size());
        encFileContents.append(totalNumbers);
        encFileContents.append("\n");

        int totalBitsInFile = totalNumbers.length();

        if (Main.arguments.debug) {
            System.out.println("[LOG] --- ENCODING TO A FILE (WITH ENCODING TABLE)");
            System.out.println("[LOG] --- ");
            System.out.println("[LOG] --- " + totalNumbers);
        }

        for (int character : huffmanTable.keySet()) {
            String currentCharacter = Integer.toBinaryString(character);
            String huffmanEquivalent = huffmanTable.get(character);
            encFileContents.append(currentCharacter);
            encFileContents.append("_");
            encFileContents.append(huffmanEquivalent);
            encFileContents.append("\n");

            if (Main.arguments.debug) {
                System.out.print("[LOG] --- " + Integer.toBinaryString(character));
                System.out.print("_");
                System.out.println(huffmanTable.get(character));
            }
            totalBitsInFile += currentCharacter.length();
            totalBitsInFile += huffmanEquivalent.length();
            totalBitsInFile += Integer.toBinaryString('_').length();
        }

        if (Main.arguments.debug) {
            System.out.print("[LOG] --- ");
        }
        for (int i = 0; i < fileContents.length(); i++) {
            int currentCharacter = fileContents.charAt(i);
            String huffmanEquivalent = huffmanTable.get(currentCharacter);
            encFileContents.append(huffmanEquivalent);
            if (Main.arguments.debug) {
                System.out.print(huffmanTable.get(currentCharacter));
            }
            totalBitsInFile += huffmanEquivalent.length();
        }

        this.encodedFileSize = totalBitsInFile;
        if(Main.arguments.debug) {
            System.out.println("\n[LOG] --- ===================================================");
        }
        writer.write(encFileContents.toString());
        calculateEntropy();
        calculateAvgWordLength();
    }

    private void calculateAvgWordLength() {
        for (int originalChar : frequencies.keySet()) {
            double charProb = (double)frequencies.get(originalChar) / fileContents.length();
            String huffmanEquivalent = huffmanTable.get(originalChar);
            this.avgWordLength += huffmanEquivalent.length() * charProb;
        }
    }

    public static void decodeFile(BufferedReader reader, BufferedWriter writer) throws IOException {
        Map<String, Integer> huffmanToChar = new HashMap<>();
        StringBuilder decodedMessage = new StringBuilder("");
        StringBuilder fileMessage = new StringBuilder("");
        StringBuilder accumulator = new StringBuilder("");


        // Read the encoding table:
        String loopTerminatorString = reader.readLine();
        int loopTerminator = Integer.parseInt(loopTerminatorString, 2);
        if (Main.arguments.debug) {
            System.out.println("[LOG] --- DECODING HUFFMAN FILE");
            System.out.println("[LOG] --- ");
            System.out.printf("[LOG] --- # of distinct characters: %d (%s)%n", loopTerminator, loopTerminatorString);
        }
        for (int i = 0; i < loopTerminator; i++) {
            String currentLine = reader.readLine();
            String[] currentTableRow = currentLine.split("_");
            int reps = Integer.parseInt(currentTableRow[0], 2);
            huffmanToChar.put(currentTableRow[1], reps);
            if (Main.arguments.debug) {
                System.out.printf("[LOG] --- Current line: %s [reps: %d(%s), char: %s] %n",
                        currentLine, reps, currentTableRow[0], currentTableRow[1]);
            }
        }

        String currentLine;
        while((currentLine = reader.readLine()) != null) {
            fileMessage.append(currentLine);
        }

        if (Main.arguments.debug) {
            System.out.println("[LOG] --- ");
            System.out.println("[LOG] --- Message:");
        }
        for(int i = 0; i < fileMessage.length(); i++) {
            accumulator.append(fileMessage.charAt(i));
            if (huffmanToChar.containsKey(accumulator.toString())) {
                int respectiveChar = huffmanToChar.get(accumulator.toString());
                decodedMessage.append(Character.toString(respectiveChar));
                if (Main.arguments.debug) {
                    System.out.printf("[LOG] --- %s -> %c %n", accumulator.toString(), respectiveChar);
                }
                accumulator = new StringBuilder("");
            }
        }
        System.out.println("[LOG] --- ===================================================");

        writer.write(decodedMessage.toString());
    }

    public int getEncodedFileSize() {
        return encodedFileSize;
    }

    public String getFileContents() {
        return fileContents;
    }

    public double getEntropy() {
        return entropy;
    }

    public double getAvgWordLength() {
        return avgWordLength;
    }
}
