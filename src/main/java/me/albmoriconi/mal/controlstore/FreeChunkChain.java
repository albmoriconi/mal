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

package me.albmoriconi.mal.controlstore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A chain of free chunks in the control store.
 * <p>
 * The chain is ordered, i.e. for any element <code>c1</code>, if c1 has a follower, be it
 * <code>c2</code>, it holds that <code>c1.endAddress &lt; c2.startAddress</code>.
 */
public class FreeChunkChain {

    private List<FreeChunk> freeChunks;

    /**
     * Constructor.
     *
     * @param memorySize Number of words in the control store.
     */
    public FreeChunkChain(int memorySize) {
        if (memorySize <= 0)
            throw new IllegalArgumentException("Invalid memory size");

        freeChunks = new LinkedList<>();
        freeChunks.add(new FreeChunk(0, memorySize - 1));
    }

    /**
     * Reclaims a specified control store region.
     * <p>
     * A control store region can only reclaimed if it's entirely contained in a single free chunk.
     *
     * @param startAddress First address of the control store region to reclaim.
     * @param endAddress Last address of the control store region to reclaim.
     */
    public void reclaim(int startAddress, int endAddress) {
        ListIterator<FreeChunk> it = freeChunks.listIterator();
        boolean canBeReclaimed = false;
        FreeChunk currentChunk = null;

        while (it.hasNext()) {
            currentChunk = it.next();

            if (currentChunk.getStartAddress() > startAddress)
                break;

            if (currentChunk.contains(startAddress, endAddress)) {
                canBeReclaimed = true;
                break;
            }
        }

        if (canBeReclaimed) {
            if (currentChunk.is(startAddress, endAddress)) // Reclaim entire chunk
                it.remove();
            else if (currentChunk.getStartAddress() == startAddress) // Reclaim right part of the chunk
                currentChunk.setStartAddress(endAddress + 1);
            else if (currentChunk.getEndAddress() == endAddress) // Reclaim left part of the chunk
                currentChunk.setEndAddress(startAddress - 1);
            else { // Split the chunk
                it.add(new FreeChunk(endAddress + 1, currentChunk.getEndAddress()));
                currentChunk.setEndAddress(startAddress - 1);
            }
        } else {
            throw new RuntimeException("Invalid control store region");
        }
    }

    /**
     * Gets the starting address of a chunk that's at least of size <code>blockSize</code>.
     *
     * @param blockSize The desired size.
     * @return The starting address of a chunk that's at least of size <code>blockSize</code>.
     */
    public int getChunkGt(int blockSize) {
        for (FreeChunk fc : freeChunks) {
            if (fc.size() >= blockSize)
                return fc.getStartAddress();
        }

        throw new RuntimeException("Unable to find suitable chunk");
    }

    /**
     * Gets the starting address of two regions according to size and displacement rules.
     *
     * @param blockSize1 Size of first region.
     * @param blockSize2 Size of second region.
     * @param displacement Displacement between regions.
     * @return The starting addresses of the two regions. First element is lower address.
     */
    public List<Integer> getDisplacedRegions(int blockSize1, int blockSize2, int displacement) {
        List<Integer> thePair = new ArrayList<>();

        for (FreeChunk firstChunk : freeChunks) {
            if (firstChunk.size() >= blockSize1) {
                // Look for another possible block
                for (FreeChunk secondChunk : freeChunks) {
                    // e.g. firstChunk = [5, 9], blockSize1 = 3, displacement = 50
                    // then second region can start at 55, 56 or 57
                    // it can't start at 58 or first region wouldn't fit in firstChunk
                    int region2StartLower = firstChunk.getStartAddress() + displacement;
                    int region2StartUpper = region2StartLower + blockSize1 - 1;

                    if (secondChunk.getEndAddress() < region2StartLower)
                        continue; // We are behind the window, get next chunk
                    else if (secondChunk.getStartAddress() > region2StartUpper)
                        break; // We are past the window, try with another first chunk

                    // Otherwise, we also have to check size
                    for (int i = region2StartLower; i <= region2StartUpper; i++) {
                        if (secondChunk.contains(i, i + blockSize2 - 1)) {
                            thePair.add(firstChunk.getStartAddress());
                            thePair.add(secondChunk.getStartAddress());

                            if (secondChunk.getStartAddress() > region2StartLower)
                                thePair.set(0, secondChunk.getStartAddress() - displacement);
                            else if (secondChunk.getStartAddress() < region2StartLower)
                                thePair.set(1, firstChunk.getStartAddress() + displacement);

                            return thePair;
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Unable to find suitable chunk pair");
    }
}
