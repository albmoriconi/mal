/*
 * Copyright (C) 2019 Alberto Moriconi
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.albmoriconi.mal;

import me.albmoriconi.mal.antlr.MalLexer;
import me.albmoriconi.mal.antlr.MalParser;
import me.albmoriconi.mal.writer.BaseProgramWriter;
import me.albmoriconi.mal.writer.BinaryWriter;
import me.albmoriconi.mal.writer.TextWriter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Assembles input MAL source code.
 * <p>
 * The assembler currently parses the input source and outputs a text file, to be used with HDL tools.
 */
public class Assembler {

    private static final int DEFAULT_PROGRAM_WORDS = 512;
    private static final String DEFAULT_OUT_TEXT = "a.txt";
    private static final String DEFAULT_OUT_BINARY = "a.out";

    private static String inFile;
    private static String outFile;
    private static boolean textFormat;

    /**
     * Application entry point
     *
     * @param args Command line arguments. See usage for details.
     */
    public static void main(String[] args) {
        parseOptions(args);

        CharStream charStream = null;
        try {
            InputStream inputStream = new FileInputStream(inFile);
            charStream = CharStreams.fromStream(inputStream);
        } catch (IOException ex) {
            System.out.println("mal: No such file: " + inFile);
            System.exit(1);
        }

        MalLexer lexer = new MalLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MalParser malParser = new MalParser(tokens);
        ParseTree tree = malParser.program();

        ParseTreeWalker walker = new ParseTreeWalker();
        Translator translator = new Translator();
        walker.walk(translator, tree);

        if (translator.getErrorState()) {
            System.out.println("mal: Source contains errors");
            System.exit(1);
        }

        Allocator.process(translator.getProgram());
        try {
            BaseProgramWriter writer = textFormat ? new TextWriter(outFile) : new BinaryWriter(outFile);
            writer.write(translator.getProgram(), DEFAULT_PROGRAM_WORDS);
        } catch (IOException ex) {
            System.out.println("mal: Can't write file: " + outFile);
            System.exit(1);
        }
    }

    private static void parseOptions(String[] args) {
        Options options = getOptions();
        CommandLineParser cmdParser = new DefaultParser();

        // Parse command line
        CommandLine cmd = null;
        try {
            cmd = cmdParser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println("mal: " + ex.getMessage());
            System.exit(1);
        }

        // Print help message
        if (cmd.hasOption("h")) {
            printHelp(options);
            System.exit(0);
        }

        // Set input file
        try {
            inFile = cmd.getArgList().get(0);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("mal: No input file");
            System.exit(1);
        }

        // Set format
        if (cmd.hasOption("f")) {
            String formatArgument = cmd.getOptionValue("f");
            if (formatArgument.equals("binary"))
                textFormat = false;
            else if (formatArgument.equals("text"))
                textFormat = true;
            else {
                System.out.println("mal: Unrecognized argument for option: f");
            }
        }

        // Set output file
        outFile = cmd.hasOption("o") ? cmd.getOptionValue("o") :
                (textFormat ? DEFAULT_OUT_TEXT : DEFAULT_OUT_BINARY);
    }

    private static Options getOptions() {
        Option outFile = Option.builder("o")
                .argName("file")
                .hasArg()
                .desc("write output to <file>")
                .build();

        Option format = Option.builder("f")
                .argName("format")
                .hasArg()
                .desc("output format: binary (default) | text")
                .build();

        Option help = Option.builder("h")
                .longOpt("help")
                .desc("display this help and exit")
                .build();

        Options options = new Options();
        options.addOption(outFile);
        options.addOption(format);
        options.addOption(help);

        return options;
    }

    private static void printHelp(Options options) {
        String header = "Assembler for MIC-1 Micro-Assembly language\n\noptions:\n";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("mal [options] <input>", header, options, "",false);
    }
}
