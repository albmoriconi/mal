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
import me.albmoriconi.mal.program.Program;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Assembles input MAL source code.
 * <p>
 * The assembler currently parses the input source and outputs a text file, to be used with HDL tools.
 */
public class Assembler {

    private CharStream charStream;
    private int size;
    private int ifElseDisplacement;
    private Program program;

    /**
     * Constructor.
     *
     * @param charStream The character stream to assemble.
     * @param size The number of words in the control store.
     * @param ifElseDisplacement The displacement between if and else targets.
     */
    public Assembler(CharStream charStream, int size, int ifElseDisplacement) {
        this.charStream = charStream;
        this.size = size;
        this.ifElseDisplacement = ifElseDisplacement;
    }

    /**
     * Getter for program.
     *
     * @return The allocated program.
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Assemble content of given character stream.
     */
    public void assemble() {
        MalLexer lexer = new MalLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MalParser malParser = new MalParser(tokens);
        ParseTree tree = malParser.program();

        ParseTreeWalker walker = new ParseTreeWalker();
        Translator translator = new Translator();
        walker.walk(translator, tree);

        if (translator.getErrorState())
            throw new RuntimeException("Source contains errors");

        Allocator allocator = new Allocator(translator.getProgram(), size, ifElseDisplacement);
        allocator.allocate();

        program = allocator.getProgram();
    }
}
