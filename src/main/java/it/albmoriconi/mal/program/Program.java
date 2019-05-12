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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A MAL program.
 * <p>
 * The information on a program that can be determined upon reading a source file, without any
 * additional processing, are:
 * <ul>
 *     <li>The translation of the instructions</li>
 *     <li>The address for placement labels</li>
 *     <li>The instruction count for all labels</li>
 *     <li>The pairs of if/else target labels</li>
 *     <li>The reclaim promises inferred from the placement labels</li>
 *     <li>The block size annotations inferred from other labels</li>
 * </ul>
 */
public class Program {

    private List<Instruction> instructions;
    private Map<String, Integer> addressForLabel;
    private Map<String, Integer> countForLabel;
    private Map<String, String> ifElseTargets;
    private Set<String> elseTargets;
    private Map<Integer, Integer> reclaimPromises;
    private Map<Integer, Integer> blockAnnotations;

    /**
     * Constructor.
     */
    public Program() {
        instructions = new ArrayList<>();
        addressForLabel = new HashMap<>();
        countForLabel = new HashMap<>();
        ifElseTargets = new HashMap<>();
        elseTargets = new HashSet<>();
        reclaimPromises = new HashMap<>();
        blockAnnotations = new HashMap<>();
    }

    /**
     * Getter for instructions.
     *
     * @return The instructions in the program.
     */
    public List<Instruction> getInstructions() {
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
     * @return A map where the key is the label, the value is the instruction count in the source.
     */
    public Map<String, Integer> getCountForLabel() {
        return countForLabel;
    }

    /**
     * Getter for reclaimPromises.
     *
     * @return A map where the key is the starting address of the region to be reclaimed, the value its ending address.
     */
    public Map<Integer, Integer> getReclaimPromises() {
        return reclaimPromises;
    }

    /**
     * Getter for blockAnnotations.
     *
     * @return A map where the key is the instruction count of the label, the value the size of its block.
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
     *
     * @param ifLabel The <code>if</code> clause label.
     * @param elseLabel The <code>else</code> clause label.
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
            throw new IllegalArgumentException("Invalid if statement");
        }
    }

    private boolean labelsAreNewTargets(String ifLabel, String elseLabel) {
        return !ifElseTargets.containsKey(ifLabel) && !ifElseTargets.containsKey(elseLabel);
    }

    private boolean labelsArePairedTogether(String ifLabel, String elseLabel) {
        // Note that this method returns false if both labels are not if/else targets
        return ifElseTargets.containsKey(ifLabel) && ifElseTargets.get(ifLabel).equals(elseLabel);
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
        String otherLabel = ifElseTargets.get(label);

        if (otherLabel == null)
            throw new IllegalArgumentException("Invalid target label");

        return otherLabel;
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
}
