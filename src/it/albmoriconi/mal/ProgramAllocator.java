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

/**
 * Offers functionality to determine the address fields for a translated microprogram.
 * <p>
 * The translation of a microprogram source code does not contain all information on instruction
 * address and next instruction address.
 * They have to be determined based on a series of constraints (e.g. distance between if/else
 * branches).
 */
public class ProgramAllocator {

    private ProgramAllocator() { }

    /**
     * Processes the microprogram, filling the missing address fields.
     *
     * @param uProgram The translated microprogram.
     */
    public static void process(TranslatedProgram uProgram) {
        // First step: set next address for instructions with goto statements.
        // This step is only useful when goto statements are found in the source before the label
        // they targets, and these labels have explicit addresses.
        // It often produces no results: in microprograms usually only instructions have explicit
        // addresses, and entire instructions are rarely targets for gotos.
        nextAddressForGotos(uProgram);

        // Second step: contiguous allocation of instruction blocks.
        // When an allocated instruction is found, the following instructions are to be allocated
        // contiguously until (including) an instruction with a control statement is found.
        // The next address can also be set until (excluding) the same instruction.
        allocateContiguously(uProgram);

        // Third step: solve constraints on if statements.

        // Fourth step: solve constraints on remaining unallocated instructions.

        // Fifth step: set next address for remaining instructions with goto statements.
        nextAddressForGotos(uProgram);
    }

    private static void allocateContiguously(TranslatedProgram uProgram) {
        boolean contiguousAllocation = false;
        int contiguousAddress = TranslatedInstruction.UNDETERMINED;

        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            if (ti.hasAddress()) {
                contiguousAllocation = true;
                contiguousAddress = ti.getAddress();
            } else if (contiguousAllocation) {
                ti.setAddress(++contiguousAddress);

                if (ti.hasLabel())
                    uProgram.getAllocations().put(ti.getLabel(), contiguousAddress);

                // At this point, it's OK to assume that only instructions with control statement
                // have a defined successor.
                if (ti.hasSuccessor())
                    contiguousAllocation = false;
            }

            if (ti.hasAddress() && !ti.hasSuccessor())
                ti.setNextAddress(ti.getAddress() + 1);
        }
    }

    /**
     * Processes the program, filling the next address field for instructions with goto statements
     * with labels with determined addresses and no next address field.
     *
     * @param uProgram The translated microprogram.
     */
    private static void nextAddressForGotos(TranslatedProgram uProgram) {
        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            if (!ti.hasNextAddress() && uProgram.getAllocations().containsKey(ti.getTargetLabel()))
                ti.setNextAddress(uProgram.getAllocations().get(ti.getTargetLabel()));
        }
    }
}
