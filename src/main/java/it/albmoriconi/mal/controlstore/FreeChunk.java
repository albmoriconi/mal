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

package it.albmoriconi.mal.controlstore;

/**
 * Represents a contiguous chunk of memory words in the program control store.
 */
class FreeChunk {

    private int startAddress;
    private int endAddress;

    /**
     * Constructor.
     *
     * @param startAddress First address of the chunk.
     * @param endAddress Last address of the chunk.
     */
    FreeChunk(int startAddress, int endAddress) {
        if (startAddress > endAddress)
            throw new IllegalArgumentException("Illegal chunk size");

        setStartAddress(startAddress);
        setEndAddress(endAddress);
    }

    /**
     * Returns the size of the chunk.
     *
     * @return The size of the chunk.
     */
    int size() {
        return endAddress - startAddress + 1;
    }

    /**
     * Checks if an address interval belongs to the chunk.
     *
     * @param startAddress First address of the interval.
     * @param endAddress Last address of the interval.
     * @return <code>true</code> if, and only if, the interval belongs to the chunk.
     */
    boolean contains(int startAddress, int endAddress) {
        return startAddress <= endAddress && startAddress >= this.startAddress && endAddress <= this.endAddress;
    }

    /**
     * Checks if the chunk is exactly the one delimited by the address pair.
     *
     * @param startAddress The starting address.
     * @param endAddress The ending address.
     * @return <code>true</code> if, and only if, the chunk is the one delimited by the given pair.
     */
    boolean is(int startAddress, int endAddress) {
        return this.getStartAddress() == startAddress && this.getEndAddress() == endAddress;
    }

    /**
     * Getter for startAddress.
     *
     * @return First address of the chunk.
     */
    int getStartAddress() {
        return startAddress;
    }

    /**
     * Setter for startAddress.
     *
     * @param startAddress First address of the chunk.
     */
    void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    /**
     * Getter for endAddress.
     *
     * @return Last address of the chunk.
     */
    int getEndAddress() {
        return endAddress;
    }

    /**
     * Setter for endAddress
     *
     * @param endAddress Last address of the chunk.
     */
    void setEndAddress(int endAddress) {
        this.endAddress = endAddress;
    }
}
