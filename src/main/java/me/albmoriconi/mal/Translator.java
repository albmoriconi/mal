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

package me.albmoriconi.mal;

import me.albmoriconi.mal.antlr.MalBaseListener;
import me.albmoriconi.mal.antlr.MalParser;
import me.albmoriconi.mal.program.CBit;
import me.albmoriconi.mal.program.Instruction;
import me.albmoriconi.mal.program.Program;

import org.antlr.v4.runtime.tree.ErrorNode;

/**
 * Listens to events on the parsed MAL source, producing a translated program.
 * <p>
 * The translator traverses the source only once, so it:
 * <ul>
 *     <li>Only allocates (determines addresses for) for blocks of consecutive instructions where:
 *     <ul>
 *         <li>The first has a placement label.</li>
 *         <li>The last contains a control statement.</li>
 *     </ul>
 *     <li>Only sets next address for instructions with <code>goto-mbr-expression</code> statements.</li>
 * </ul>
 * <p>
 * Appropriate entries in the {@link Program} object are created for:
 * <ul>
 *     <li>Label addresses.</li>
 *     <li>Label instruction count.</li>
 *     <li>If/else target pair.</li>
 *     <li>Reclaim promises inferred from the placement labels.</li>
 *     <li>Block size annotations inferred from other labels.</li>
 * </ul>
 * The remaining allocations and next addresses are determined separately (e.g. by an {@link Allocator}).
 */
public class Translator extends MalBaseListener {

    private Instruction currentInstruction;
    private Program program;

    private boolean inContiguousAllocation;
    private int reclaimedBlockStart;
    private int reclaimedBlockEnd;

    private boolean inBlockAnnotation;
    private int blockStartInstruction;
    private int blockSize;

    private boolean errorState;

    /**
     * Getter for program.
     *
     * @return The translated program based on the parsed MAL source.
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Getter for errorState
     *
     * @return <code>true</code> if, and only if, there are parsing errors.
     */
    public boolean getErrorState() {
        return errorState;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterProgram(MalParser.ProgramContext ctx) {
        program = new Program();
        inContiguousAllocation = false;
        reclaimedBlockStart = Instruction.UNDETERMINED;
        reclaimedBlockEnd = Instruction.UNDETERMINED;
        inBlockAnnotation = true; // Start in block annotation for entry point
        blockStartInstruction = 0;
        blockSize = 1;
        errorState = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void exitProgram(MalParser.ProgramContext ctx) {
        if (inContiguousAllocation) {
            inContiguousAllocation = false;
            program.getReclaimPromises().put(reclaimedBlockStart, reclaimedBlockEnd - 1);
        } else if (inBlockAnnotation) {
            inBlockAnnotation = false;
            program.getBlockAnnotations().put(blockStartInstruction, blockSize - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterInstruction(MalParser.InstructionContext ctx) {
        currentInstruction = new Instruction();

        if (inContiguousAllocation)
            currentInstruction.setAddress(reclaimedBlockEnd);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void exitInstruction(MalParser.InstructionContext ctx) {
        if (inContiguousAllocation)
            currentInstruction.setNextAddress(++reclaimedBlockEnd);
        else if (inBlockAnnotation)
            ++blockSize;

        program.getInstructions().add(currentInstruction);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterLabel(MalParser.LabelContext ctx) {
        currentInstruction.setLabel(ctx.NAME().getText());

        if (ctx.ADDRESS() != null) {
            // Start a contiguous allocation
            currentInstruction.setAddress(Integer.decode(ctx.ADDRESS().getText()));
            inContiguousAllocation = true;
            reclaimedBlockStart = currentInstruction.getAddress();
            reclaimedBlockEnd = reclaimedBlockStart;

            // Consider allowing contiguous allocation during block annotation.
            // This is tricky, because addresses for the annotated block became determined.
            // For now, just drop it: it's always possible to end the block with a goto the
            // next instruction instead of letting it fall through.
            inBlockAnnotation = false; // The block is not annotated and becomes unreachable
        } else {
            // Start a block annotation
            inBlockAnnotation = true;
            blockStartInstruction = program.getInstructions().size();
            blockSize = 1;
        }

        if (!program.getAddressForLabel().containsKey(currentInstruction.getLabel()))
            program.getAddressForLabel().put(currentInstruction.getLabel(), currentInstruction.getAddress());

        if (!program.getCountForLabel().containsKey(currentInstruction.getLabel()))
            program.getCountForLabel().put(currentInstruction.getLabel(), program.getInstructions().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBRegister(MalParser.BRegisterContext ctx) {
        currentInstruction.getControl().clear(CBit.B_0.getBitIndex(), CBit.B_3.getBitIndex());

        switch (ctx.getText()) {
            case "MAR":
                break;
            case "PC":
                currentInstruction.getControl().set(CBit.B_0.getBitIndex());
                break;
            case "MBR":
                currentInstruction.getControl().set(CBit.B_1.getBitIndex());
                break;
            case "MBRU":
                currentInstruction.getControl().set(CBit.B_0.getBitIndex());
                currentInstruction.getControl().set(CBit.B_1.getBitIndex());
                break;
            case "SP":
                currentInstruction.getControl().set(CBit.B_2.getBitIndex());
                break;
            case "LV":
                currentInstruction.getControl().set(CBit.B_2.getBitIndex());
                currentInstruction.getControl().set(CBit.B_0.getBitIndex());
                break;
            case "CPP":
                currentInstruction.getControl().set(CBit.B_2.getBitIndex());
                currentInstruction.getControl().set(CBit.B_1.getBitIndex());
                break;
            case "TOS":
                currentInstruction.getControl().set(CBit.B_2.getBitIndex());
                currentInstruction.getControl().set(CBit.B_1.getBitIndex());
                currentInstruction.getControl().set(CBit.B_0.getBitIndex());
                break;
            case "OPC":
                currentInstruction.getControl().set(CBit.B_3.getBitIndex());
                break;
            default:
                currentInstruction.getControl().set(CBit.B_0.getBitIndex(), CBit.B_3.getBitIndex());
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterCRegister(MalParser.CRegisterContext ctx) {
        switch (ctx.getText()) {
            case "MAR":
                currentInstruction.getControl().set(CBit.C_MAR.getBitIndex());
                break;
            case "MDR":
                currentInstruction.getControl().set(CBit.C_MDR.getBitIndex());
                break;
            case "PC":
                currentInstruction.getControl().set(CBit.C_PC.getBitIndex());
                break;
            case "SP":
                currentInstruction.getControl().set(CBit.C_SP.getBitIndex());
                break;
            case "LV":
                currentInstruction.getControl().set(CBit.C_LV.getBitIndex());
                break;
            case "CPP":
                currentInstruction.getControl().set(CBit.C_CPP.getBitIndex());
                break;
            case "TOS":
                currentInstruction.getControl().set(CBit.C_TOS.getBitIndex());
                break;
            case "OPC":
                currentInstruction.getControl().set(CBit.C_OPC.getBitIndex());
                break;
            case "H":
                currentInstruction.getControl().set(CBit.C_H.getBitIndex());
                break;
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSll8Expression(MalParser.Sll8ExpressionContext ctx) {
        currentInstruction.getControl().set((CBit.SLL_8.getBitIndex()));
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSra1Expression(MalParser.Sra1ExpressionContext ctx) {
        currentInstruction.getControl().set((CBit.SRA_1.getBitIndex()));
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterAndOperation(MalParser.AndOperationContext ctx) {
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterOrOperation(MalParser.OrOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterANotOperation(MalParser.ANotOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.INV_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBNotOperation(MalParser.BNotOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSumOperation(MalParser.SumOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterAIncOperation(MalParser.AIncOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBIncOperation(MalParser.BIncOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
        currentInstruction.getControl().set(CBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSubOperation(MalParser.SubOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
        currentInstruction.getControl().set(CBit.INV_A.getBitIndex());
        currentInstruction.getControl().set(CBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterANegOperation(MalParser.ANegOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.INV_A.getBitIndex());
        currentInstruction.getControl().set(CBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBDecOperation(MalParser.BDecOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
        currentInstruction.getControl().set(CBit.INV_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSumIncOperation(MalParser.SumIncOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
        currentInstruction.getControl().set(CBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterAPassOperation(MalParser.APassOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBPassOperation(MalParser.BPassOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterNegOneOperation(MalParser.NegOneOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.INV_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterZeroOperation(MalParser.ZeroOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterOneOperation(MalParser.OneOperationContext ctx) {
        currentInstruction.getControl().set(CBit.F_0.getBitIndex());
        currentInstruction.getControl().set(CBit.F_1.getBitIndex());
        currentInstruction.getControl().set(CBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterWordMemoryStatement(MalParser.WordMemoryStatementContext ctx) {
        if (ctx.getText().equals("rd"))
            currentInstruction.getControl().set(CBit.READ.getBitIndex());
        else if (ctx.getText().equals("wr"))
            currentInstruction.getControl().set(CBit.WRITE.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterByteMemoryStatement(MalParser.ByteMemoryStatementContext ctx) {
        if (ctx.getText().equals("fetch"))
            currentInstruction.getControl().set(CBit.FETCH.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterControlStatement(MalParser.ControlStatementContext ctx) {
        if (inContiguousAllocation) {
            inContiguousAllocation = false;
            program.getReclaimPromises().put(reclaimedBlockStart, reclaimedBlockEnd);
        } else if (inBlockAnnotation) {
            inBlockAnnotation = false;
            program.getBlockAnnotations().put(blockStartInstruction, blockSize);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterGotoStatement(MalParser.GotoStatementContext ctx) {
        currentInstruction.setTargetLabel(ctx.NAME().getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterGotoMbrExprStatement(MalParser.GotoMbrExprStatementContext ctx) {
        currentInstruction.getControl().set(CBit.JMPC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterMbrExpr(MalParser.MbrExprContext ctx) {
        if (ctx.ADDRESS() != null)
            currentInstruction.setNextAddress(Integer.decode(ctx.ADDRESS().getText()));
        else
            currentInstruction.setNextAddress(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterIfStatement(MalParser.IfStatementContext ctx) {
        if (ctx.NAME().size() == 2) {
            currentInstruction.setTargetLabel(ctx.NAME().get(1).getText());
            program.addIfElseTarget(ctx.NAME().get(0).getText(), ctx.NAME().get(1).getText());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterCondition(MalParser.ConditionContext ctx) {
        if (ctx.getText().equals("N"))
            currentInstruction.getControl().set(CBit.JAMN.getBitIndex());
        else if (ctx.getText().equals("Z"))
            currentInstruction.getControl().set(CBit.JAMZ.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterHaltStatement(MalParser.HaltStatementContext ctx) {
        currentInstruction.setIsHalt(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void visitErrorNode(ErrorNode node) {
        errorState = true;
    }
}
