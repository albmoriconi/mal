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

/**
 * Listens to events on the parsed MAL source, producing a translated program.
 * <p>
 * The translator traverses the source only once, so it:
 * <ul>
 *     <li>Only allocates (determines address for) contiguous blocks, i.e. one or more consecutive
 *     instruction, where:
 *     <ul>
 *         <li>The first is labeled with explicit address.</li>
 *         <li>The last contains a control statement.</li>
 *     </ul>
 *     <li>Only sets next address for instructions with goto-mbr-expression statements.</li>
 * </ul>
 * <p>
 * The user can however expect that:
 * <ul>
 *     <li>An entry for every label is in the translated program allocation table.</li>
 *     <li>An entry for every if/else target pair is in the translated if/else bidirectional map.</li>
 *     <li>A reclaim promise is made for every block allocated contiguosly.</li>
 *     <li>A block annotation is made for every block without explicit starting address.</li>
 * </ul>
 * The remaining allocations and next addresses are determined separately (e.g. by a {@link MalAllocator}).
 */
public class MalTranslator extends MalBaseListener {

    private TranslatedInstruction currentInstruction;
    private TranslatedProgram translatedProgram;

    private boolean inContiguousAllocation;
    private int reclaimedBlockStart;
    private int reclaimedBlockEnd;

    private boolean inBlockAnnotation;
    private int blockStartInstruction;
    private int blockSize;

    /**
     * Getter for translatedProgram.
     *
     * @return The translated program based on the parsed MAL source.
     */
    public TranslatedProgram getTranslatedProgram() {
        return translatedProgram;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterUProgram(MalParser.UProgramContext ctx) {
        translatedProgram = new TranslatedProgram();
        inContiguousAllocation = false;
        reclaimedBlockStart = TranslatedInstruction.UNDETERMINED;
        reclaimedBlockEnd = TranslatedInstruction.UNDETERMINED;
        inBlockAnnotation = true; // Start in block annotation for entry point
        blockStartInstruction = 0;
        blockSize = 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void exitUProgram(MalParser.UProgramContext ctx) {
        if (inContiguousAllocation) {
            inContiguousAllocation = false;
            translatedProgram.getReclaimPromises().add(new FreeChunk(reclaimedBlockStart, reclaimedBlockEnd - 1));
        } else if (inBlockAnnotation) {
            inBlockAnnotation = false;
            translatedProgram.getBlockAnnotations().put(blockStartInstruction, blockSize - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterInstruction(MalParser.InstructionContext ctx) {
        currentInstruction = new TranslatedInstruction();

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

        translatedProgram.getInstructions().add(currentInstruction);
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

            // TODO Consider allowing contiguous allocation during block annotation
            // This is tricky, because addresses for the annotated block became determined.
            // For now, just drop it: it's always possible to end the block with a goto the
            // next instruction instead of letting it fall through.
            inBlockAnnotation = false;
        } else {
            // Start a block annotation
            inBlockAnnotation = true;
            blockStartInstruction = translatedProgram.getInstructions().size();
            blockSize = 1;
        }

        if (!translatedProgram.getAddressForLabel().containsKey(currentInstruction.getLabel()))
            translatedProgram.getAddressForLabel().put(currentInstruction.getLabel(), currentInstruction.getAddress());

        if (!translatedProgram.getCountForLabel().containsKey(currentInstruction.getLabel()))
            translatedProgram.getCountForLabel().put(currentInstruction.getLabel(), translatedProgram.getInstructions().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBRegister(MalParser.BRegisterContext ctx) {
        currentInstruction.getInstruction().clear(IBit.B_0.getBitIndex(), IBit.B_3.getBitIndex());

        switch (ctx.getText()) {
            case "MAR":
                break;
            case "PC":
                currentInstruction.getInstruction().set(IBit.B_0.getBitIndex());
                break;
            case "MBR":
                currentInstruction.getInstruction().set(IBit.B_1.getBitIndex());
                break;
            case "MBRU":
                currentInstruction.getInstruction().set(IBit.B_0.getBitIndex());
                currentInstruction.getInstruction().set(IBit.B_1.getBitIndex());
                break;
            case "SP":
                currentInstruction.getInstruction().set(IBit.B_2.getBitIndex());
                break;
            case "LV":
                currentInstruction.getInstruction().set(IBit.B_2.getBitIndex());
                currentInstruction.getInstruction().set(IBit.B_0.getBitIndex());
                break;
            case "CPP":
                currentInstruction.getInstruction().set(IBit.B_2.getBitIndex());
                currentInstruction.getInstruction().set(IBit.B_1.getBitIndex());
                break;
            case "TOS":
                currentInstruction.getInstruction().set(IBit.B_2.getBitIndex());
                currentInstruction.getInstruction().set(IBit.B_1.getBitIndex());
                currentInstruction.getInstruction().set(IBit.B_0.getBitIndex());
                break;
            case "OPC":
                currentInstruction.getInstruction().set(IBit.B_3.getBitIndex());
                break;
            default:
                currentInstruction.getInstruction().set(IBit.B_0.getBitIndex(), IBit.B_3.getBitIndex());
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterCRegister(MalParser.CRegisterContext ctx) {
        switch (ctx.getText()) {
            case "MAR":
                currentInstruction.getInstruction().set(IBit.C_MAR.getBitIndex());
                break;
            case "MDR":
                currentInstruction.getInstruction().set(IBit.C_MDR.getBitIndex());
                break;
            case "PC":
                currentInstruction.getInstruction().set(IBit.C_PC.getBitIndex());
                break;
            case "SP":
                currentInstruction.getInstruction().set(IBit.C_SP.getBitIndex());
                break;
            case "LV":
                currentInstruction.getInstruction().set(IBit.C_LV.getBitIndex());
                break;
            case "CPP":
                currentInstruction.getInstruction().set(IBit.C_CPP.getBitIndex());
                break;
            case "TOS":
                currentInstruction.getInstruction().set(IBit.C_TOS.getBitIndex());
                break;
            case "OPC":
                currentInstruction.getInstruction().set(IBit.C_OPC.getBitIndex());
                break;
            case "H":
                currentInstruction.getInstruction().set(IBit.C_H.getBitIndex());
                break;
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSll8Expression(MalParser.Sll8ExpressionContext ctx) {
        currentInstruction.getInstruction().set((IBit.SLL_8.getBitIndex()));
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSra1Expression(MalParser.Sra1ExpressionContext ctx) {
        currentInstruction.getInstruction().set((IBit.SRA_1.getBitIndex()));
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterAndOperation(MalParser.AndOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterOrOperation(MalParser.OrOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterANotOperation(MalParser.ANotOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INV_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBNotOperation(MalParser.BNotOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSumOperation(MalParser.SumOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterAIncOperation(MalParser.AIncOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBIncOperation(MalParser.BIncOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSubOperation(MalParser.SubOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INV_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterANegOperation(MalParser.ANegOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INV_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBDecOperation(MalParser.BDecOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INV_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterSumIncOperation(MalParser.SumIncOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterAPassOperation(MalParser.APassOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterBPassOperation(MalParser.BPassOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.EN_B.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterNegOneOperation(MalParser.NegOneOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INV_A.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterZeroOperation(MalParser.ZeroOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterOneOperation(MalParser.OneOperationContext ctx) {
        currentInstruction.getInstruction().set(IBit.F_0.getBitIndex());
        currentInstruction.getInstruction().set(IBit.F_1.getBitIndex());
        currentInstruction.getInstruction().set(IBit.INC.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterWordMemoryStatement(MalParser.WordMemoryStatementContext ctx) {
        if (ctx.getText().equals("rd"))
            currentInstruction.getInstruction().set(IBit.READ.getBitIndex());
        else if (ctx.getText().equals("wr"))
            currentInstruction.getInstruction().set(IBit.WRITE.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterByteMemoryStatement(MalParser.ByteMemoryStatementContext ctx) {
        if (ctx.getText().equals("fetch"))
            currentInstruction.getInstruction().set(IBit.FETCH.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterControlStatement(MalParser.ControlStatementContext ctx) {
        if (inContiguousAllocation) {
            inContiguousAllocation = false;
            translatedProgram.getReclaimPromises().add(new FreeChunk(reclaimedBlockStart, reclaimedBlockEnd));
        } else if (inBlockAnnotation) {
            inBlockAnnotation = false;
            translatedProgram.getBlockAnnotations().put(blockStartInstruction, blockSize);
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
        currentInstruction.getInstruction().set(IBit.JMPC.getBitIndex());
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
        currentInstruction.setTargetLabel(ctx.NAME().get(1).getText());
        translatedProgram.addIfElseTarget(ctx.NAME().get(0).getText(), ctx.NAME().get(1).getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterCondition(MalParser.ConditionContext ctx) {
        if (ctx.getText().equals("N"))
            currentInstruction.getInstruction().set(IBit.JAMN.getBitIndex());
        else if (ctx.getText().equals("Z"))
            currentInstruction.getInstruction().set(IBit.JAMZ.getBitIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override public void enterHaltStatement(MalParser.HaltStatementContext ctx) {
        currentInstruction.setIsHalt(true);
    }
}
