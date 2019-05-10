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

import java.util.List;

/**
 * Offers functionality to determine the address and next address fields for a translated microprogram.
 * <p>
 * The translation of a microprogram source code does not contain all information on instruction
 * address and next instruction address. They have to be determined based on a series of constraints
 * (e.g. distance between if/else branches).
 */
public class MalAllocator {

    private static final int IF_ELSE_DISPLACEMENT = 256;

    private MalAllocator() { }

    /**
     * Processes the microprogram, filling the missing address fields.
     *
     * @param uProgram The translated microprogram.
     */
    public static void process(TranslatedProgram uProgram, int programWords) {
        // Allocations during the processing have to update a free chunk map.
        FreeChunkChain freeChunks = new FreeChunkChain(programWords);

        // First step: honour reclaim promises made by the translator.
        // We do this in the allocator because the translator does not know the number of words and
        // can't mantain a free chunk chain.
        for (FreeChunk fc : uProgram.getReclaimPromises())
            freeChunks.reclaim(fc.getStartAddress(), fc.getEndAddress());

        // Second step: allocate annotated blocks (i.e. blocks without explicit address in the source)
        allocateBlocks(uProgram, freeChunks);

        // Third step: now we know all the targets, set next address for remaining instructions with goto
        // TODO Should we handle halt at this step?
        nextAddressForGotos(uProgram);
    }

    // TODO Refactor
    // TODO Add special case for if / else where one is allocated, the other is not
    private static void allocateBlocks(TranslatedProgram uProgram, FreeChunkChain freeChunks) {
        // TODO Add exception handling
        if (uProgram.hasInvalidIfStatements())
            return;

        // Iterate over instruction counts where blocks starts
        for (int ic : uProgram.getBlockAnnotations().keySet()) {
            String blockLabel = uProgram.getInstructions().get(ic).getLabel();
            int blockSize = uProgram.getBlockAnnotations().get(ic);

            if (uProgram.hasIfElseTarget(blockLabel)) { // If it's an if/else target block allocate them together
                String pairBlockLabel = uProgram.getOtherTargetInPair(blockLabel);
                String ifLabel = "";
                String elseLabel = "";
                int pairBlockCount = uProgram.getCountForLabel().get(pairBlockLabel);
                int pairBlockSize = uProgram.getBlockAnnotations().get(pairBlockCount);
                int ifCount = TranslatedInstruction.UNDETERMINED;
                int elseCount = TranslatedInstruction.UNDETERMINED;
                int ifBlockSize = TranslatedInstruction.UNDETERMINED;
                int elseBlockSize = TranslatedInstruction.UNDETERMINED;

                if (uProgram.isIfTarget(blockLabel)) {
                    ifLabel = blockLabel;
                    elseLabel = pairBlockLabel;
                    ifCount = ic;
                    ifBlockSize = blockSize;
                    elseCount = pairBlockCount;
                    elseBlockSize = pairBlockSize;
                } else {
                    ifLabel = pairBlockLabel;
                    elseLabel = blockLabel;
                    ifCount = pairBlockCount;
                    ifBlockSize = pairBlockSize;
                    elseCount = ic;
                    elseBlockSize = blockSize;
                }

                List<Integer> chunkPair = freeChunks.getChunkPairGt(elseBlockSize, ifBlockSize, IF_ELSE_DISPLACEMENT);
                int elseChunkStart = chunkPair.get(0);
                int ifChunkStart = chunkPair.get(1);
                freeChunks.reclaim(ifChunkStart, ifChunkStart + ifBlockSize - 1);
                freeChunks.reclaim(elseChunkStart, elseChunkStart + elseBlockSize - 1);
                allocateBlock(uProgram, ifCount, ifChunkStart, ifBlockSize);
                allocateBlock(uProgram, elseCount, elseChunkStart, elseBlockSize);
                uProgram.getAddressForLabel().put(ifLabel, ifChunkStart);
                uProgram.getAddressForLabel().put(elseLabel, elseChunkStart);
            } else { // Otherwise it's a simple block, allocate wherever
                int fcStart = freeChunks.getChunkGt(blockSize);
                freeChunks.reclaim(fcStart, fcStart + blockSize - 1);
                allocateBlock(uProgram, ic, fcStart, blockSize);
                if (!blockLabel.isEmpty()) // Consider exception for entry point
                    uProgram.getAddressForLabel().put(blockLabel, fcStart);
            }

        }
    }

    private static void allocateBlock(TranslatedProgram uProgram, int instructionCount, int firstAddress, int blockSize) {
        for (int ic = instructionCount; ic < instructionCount + blockSize - 1; ic++) {
            uProgram.getInstructions().get(ic).setAddress(firstAddress++);
            uProgram.getInstructions().get(ic).setNextAddress(firstAddress);
        }
        uProgram.getInstructions().get(instructionCount + blockSize - 1).setAddress(firstAddress);
    }

    private static void nextAddressForGotos(TranslatedProgram uProgram) {
        for (TranslatedInstruction ti : uProgram.getInstructions()) {
            if (!ti.hasNextAddress() && uProgram.getAddressForLabel().containsKey(ti.getTargetLabel()))
                ti.setNextAddress(uProgram.getAddressForLabel().get(ti.getTargetLabel()));
            else if (ti.isHalt())
                ti.setNextAddress(ti.getAddress());
        }
    }
}
