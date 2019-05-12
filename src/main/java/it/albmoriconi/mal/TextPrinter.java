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

import it.albmoriconi.mal.program.TranslatedInstruction;
import it.albmoriconi.mal.program.TranslatedProgram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A naive text file printer for the translated program.
 * <p>
 * See {@link #printProgram} for detailed description.
 */
public class TextPrinter {

    private TextPrinter() { }

    /**
     * Text file printer for MAL assembled program.
     * <p>
     * Every row in the output is the word of the same index in the control store.
     * If more than one instruction in the translated program have the same address, which one is
     * preserved is undefined.
     *
     * @param program A translated program.
     * @param programWords The number of words in the control store.
     * @param filename The path of the output file.
     */
    public static void printProgram(TranslatedProgram program, int programWords, String filename) throws IOException {
        Map<Integer, TranslatedInstruction> controlStoreMapping = new HashMap<>();

        for (TranslatedInstruction ti : program.getInstructions())
            controlStoreMapping.put(ti.getAddress(), ti);

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

        for (int i = 0; i < programWords; i++) {
            TranslatedInstruction ci = controlStoreMapping.get(i);
            String instructionText = "";

            if (ci != null) {
                String nextAddressFormat = "%" + TranslatedInstruction.NEXT_ADDRESS_FIELD_LENGTH + "s";
                String nextAddress = Integer.toBinaryString(controlStoreMapping.get(i).getNextAddress());

                if (nextAddress.length() > TranslatedInstruction.NEXT_ADDRESS_FIELD_LENGTH)
                    nextAddress = nextAddress.substring(nextAddress.length() - TranslatedInstruction.NEXT_ADDRESS_FIELD_LENGTH);

                instructionText = String.format(nextAddressFormat, nextAddress).replace(" ", "0");

                for (int j = 0; j < TranslatedInstruction.CONTROL_FIELD_LENGTH; j++)
                    instructionText += (ci.getInstruction().get(j)) ? "1" : "0";

                writer.write(instructionText + "\n");
            } else {
                for (int j = 0; j < TranslatedInstruction.INSTRUCTION_LENGTH; j++)
                    instructionText += "0";
                    writer.write(instructionText + "\n");
            }
        }

        writer.flush();
    }
}
