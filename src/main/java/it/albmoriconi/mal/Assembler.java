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

package it.albmoriconi.mal;

import it.albmoriconi.mal.antlr.MalLexer;
import it.albmoriconi.mal.antlr.MalParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Assembles input MAL source code.
 * <p>
 * The assembler currently parses the input source and outputs a text file, to be used with HDL tools.
 */
public class Assembler {

    private static final int DEFAULT_PROGRAM_WORDS = 512;

    /**
     * Application entry point
     *
     * @param args Command line arguments.
     * @throws Exception Generic processing exception.
     */
    public static void main(String[] args) throws Exception {
        // TODO Add exception handling
        InputStream inputStream = System.in;

        String inputFile = null;
        if (args.length > 0)
            inputFile = args[0];
        if (inputFile != null)
            inputStream = new FileInputStream(inputFile);

        CharStream charStream = CharStreams.fromStream(inputStream);

        MalLexer lexer = new MalLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MalParser parser = new MalParser(tokens);
        ParseTree tree = parser.uProgram();

        ParseTreeWalker walker = new ParseTreeWalker();
        Translator translator = new Translator();
        walker.walk(translator, tree);

        // TODO Add binary file creation
        Allocator.process(translator.getProgram());
        TextPrinter.printProgram(translator.getProgram(), DEFAULT_PROGRAM_WORDS, "a.out");
    }
}
