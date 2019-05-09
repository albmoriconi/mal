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
 * Determines the address fields for a translated microprogram.
 * <p>
 * The translation of a microprogram source code does not contain all information on instruction
 * address and next instruction address.
 * They have to be determined based on a series of constraints (e.g. distance between if/else
 * branches).
 */
public class ProgramAllocator {
    /**
     * The translated microprogram.
     */
    private TranslatedProgram uProgram;

    /**
     * Constructor.
     *
     * @param uProgram The translated microprogram.
     */
    public ProgramAllocator(TranslatedProgram uProgram) {
        this.uProgram = uProgram;
    }

    /**
     * Processes the microprogram, filling the missing address fields.
     */
    public void process() {
        boolean contiguousAllocation = false;
        int previousAddress = TranslatedInstruction.UNDETERMINED;

        // TODO Complete location assignment
        // First pass: solve contraints on contiguous allocations
        // Scan program
        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            // On finding an instruction that is already allocated
            if (ti.getAddress() >= 0) {
                // start a contiguous allocation
                contiguousAllocation = true;
                previousAddress = ti.getAddress();
            } else {
                // otherwise, instruction is not already allocated
                // if we are in a contiguous allocation
                if (contiguousAllocation) {
                    // and there's no label, we can allocate
                    // TODO should be: if there's no ENTRY IN IF TABLE
                    if (ti.getLabel().isEmpty()) {
                        ti.setAddress(previousAddress + 1);
                        previousAddress = ti.getAddress();
                    } else {
                        // otherwise stop contiguous allocation
                        contiguousAllocation = false;
                    }
                }
            }

            // Iny case, if instruction is now allocated and has no next label
            if (ti.getAddress() >= 0 && ti.getNextLabel().isEmpty()) {
                // next address is current address + 1
                ti.setNextAddress(ti.getAddress() + 1);
            }
        }

        // Second pass: solve costraints on ifs

        // Third pass: solve contraints on remaining unallocated instructions

        // Last pass: labeled gotos
        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            if (uProgram.getAllocations().containsKey(ti.getNextLabel()))
                ti.setNextAddress(uProgram.getAllocations().get(ti.getNextLabel()));
        }
    }
}
