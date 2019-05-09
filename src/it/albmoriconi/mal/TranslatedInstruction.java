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

import java.util.BitSet;

/**
 * Represents a MAL microinstruction.
 * <p>
 * It keeps track of:
 * <ul>
 *     <li>The address it's allocated at</li>
 *     <li>The next instruction address</li>
 *     <li>The control fields</li>
 * </ul>
 * If applicable, it also contains:
 * <ul>
 *     <li>The instruction label</li>
 *     <li>The next instruction label</li>
 * </ul>
 */
public class TranslatedInstruction {

    private int address;
    private int nextAddress;
    private BitSet instruction;
    private String label;
    private String targetLabel;

    /**
     * Conventional value to be used for undetermined addresses.
     */
    public static int UNDETERMINED = -1;

    /**
     * Constructor.
     */
    public TranslatedInstruction() {
        this.address = UNDETERMINED;
        this.nextAddress = UNDETERMINED;
        this.label = "";
        this.targetLabel = "";
        this.instruction = new BitSet(IBit.BIT_NUMBER);

        // By default, an instruction doesn't read any register on the B bus
        this.instruction.set(IBit.B_0.getBitIndex(), IBit.B_3.getBitIndex());
    }

    /**
     * Getter for address.
     *
     * @return The address where the instruction is to be allocated.
     */
    public int getAddress() {
        return address;
    }

    /**
     * Setter for address.
     *
     * @param address The address where the instruction is to be allocated.
     */
    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Getter for nextAddress.
     *
     * @return The next instruction field of the instruction.
     */
    public int getNextAddress() {
        return nextAddress;
    }

    /**
     * Setter for nextAddress.
     *
     * @param nextAddress The next instruction field of the instruction.
     */
    public void setNextAddress(int nextAddress) {
        this.nextAddress = nextAddress;
    }


    /**
     * Getter for instruction.
     *
     * @return The instruction control fields.
     */
    public BitSet getInstruction() {
        return instruction;
    }

    /**
     * Setter for instruction.
     *
     * @param instruction The instruction control fields.
     */
    public void setInstruction(BitSet instruction) {
        this.instruction = instruction;
    }

    /**
     * Getter for label.
     *
     * @return The instruction label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter for label.
     *
     * @param label The instruction label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Getter for targetLabel.
     *
     * @return The target instruction label.
     */
    public String getTargetLabel() {
        return targetLabel;
    }

    /**
     * Setter for targetLabel.
     *
     * @param targetLabel The target instruction label.
     */
    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    /**
     * Checks if the instruction is allocated.
     *
     * @return <code>true</code> if the address is determined, <code>false</code> otherwise.
     */
    public boolean hasAddress() {
        return address != UNDETERMINED;
    }

    /**
     * Checks if next address field is determined.
     *
     * @return <code>true</code> if next address is determined, <code>false</code> otherwise.
     */
    public boolean hasNextAddress() {
        return nextAddress != UNDETERMINED;
    }

    /**
     * Checks if the instruction has a label.
     *
     * @return <code>true</code> if instruction has label, <code>false</code> otherwise.
     */
    public boolean hasLabel() {
        return !label.isEmpty();
    }

    /**
     * Checks if the instruction specifies a label for the next instruction.
     *
     * @return <code>true</code> if instruction has label for target, <code>false</code> otherwise.
     */
    public boolean hasTargetLabel() {
        return !targetLabel.isEmpty();
    }

    /**
     * Checks if the instruction successor is already specified, in the form of an address or a label.
     *
     * @return <code>true</code> if instruction has determined address or label for target.
     */
    public boolean hasSuccessor() {
        return hasNextAddress() || hasTargetLabel();
    }
}
