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

package it.albmoriconi.mal.memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a chain of free memory chunks in the control store.
 * <p>
 * The chain is ordered, i.e. for any element <code>c1</code>, if c1 has a follower, be it
 * <code>c2</code>, it holds that <code>c1.endAddress < c2.startAddress</code>.
 */
public class FreeChunkChain {

    private List<FreeChunk> freeChunks;

    /**
     * Index of the start of else block allocation in the list returned by {@link #getChunkPairGt}.
     */
    public static final int ELSE_START_I = 0;

    /**
     * Index of the start of if block allocation in the list returned by {@link #getChunkPairGt}.
     */
    public static final int IF_START_I = 1;

    /**
     * Address returned when a chunk request can't be satisfied
     */
    public static final int NO_SUCH_ADDRESS = -1;

    /**
     * Constructor.
     *
     * @param memorySize Number of words in the control store.
     */
    public FreeChunkChain(int memorySize) {
        freeChunks = new LinkedList<>();
        freeChunks.add(new FreeChunk(0, memorySize - 1));
    }

    /**
     * Check if memory region can be reclaimed.
     * <p>
     * A memory region can only reclaimed if it's entirely contained in a single free chunk.
     *
     * @param startAddress First address of the memory region.
     * @param endAddress Last address of the memory region.
     * @return <code>true</code> if, and only if, the memory region can be reclaimed.
     */
    public boolean canBeReclaimed(int startAddress, int endAddress) {
        return reclaim(startAddress, endAddress, false);
    }

    /**
     * Tries to reclaim a specified memory region, signaling it's been allocated.
     * <p>
     * A memory region can only reclaimed if it's entirely contained in a single free chunk.
     *
     * @param startAddress First address of the memory region to reclaim.
     * @param endAddress Last address of the memory region to reclaim.
     * @return <code>true</code> if, and only if, the memory region has been reclaimed.
     */
    public boolean reclaim(int startAddress, int endAddress) {
        return reclaim(startAddress, endAddress, true);
    }

    private boolean reclaim(int startAddress, int endAddress, boolean doReclaim) {
        // TODO Add exception handling (can't be reclaimed) - Remove return value? -
        // TODO Ideally, the SMALLEST chunk that can contain the allocation should be used
        // TODO Remove duplicate code
        ListIterator<FreeChunk> it = freeChunks.listIterator();
        boolean canBeReclaimed = false;
        FreeChunk currentChunk = null;

        while (it.hasNext()) {
            currentChunk = it.next();

            if (currentChunk.startsAfter(startAddress))
                break;

            if (currentChunk.contains(startAddress, endAddress)) {
                canBeReclaimed = true;
                break;
            }
        }

        if (canBeReclaimed && doReclaim) {
            if (currentChunk.is(startAddress, endAddress))
                it.remove();
            if (currentChunk.startsAt(startAddress))
                currentChunk.cutFromStartTo(endAddress);
            else if (currentChunk.endsAt(endAddress))
                currentChunk.cutFrom(startAddress);
            else {
                it.add(new FreeChunk(endAddress + 1, currentChunk.getEndAddress()));
                currentChunk.setEndAddress(startAddress - 1);
            }
        }

        return canBeReclaimed;
    }

    /**
     * Gets a chunk that's at least of size <code>blockSize</code>.
     *
     * @param blockSize The desired size.
     * @return The starting address of a chunk that's at least of size <code>blockSize</code>.
     */
    public int getChunkGt(int blockSize) {
        for (FreeChunk fc : freeChunks) {
            if (fc.size() >= blockSize)
                return fc.getStartAddress();
        }

        return NO_SUCH_ADDRESS;
    }

    /**
     * Gets a pair of chunks according to size and displacement rules.
     *
     * @param blockSize1 Size of first block.
     * @param blockSize2 Size of second block.
     * @param displacement Displacement between allocation of the two blocks.
     * @return The starting addresses of a pair of chunks that satisfy conditions on size and displacement.
     */
    public List<Integer> getChunkPairGt(int blockSize1, int blockSize2, int displacement) {
        List<Integer> thePair = new ArrayList<>();
        thePair.add(NO_SUCH_ADDRESS);
        thePair.add(NO_SUCH_ADDRESS);

        // TODO We iterate twice on the entire list, but ArrayList is probably not an option
        for (FreeChunk elseChunk : freeChunks) {
            if (elseChunk.size() >= blockSize1) {
                // Look for another possible block
                for (FreeChunk ifChunk : freeChunks) {
                    // If e.g. elseChunk = [5, 9], bs1 = 3, disp = 50 then block in ifChunk can start at 55, 56 or 57
                    int block2StartLower = elseChunk.getStartAddress() + displacement;
                    int block2StartUpper = block2StartLower + blockSize1 - 1;

                    // If we are behind or after this window, current chunk is no good
                    if (ifChunk.getEndAddress() < block2StartLower)
                        continue;
                    else if (ifChunk.getStartAddress() > block2StartUpper)
                        break;

                    // Otherwise, we also have to check size
                    for (int i = block2StartLower; i <= block2StartUpper; i++) {
                        if (ifChunk.contains(i, i + blockSize2)) {
                            // Ok, we found a pair
                            thePair.set(ELSE_START_I, elseChunk.getStartAddress());
                            thePair.set(IF_START_I, ifChunk.getStartAddress());

                            if (ifChunk.getStartAddress() > block2StartLower)
                                thePair.set(ELSE_START_I, ifChunk.getStartAddress() - displacement);
                            else if (ifChunk.getStartAddress() < block2StartLower)
                                thePair.set(IF_START_I, elseChunk.getStartAddress() + displacement);

                            return thePair;
                        }
                    }
                }
            }
        }

        return thePair;
    }
}
