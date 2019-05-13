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

import me.albmoriconi.mal.program.Instruction;
import me.albmoriconi.mal.program.Program;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Binary file writer for MAL assembled program.
 * <p>
 * See {@link #write} for detailed description.
 */
public class BinaryWriter {

    private static final int BYTE_SIZE = 8;
    private static final int BINARY_RADIX = 2;

    private BinaryWriter() { }

    /**
     * Binary file writer for MAL assembled program.
     * <p>
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

        int programBits = programWords * Instruction.INSTRUCTION_LENGTH;
        programBits += programBits % BYTE_SIZE; // Ensure output contains entire number of bytes
        StringBuilder programText = new StringBuilder(programBits);

        for (int i = 0; i < programWords; i++) {
            Instruction ci = controlStoreMapping.get(i);
            programText.append(Objects.requireNonNullElseGet(ci, () -> "0".repeat(Instruction.INSTRUCTION_LENGTH)));
        }
        programText.append("0".repeat(programBits - programText.toString().length()));

        int programBytes = programBits / BYTE_SIZE;
        byte[] outputBytes = new byte[programBytes];

        for (int i = 0; i < programBytes - 1; i++) {
            int wordStart = i * BYTE_SIZE;
            int wordEnd = wordStart + BYTE_SIZE;
            outputBytes[i] = (byte) Integer.parseInt(programText.toString().substring(wordStart, wordEnd), BINARY_RADIX);
        }

        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(fileName));
        writer.write(outputBytes);
        writer.flush();
    }
}
