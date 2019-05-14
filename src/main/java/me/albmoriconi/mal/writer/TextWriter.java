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

package me.albmoriconi.mal.writer;

import me.albmoriconi.mal.program.Program;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Text file writer for for MAL assembled program.
 * <p>
 * See {@link #write} for detailed description.
 */
public class TextWriter extends BaseProgramWriter {

    private final BufferedWriter writer;

    /**
     * Constructor.
     *
     * @param fileName The path of the output file.
     *
     * @throws IOException If file can't be opened for any reason.
     */
    public TextWriter(String fileName) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(fileName));
    }

    /**
     * Text file writer for MAL assembled program.
     * <p>
     * Every row in the output is the word of the same index in the control store.
     * If more than one instruction in the translated program have the same address, which one is
     * preserved is undefined.
     *
     * @param program A MAL program.
     * @param size The number of words in the control store.
     *
     * @throws IOException If an IO error occurs.
     */
    @Override public void write(Program program, int size) throws IOException {
        for (String word : program.getWords(size))
            writer.write(word + "\n");
        writer.flush();
    }
}
