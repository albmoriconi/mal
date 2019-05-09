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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<String> elseTargets;
    private boolean invalidIfStatements;

    /**
     * Constructor.
     */
    public TranslatedProgram() {
        instructions = new ArrayList<>();
        allocations = new HashMap<>();
        ifElseTargets = new HashMap<>();
        elseTargets = new HashSet<>();
        invalidIfStatements = false;
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
     * Add if/else target pairs to the map.
     * <p>
     * The map is bidirectional, i.e. uniqueness of both keys and values is preserved.
     */
    public void addIfElseTarget(String ifLabel, String elseLabel) {
        // Preserve bidirectionality of mapping: if entry for if label is already in the map, check
        // that it's paired with the current else label.
        if (!ifElseTargets.containsKey(ifLabel)) {
            ifElseTargets.put(ifLabel, elseLabel);
            ifElseTargets.put(elseLabel, ifLabel);
            elseTargets.add(elseLabel);
        } else if (!ifElseTargets.get(ifLabel).equals(elseLabel)) {
            invalidIfStatements = true;
        }
    }

    /**
     * Checks if the label is in an if/else target label pair.
     *
     * @param label A target label.
     * @return <code>true</code> if, and only if, <code>label</code> is in the map.
     */
    public boolean hasIfElseTarget(String label) {
        return ifElseTargets.containsKey(label);
    }

    /**
     * Return the other element of an if/else target label pair.
     *
     * @param label A target label.
     * @return The other element of the pair, or <code>null</code> if <code>label</code> is not in the map.
     */
    public String getOtherTargetInPair(String label) {
        return ifElseTargets.get(label);
    }

    /**
     * Checks if the label is an else target label.
     *
     * @param label A target label.
     * @return <code>true</code> if, and only if, <code>label</code> is an else target label.
     */
    public boolean isElseTarget(String label) {
        return elseTargets.contains(label);
    }

    /**
     * Checks if the label is an if target label.
     *
     * @param label A target label.
     * @return <code>true</code> if, and only if, <code>label</code> is an if target label.
     */
    public boolean isIfTarget(String label) {
        return hasIfElseTarget(label) && !elseTargets.contains(label);
    }

    /**
     * A program has invalid if statements if target labels are not in a bidirectional mapping.
     *
     * @return <code>true</code> if, and only if, the program has invalid if statements.
     */
    public boolean hasInvalidIfStatements() {
        return invalidIfStatements;
    }
}
