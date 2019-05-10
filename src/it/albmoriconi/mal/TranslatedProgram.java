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

import java.util.*;

/**
 * Contains the translated microprogram.
 * <p>
 * The information on a microprogram that can be determined upon reading a source file, without any
 * additional processing, are:
 * <ul>
 *     <li>The translation of the instructions</li>
 *     <li>The allocation of instructions with addressForLabel and specified addresses</li>
 *     <li>The pairs of if/else target addressForLabel</li>
 * </ul>
 */
public class TranslatedProgram {

    // TODO Add methods for puts/gets/etc. instead of field accessors?
    private List<TranslatedInstruction> instructions;
    private Map<String, Integer> addressForLabel;
    private Map<String, Integer> countForLabel;
    private Map<String, String> ifElseTargets;
    private Set<String> elseTargets;
    private boolean invalidIfStatements;
    private List<FreeChunk> reclaimPromises;
    private Map<Integer, Integer> blockAnnotations;

    /**
     * Constructor.
     */
    public TranslatedProgram() {
        instructions = new ArrayList<>();
        addressForLabel = new HashMap<>();
        countForLabel = new HashMap<>();
        ifElseTargets = new HashMap<>();
        elseTargets = new HashSet<>();
        invalidIfStatements = false;
        reclaimPromises = new LinkedList<>();
        blockAnnotations = new HashMap<>();
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
     * Getter for addressForLabel.
     *
     * @return A map where the key is the label, the value is the address (can be undetermined).
     */
    public Map<String, Integer> getAddressForLabel() {
        return addressForLabel;
    }

    /**
     * Getter for countForLabel.
     *
     * @return A map where the key is the label, the value is the count in the source.
     */
    public Map<String, Integer> getCountForLabel() {
        return countForLabel;
    }

    /**
     * Getter for reclaimPromises
     *
     * @return A list of chunks that have to be reclaimed by the allocator. Is valid if source is valid.
     */
    public List<FreeChunk> getReclaimPromises() {
        return reclaimPromises;
    }

    /**
     * Getter for blockAnnotations
     *
     * @return A map where the key is the instruction count (relative to source) and the value the size of block.
     */
    public Map<Integer, Integer> getBlockAnnotations() {
        return blockAnnotations;
    }

    /**
     * Add if/else target pairs to the map.
     * <p>
     * The map is bidirectional, i.e. uniqueness of both keys and values is preserved.
     * This means that statement <code>if (cond) goto l1; else goto l2;</code> in the source enforces
     * that <code>l1</code> is never used as an else target, <code>l2</code> is never used as an if
     * target, and every time they're used as if/else targets they're paired together.
     */
    public void addIfElseTarget(String ifLabel, String elseLabel) {
        // Preserve bidirectionality of mapping
        if (labelsAreNewTargets(ifLabel, elseLabel)) {
            // If both labels appear as if/else targets for the first time, create entries
            ifElseTargets.put(ifLabel, elseLabel);
            ifElseTargets.put(elseLabel, ifLabel);
            elseTargets.add(elseLabel);
        } else if (!labelsArePairedTogether(ifLabel, elseLabel)) {
            // If one (or both) are already in the map, but they're not paired together, source is invalid
            invalidIfStatements = true;
        }
    }

    private boolean labelsArePairedTogether(String ifLabel, String elseLabel) {
        // Note that this method returns false if both labels are not if/else targets
        return ifElseTargets.containsKey(ifLabel) && ifElseTargets.get(ifLabel).equals(elseLabel);
    }

    private boolean labelsAreNewTargets(String ifLabel, String elseLabel) {
        return !ifElseTargets.containsKey(ifLabel) && !ifElseTargets.containsKey(elseLabel);
    }

    /**
     * Checks if the label is an if or else target label.
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
