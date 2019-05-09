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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains the translated microprogram.
 * <p>
 * The information on a microprogram that can be determined upon reading a source file, without any
 * additional processing, are:
 * <ul>
 *     <li>The translation of the instructions</li>
 *     <li>The allocation of instructions with labels and specified addresses</li>
 *     <li>The pairs of if/else target labels</li>
 * </ul>
 */
public class TranslatedProgram {

    private List<TranslatedInstruction> instructions;
    private Map<String, Integer> allocations;
    private Map<String, String> ifElseTargets;

    /**
     * Constructor.
     */
    public TranslatedProgram() {
        instructions = new LinkedList<>();
        allocations = new HashMap<>();
        ifElseTargets = new HashMap<>();
    }

    /**
     * Getter for instructions.
     *
     * @return The translation of the instructions in the microprogram.
     */
    public List<TranslatedInstruction> getInstructions() {
        return instructions;
    }

    /**
     * Getter for allocations.
     *
     * @return The allocation of instructions with labels and specified addresses.
     *         The key is the label, the value is the address.
     */
    public Map<String, Integer> getAllocations() {
        return allocations;
    }

    /**
     * Getter for ifElseTargets.
     *
     * @return The pairs of if/else target labels.
     *         The key is the else target label, the value is the if target label.
     */
    public Map<String, String> getIfElseTargets() {
        return ifElseTargets;
    }
}
