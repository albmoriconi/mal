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

package me.albmoriconi.mal.program;

import java.util.BitSet;

/**
 * A MAL instruction.
 * <p>
 * It keeps track of:
 * <ul>
 *     <li>The address it's allocated at (possibly undetermined)</li>
 *     <li>The next address field (possibly undetermined)</li>
 *     <li>The control field</li>
 *     <li>If the instruction is <code>halt</code></li>
 * </ul>
 * If applicable, it also contains:
 * <ul>
 *     <li>The instruction label</li>
 *     <li>The target label</li>
 * </ul>
 */
public class Instruction {

    private int address;
    private int nextAddress;
    private BitSet control;
    private boolean isHalt;
    private String label;
    private String targetLabel;

    /**
     * Conventional value to be used for undetermined addresses.
     */
    public static int UNDETERMINED = -1;

    /**
     * Address fields length in bits
     */
    public static int NEXT_ADDRESS_FIELD_LENGTH = 9;

    /**
     * Control field length in bits.
     */
    public static final int CONTROL_FIELD_LENGTH = 27;

    /**
     * Number of bits in a instruction.
     */
    public static final int INSTRUCTION_LENGTH = NEXT_ADDRESS_FIELD_LENGTH + CONTROL_FIELD_LENGTH;

    /**
     * Constructor.
     */
    public Instruction() {
        this.address = UNDETERMINED;
        this.nextAddress = UNDETERMINED;
        this.control = new BitSet(CONTROL_FIELD_LENGTH);
        this.isHalt = false;
        this.label = "";
        this.targetLabel = "";

        // By default, an instruction doesn't read any register on the B bus
        // Set an unused value (a.t.m. 1001 to 1111 are unused)
        this.control.set(CBit.B_3.getBitIndex());
        this.control.set(CBit.B_0.getBitIndex());
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
     * Getter for the isHalt property.
     *
     * @return <code>true</code> if, and only if, the instruction is an <code>halt</code>.
     */
    public boolean isHalt() {
        return isHalt;
    }

    /**
     * Setter for the isHalt property.
     *
     * @param isHalt The value for isHalt.
     */
    public void setIsHalt(boolean isHalt) {
        this.isHalt = isHalt;
    }

    /**
     * Getter for control.
     *
     * @return The instruction control field.
     */
    public BitSet getControl() {
        return control;
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
     * @return <code>true</code> if, and only if, the address is determined.
     */
    public boolean hasAddress() {
        return address != UNDETERMINED;
    }

    /**
     * Checks if next address field is determined.
     *
     * @return <code>true</code> if, and only if, the next address is determined.
     */
    public boolean hasNextAddress() {
        return nextAddress != UNDETERMINED;
    }

    /**
     * Checks if the instruction has a label.
     *
     * @return <code>true</code> if, and only if, the instruction has label.
     */
    public boolean hasLabel() {
        return !label.isEmpty();
    }

    /**
     * Checks if the instruction specifies a target label.
     *
     * @return <code>true</code> if, and only if, the instruction has a target label.
     */
    public boolean hasTargetLabel() {
        return !targetLabel.isEmpty();
    }

    /**
     * Returns a string object with the binary representation of the instruction.
     *
     * @return A string object with the binary representation of the instruction.
     */
    @Override public String toString() {
        StringBuilder instructionText = new StringBuilder(INSTRUCTION_LENGTH);

        String nextAddressFormat = "%" + NEXT_ADDRESS_FIELD_LENGTH + "s";
        String nextAddress = Integer.toBinaryString(this.nextAddress);

        if (nextAddress.length() > NEXT_ADDRESS_FIELD_LENGTH)
            nextAddress = nextAddress.substring(nextAddress.length() - NEXT_ADDRESS_FIELD_LENGTH);

        instructionText.append(String.format(nextAddressFormat, nextAddress).replace(" ", "0"));

        for (int i = CONTROL_FIELD_LENGTH - 1; i >= 0; i--) {
            if (this.control.get(i))
                instructionText.append("1");
            else
                instructionText.append("0");
        }

        return instructionText.toString();
    }
}
