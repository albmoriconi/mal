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

package it.albmoriconi.mal.writer;

import it.albmoriconi.mal.program.Instruction;
import it.albmoriconi.mal.program.Program;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Text file writer for for MAL assembled program.
 * <p>
 * See {@link #write} for detailed description.
 */
public class TextWriter {

    private TextWriter() { }

    /**
     * Text file writer for MAL assembled program.
     * <p>
     * Every row in the output is the word of the same index in the control store.
     * If more than one instruction in the translated program have the same address, which one is
     * preserved is undefined.
     *
     * @param program A MAL program.
     * @param programWords The number of words in the control store.
     * @param fileName The path of the output file.
     *
     * @throws IOException If file can't be opened for any reason.
     */
    public static void write(Program program, int programWords, String fileName) throws IOException {
        Map<Integer, Instruction> controlStoreMapping = new HashMap<>();

        for (Instruction ti : program.getInstructions())
            controlStoreMapping.put(ti.getAddress(), ti);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        for (int i = 0; i < programWords; i++) {
            Instruction ci = controlStoreMapping.get(i);
            writer.write(Objects.requireNonNullElseGet(ci, () -> "0".repeat(Instruction.INSTRUCTION_LENGTH)) + "\n");
        }

        writer.flush();
    }
}
