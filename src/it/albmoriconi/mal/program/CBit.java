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

package it.albmoriconi.mal.program;

/**
 * Enumerates the control bits in a microinstruction.
 */
public enum CBit {
    B_0(0),
    B_1(1),
    B_2(2),
    B_3(3),
    FETCH(4),
    READ(5),
    WRITE(6),
    C_MAR(7),
    C_MDR(8),
    C_PC(9),
    C_SP(10),
    C_LV(11),
    C_CPP(12),
    C_TOS(13),
    C_OPC(14),
    C_H(15),
    INC(16),
    INV_A(17),
    EN_B(18),
    EN_A(19),
    F_1(20),
    F_0(21),
    SRA_1(22),
    SLL_8(23),
    JAMZ(24),
    JAMN(25),
    JMPC(26);

    private final int bitIndex;

    /**
     * Constructor.
     *
     * @param bitIndex The index of the bit in the instruction.
     */
    CBit(int bitIndex) {
        this.bitIndex = bitIndex;
    }

    /**
     * Getter for bitIndex.
     *
     * @return The index of the bit in the instruction.
     */
    public int getBitIndex() {
        return bitIndex;
    }
}
