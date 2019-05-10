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
     * Gets the first free address of the first chank of the chain.
     *
     * @return The first free address of the first chank of the chain.
     */
    public int firstFree() {
        return freeChunks.get(0).getStartAddress();
    }


    /**
     * Gets the last free address of the last chunk of the chain.
     *
     * @return The last free address of the last chunk of the chain.
     */
    public int lastFree() {
        // TODO Implement
        return 512;
    }
}
