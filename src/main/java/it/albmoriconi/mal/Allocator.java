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

import it.albmoriconi.mal.controlstore.FreeChunkChain;
import it.albmoriconi.mal.program.Instruction;
import it.albmoriconi.mal.program.Program;

import java.util.List;

/**
 * Determines the address and next address fields for instructions in a program.
 * <p>
 * The translation of a MAL source code does not contain all information on instruction addresses
 * and next instruction addresses. Some of them have to be determined based on a series of constraints.
 */
class Allocator {

    private static final int PROGRAM_WORDS = 512;
    private static final int IF_ELSE_DISPLACEMENT = 256;

    private Allocator() { }

    /**
     * Processes the program, filling the missing address and next address fields.
     *
     * @param program The translated program.
     */
    static void process(Program program) {
        // Allocations during the processing have to update a free chunk map.
        FreeChunkChain freeChunks = new FreeChunkChain(PROGRAM_WORDS);

        // First step: honour reclaim promises made by the translator.
        // We do this in the allocator because the translator does not know the number of words and
        // can't keep a free chunk chain.
        for (int startAddress : program.getReclaimPromises().keySet()) {
            int endAddress = program.getReclaimPromises().get(startAddress);
            freeChunks.reclaim(startAddress, endAddress);
        }

        // Second step: allocate annotated blocks (i.e. entry point and blocks without placement label)
        allocateBlocks(program, freeChunks);

        // Third step: now we know all the targets, set next address for remaining instructions with goto (or halt)
        for (Instruction ti : program.getInstructions()) {
            if (!ti.hasNextAddress() && program.getAddressForLabel().containsKey(ti.getTargetLabel()))
                ti.setNextAddress(program.getAddressForLabel().get(ti.getTargetLabel()));
            else if (ti.isHalt())
                ti.setNextAddress(ti.getAddress());
        }
    }

    private static void allocateBlocks(Program program, FreeChunkChain freeChunks) {
        // Iterate over instruction counts where blocks starts
        for (int ic : program.getBlockAnnotations().keySet()) {
            String blockLabel = program.getInstructions().get(ic).getLabel();
            int blockSize = program.getBlockAnnotations().get(ic);

            if (program.hasIfElseTarget(blockLabel)) { // If it's an if/else target block allocate them together
                String pairBlockLabel = program.getOtherTargetInPair(blockLabel);
                int pairBlockCount = program.getCountForLabel().get(pairBlockLabel);
                int pairBlockSize = program.getBlockAnnotations().get(pairBlockCount);

                boolean blockIsIf = program.isIfTarget(blockLabel);

                String ifLabel = blockIsIf ? blockLabel : pairBlockLabel;
                int ifBlockSize = blockIsIf ? blockSize : pairBlockSize;
                String elseLabel = blockIsIf ? pairBlockLabel : blockLabel;
                int elseBlockSize = blockIsIf ? pairBlockSize : blockSize;

                List<Integer> regionPair = freeChunks.getDisplacedRegions(elseBlockSize, ifBlockSize, IF_ELSE_DISPLACEMENT);
                allocateBlock(program, freeChunks, elseLabel, regionPair.get(0));
                allocateBlock(program, freeChunks, ifLabel, regionPair.get(1));
            } else { // Otherwise it's a simple block, allocate at start of first useful chunk
                allocateBlock(program, freeChunks, blockLabel, freeChunks.getChunkGt(blockSize));
            }
        }
    }

    private static void allocateBlock(Program program, FreeChunkChain freeChunks, String label, int firstAddress) {
        int labelCount = label.isEmpty() ? 0 : program.getCountForLabel().get(label);
        int blockSize = program.getBlockAnnotations().get(labelCount);

        // Reclaim chunk
        freeChunks.reclaim(firstAddress, firstAddress + blockSize - 1);

        // Add entry for label
        if (!label.isEmpty())
            program.getAddressForLabel().put(label, firstAddress);

        // Fill address and next address
        for (int ic = labelCount; ic < labelCount + blockSize - 1; ic++) {
            program.getInstructions().get(ic).setAddress(firstAddress++);
            program.getInstructions().get(ic).setNextAddress(firstAddress);
        }
        program.getInstructions().get(labelCount + blockSize - 1).setAddress(firstAddress);
    }
}
