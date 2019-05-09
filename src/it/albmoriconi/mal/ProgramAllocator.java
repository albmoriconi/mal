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
    public static void process(TranslatedProgram uProgram, int programWords) {
        // Allocations during the processing have to update a free chunk map.
        FreeChunkChain freeChunks = new FreeChunkChain(programWords);

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
        allocateContiguously(uProgram, freeChunks);

        // Third step: solve constraints on if statements.
        // The if target has to be allocated at an address that's exactly 256 more than the else
        // target; therefore, two regions of adequate size have to be found at a 256-words interval.
        allocateIfElseTargets(uProgram, freeChunks);

        // Fourth step: solve constraints on remaining unallocated instructions.
        // At this point, the only instructions that can be not allocated are contiguous blocks
        // following a label, with the exception of a contiguous block at the beginning of source
        // (i.e. the microprogram entry point).

        // Fifth step: set next address for remaining instructions with goto statements.
        // At this point, unallocated code is considered unreachable.
        nextAddressForGotos(uProgram);
    }

    private static void nextAddressForGotos(TranslatedProgram uProgram) {
        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            if (!ti.hasNextAddress() && uProgram.getAllocations().containsKey(ti.getTargetLabel()))
                ti.setNextAddress(uProgram.getAllocations().get(ti.getTargetLabel()));
        }
    }

    private static void allocateContiguously(TranslatedProgram uProgram, FreeChunkChain freeChunks) {
        boolean contiguousAllocation = false;
        int firstAddress = TranslatedInstruction.UNDETERMINED;
        int currentAddress = TranslatedInstruction.UNDETERMINED;

        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            if (ti.hasAddress()) {
                contiguousAllocation = true;
                firstAddress = ti.getAddress();
                currentAddress = firstAddress + 1;
            } else if (contiguousAllocation) {
                ti.setAddress(currentAddress);

                // TODO Should check for label re-allocation?
                if (ti.hasLabel())
                    uProgram.getAllocations().put(ti.getLabel(), currentAddress);

                if (ti.hasSuccessor()) {
                    contiguousAllocation = false;
                    // TODO Add check for errors, unless exceptions are added to reclaim()
                    freeChunks.reclaim(firstAddress, currentAddress);
                }

                currentAddress++;
            }

            if (ti.hasAddress() && !ti.hasSuccessor())
                ti.setNextAddress(ti.getAddress() + 1);
        }
    }

    private static void allocateIfElseTargets(TranslatedProgram uProgram, FreeChunkChain freeChunks) {
        if (uProgram.hasInvalidIfStatements())
            // TODO Should throw an exception?
            return;

        // TODO Refactor using an IfElseTarget class?
        // TODO Complete allocation
        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            String currentLabel = ti.getLabel();
            String currentIfLabel = "";
            String currentElseLabel = "";
            int currentIfBlockSize = 0;
            int currentElseBlockSize = 0;

            if (ti.hasLabel() && uProgram.hasIfElseTarget(currentLabel)) {
                if (uProgram.isElseTarget(currentLabel)) {
                    currentElseLabel = currentLabel;
                    currentIfLabel = uProgram.getOtherTargetInPair(currentLabel);
                } else {
                    currentIfLabel = currentLabel;
                    currentElseLabel = uProgram.getOtherTargetInPair(currentLabel);
                }
                currentIfBlockSize++;
                currentElseBlockSize++;
            }
        }
    }
}
