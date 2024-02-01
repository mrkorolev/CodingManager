package org.example;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.*;

public class Main {
    public static Args arguments;

    public static class Args {
        @Parameter(names = {"-h", "--help"}, description = "description of how to use the CLI", help = true, order = 1)
        public boolean help;

        @Parameter(names = {"-enc", "--encode"}, description = "run the CLI in encoder mode", order = 1)
        public boolean encode;

        @Parameter(names = {"-sf", "--source-file"}, description = "name of source file (in the same directory) to be encoded", order = 2)
        public String sourceFile;

        @Parameter(names = {"-dec", "--decode"}, description = "run the CLI in decoder mode", order = 1)
        public boolean decode;

        @Parameter(names = {"-he", "--huffman-encoded"}, description = "Huffman encoded file (only for decoding)", order = 2)
        public String hFile;

        @Parameter(names = {"-rle", "--rl-encoded"}, description = "run-length encoded file (only for decoding)", order = 2)
        public String rlFile;

        @Parameter(names = {"-D", "--debug"}, description = "enable logs for every step of encoding/decoding", order = 3)
        public boolean debug;
    }

    public static void main(String[] args) {

        arguments = new Args();
        JCommander jc = JCommander.newBuilder().addObject(arguments).build();
        jc.parse(args);

        if (args.length == 0) {
            jc.usage();
        } else {
            analyzeCmdArgs(jc, arguments);
        }
    }

    public static void analyzeCmdArgs(JCommander jc, Args arguments) {
        if (arguments.help) {
            jc.usage();
        }  else if (arguments.encode && arguments.sourceFile != null) {
            encodeHuffmanAndRunlength(arguments.sourceFile);
        } else if (arguments.decode && arguments.hFile != null && arguments.rlFile != null) {
            decodeHuffmanAndRunlength(arguments.hFile, arguments.rlFile);
        } else if (arguments.decode && arguments.hFile != null) {
            decodeHuffman(arguments.hFile);
        } else if (arguments.decode && arguments.rlFile != null) {
            decodeRunlength(arguments.rlFile);
        } else {
            jc.usage();
        }
    }

    public static void encodeHuffmanAndRunlength(String sourceFile) {
        try {
            int filenameFinalIndex = sourceFile.lastIndexOf('.');
            BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
            HuffmanManager hManager = new HuffmanManager(reader);
            reader.close();

            hManager.generateHuffmanCode();
            BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile.substring(0, filenameFinalIndex) + "-HC.enc"));
            hManager.encodeToFile(writer);
            writer.close();

            reader = new BufferedReader(new FileReader(sourceFile));
            RLManager rlManager = new RLManager(reader);
            reader.close();

            writer = new BufferedWriter(new FileWriter(sourceFile.substring(0, filenameFinalIndex) + "-RL.enc"));
            rlManager.encode(writer);
            writer.close();

            int originalFileSize = 0;
            for(int i = 0; i < hManager.getFileContents().length(); i++) {
                int currentChar = hManager.getFileContents().charAt(i);
                originalFileSize += Integer.toBinaryString(currentChar).length();
            }
            System.out.printf("[LOG] --- Original file size: %d%n", originalFileSize);
            System.out.printf("[LOG] --- Huffman encoded file size (with separators and table): %d bits%n", hManager.getEncodedFileSize());
            System.out.printf("[LOG] --- Run-length encoded file size (with separators): %d bits%n", rlManager.getEncodedFileSize());
            System.out.printf("[LOG] --- H(X) = %.3f, entropy of the source file%n", hManager.getEntropy());
            System.out.printf("[LOG] --- H(Y - Huffman) = %.3f (the original entropy of source), average word length L = %.3f %n", hManager.getEntropy(), hManager.getAvgWordLength());
            System.out.printf("[LOG] --- H(Y - Run-length) = %.3f, average word length L = %.3f %n", rlManager.getEntropy(), rlManager.getAvgWordLength());
            System.out.println("[LOG] --- ===================================================");
        } catch(IOException e) {
            System.out.println("[LOG] --- ERROR: something went wrong when reading/writing from/to a file!");
        }
    }

    public static void decodeHuffmanAndRunlength(String hFilename, String rlFilename) {
        decodeHuffman(hFilename);
        decodeRunlength(rlFilename);
    }

    public static void decodeHuffman(String hFilename) {
        try {
            int filenameFinalIndex = hFilename.lastIndexOf('.');
            BufferedReader reader = new BufferedReader(new FileReader(hFilename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(hFilename.substring(0, filenameFinalIndex) + "-decoded.txt"));
            HuffmanManager.decodeFile(reader, writer);
            reader.close();
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void decodeRunlength(String rlFilename) {
        try{
            int filenameFinalIndex = rlFilename.lastIndexOf('.');
            BufferedReader reader = new BufferedReader(new FileReader(rlFilename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(rlFilename.substring(0, filenameFinalIndex) + "-decoded.txt"));

            RLManager.decode(reader.readLine(), writer);

            reader.close();
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}