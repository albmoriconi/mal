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

/**
 * Represents a contiguous chunk of memory in the program store.
 */
public class FreeChunk {

    private int startAddress;
    private int endAddress;

    /**
     * Constructor.
     *
     * @param startAddress First address of the chunk.
     * @param endAddress Last address of the chunk.
     */
    public FreeChunk(int startAddress, int endAddress) {
        this.setStartAddress(startAddress);
        this.setEndAddress(endAddress);
    }

    /**
     * Returns the size of the chunk.
     *
     * @return The size of the chunk.
     */
    public int size() {
        return endAddress - startAddress + 1;
    }

    /**
     * Checks if an address belongs to the chunk.
     *
     * @param address An address.
     * @return <code>true</code> if, and only if, <code>address</code> belongs to the chunk.
     */
    public boolean contains(int address) {
        return address >= getStartAddress() && address <= getEndAddress();
    }

    /**
     * Checks if an address interval belongs to the chunk.
     *
     * @param startAddress First address of the interval.
     * @param endAddress Last address of the interval.
     * @return <code>true</code> if, and only if, the interval belongs to the chunk.
     */
    public boolean contains(int startAddress, int endAddress) {
        return contains(startAddress) && contains(endAddress);
    }

    /**
     * Checks if the chunk starts after given address.
     *
     * @param address An address.
     * @return <code>true</code> if, and only if, the chunk starts after <code>address</code>.
     */
    public boolean startsAfter(int address) {
        return getStartAddress() > address;
    }

    /**
     * Checks if the chunk starts before given address.
     *
     * @param address An address.
     * @return <code>true</code> if, and only if, the chunk starts before <code>address</code>.
     */
    public boolean startsBefore(int address) {
        return getStartAddress() < address;
    }

    /**
     * Checks if the chunk is exactly the one delimited by the address pair.
     *
     * @param startAddress The starting address.
     * @param endAddress The ending address.
     * @return <code>true</code> if, and only if, the chunk is the one delimited by the given pair.
     */
    public boolean is(int startAddress, int endAddress) {
        // TODO Add exception handling (startAddress > endAddress)
        return this.getStartAddress() == startAddress && this.getEndAddress() == endAddress;
    }

    /**
     * Checks if the chunk starts at given address.
     *
     * @param address An address.
     * @return <code>true</code> if, and only if, the chunk starts at <code>address</code>.
     */
    public boolean startsAt(int address) {
        return getStartAddress() == address;
    }

    /**
     * Checks if the chunk starts at given address.
     *
     * @param address An address.
     * @return <code>true</code> if, and only if, the chunk starts at <code>address</code>.
     */
    public boolean endsAt(int address) {
        return getStartAddress() == address;
    }

    /**
     * Cuts the chunk part that goes from the start to the given address, included.
     *
     * @param address Last address of the cut area.
     */
    public void cutFromStartTo(int address) {
        // TODO Add exception handling (address not in chunk; chunk is reclaimed entirely)
        setStartAddress(address + 1);
    }

    /**
     * Cuts the chunk part that goes from address, included, to the end.
     *
     * @param address First address of the cut area.
     */
    public void cutFrom(int address) {
        // TODO Add exception handling (address not in chunk; chunk is reclaimed entirely)
        setEndAddress(address - 1);
    }

    /**
     * Getter for startAddress.
     *
     * @return First address of the chunk.
     */
    public int getStartAddress() {
        return startAddress;
    }

    /**
     * Setter for startAddress.
     *
     * @param startAddress First address of the chunk.
     */
    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    /**
     * Getter for endAddress.
     *
     * @return Last address of the chunk.
     */
    public int getEndAddress() {
        return endAddress;
    }

    /**
     * Setter for endAddress
     *
     * @param endAddress Last address of the chunk.
     */
    public void setEndAddress(int endAddress) {
        this.endAddress = endAddress;
    }
}
