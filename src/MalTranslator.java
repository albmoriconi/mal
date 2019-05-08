import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MalTranslator extends MalBaseListener {
    public class TranslatedInstruction {
        public String address;
        public String nextAddress;
        public BitSet instruction;
    }

    private String currentAddress = "";
    private String currentNextAddress = "Incr";
    private BitSet currentInstruction = new BitSet(27);
    public List<TranslatedInstruction> translatedProgram= new ArrayList<>();

    private enum MIBits {
        B_0,
        B_1,
        B_2,
        B_3,
        FETCH,
        READ,
        WRITE,
        C_MAR,
        C_MDR,
        C_PC,
        C_SP,
        C_LV,
        C_CPP,
        C_TOS,
        C_OPC,
        C_H,
        INC,
        INV_A,
        EN_B,
        EN_A,
        F_1,
        F_0,
        SRA_1,
        SLL_8,
        JAMZ,
        JAMN,
        JMPC
    }

    @Override public void enterInstruction(MalParser.InstructionContext ctx) {
        currentInstruction.set(MIBits.B_0.ordinal(), MIBits.B_3.ordinal());
    }

    @Override public void exitInstruction(MalParser.InstructionContext ctx) {
        TranslatedInstruction ti = new TranslatedInstruction();
        ti.address = currentAddress;
        ti.nextAddress = currentNextAddress;
        ti.instruction = currentInstruction;
        translatedProgram.add(ti);

        currentAddress = "";
        currentNextAddress = "Incr";
        currentInstruction = new BitSet(27);
    }

    @Override public void enterLabel(MalParser.LabelContext ctx) {
        if (ctx.ADDRESS() != null)
            currentAddress = ctx.ADDRESS().getText();
    }

    @Override public void enterBRegister(MalParser.BRegisterContext ctx) {
        currentInstruction.clear(MIBits.B_0.ordinal(), MIBits.B_3.ordinal());

        switch (ctx.getText()) {
            case "MAR":
                break;
            case "PC":
                currentInstruction.set(MIBits.B_0.ordinal());
                break;
            case "MBR":
                currentInstruction.set(MIBits.B_1.ordinal());
                break;
            case "MBRU":
                currentInstruction.set(MIBits.B_0.ordinal());
                currentInstruction.set(MIBits.B_1.ordinal());
                break;
            case "SP":
                currentInstruction.set(MIBits.B_2.ordinal());
                break;
            case "LV":
                currentInstruction.set(MIBits.B_2.ordinal());
                currentInstruction.set(MIBits.B_0.ordinal());
                break;
            case "CPP":
                currentInstruction.set(MIBits.B_2.ordinal());
                currentInstruction.set(MIBits.B_1.ordinal());
                break;
            case "TOS":
                currentInstruction.set(MIBits.B_2.ordinal());
                currentInstruction.set(MIBits.B_1.ordinal());
                currentInstruction.set(MIBits.B_0.ordinal());
                break;
            case "OPC":
                currentInstruction.set(MIBits.B_3.ordinal());
                break;
            default:
                currentInstruction.set(MIBits.B_0.ordinal(), MIBits.B_3.ordinal());
                break;
        }
    }

    @Override public void enterCRegister(MalParser.CRegisterContext ctx) {
        switch (ctx.getText()) {
            case "MAR":
                currentInstruction.set(MIBits.C_MAR.ordinal());
                break;
            case "MDR":
                currentInstruction.set(MIBits.C_MDR.ordinal());
                break;
            case "PC":
                currentInstruction.set(MIBits.C_PC.ordinal());
                break;
            case "SP":
                currentInstruction.set(MIBits.C_SP.ordinal());
                break;
            case "LV":
                currentInstruction.set(MIBits.C_LV.ordinal());
                break;
            case "CPP":
                currentInstruction.set(MIBits.C_CPP.ordinal());
                break;
            case "TOS":
                currentInstruction.set(MIBits.C_TOS.ordinal());
                break;
            case "OPC":
                currentInstruction.set(MIBits.C_OPC.ordinal());
                break;
            case "H":
                currentInstruction.set(MIBits.C_H.ordinal());
                break;
            default:
                break;
        }
    }

    @Override public void enterSll8Expression(MalParser.Sll8ExpressionContext ctx) {
        currentInstruction.set((MIBits.SLL_8.ordinal()));
    }

    @Override public void enterSra1Expression(MalParser.Sra1ExpressionContext ctx) {
        currentInstruction.set((MIBits.SRA_1.ordinal()));
    }

    @Override public void enterAndOperation(MalParser.AndOperationContext ctx) {
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
    }

    @Override public void enterOrOperation(MalParser.OrOperationContext ctx) {
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
    }

    @Override public void enterANotOperation(MalParser.ANotOperationContext ctx) {
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.INV_A.ordinal());
    }

    @Override public void enterBNotOperation(MalParser.BNotOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
    }

    @Override public void enterSumOperation(MalParser.SumOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
    }

    @Override public void enterAIncOperation(MalParser.AIncOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.INC.ordinal());
    }

    @Override public void enterBIncOperation(MalParser.BIncOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
        currentInstruction.set(MIBits.INC.ordinal());
    }

    @Override public void enterSubOperation(MalParser.SubOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
        currentInstruction.set(MIBits.INV_A.ordinal());
        currentInstruction.set(MIBits.INC.ordinal());
    }

    @Override public void enterANegOperation(MalParser.ANegOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.INV_A.ordinal());
        currentInstruction.set(MIBits.INC.ordinal());
    }

    @Override public void enterBDecOperation(MalParser.BDecOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
        currentInstruction.set(MIBits.INV_A.ordinal());
    }

    @Override public void enterSumIncOperation(MalParser.SumIncOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
        currentInstruction.set(MIBits.INC.ordinal());
    }

    @Override public void enterAPassOperation(MalParser.APassOperationContext ctx) {
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_A.ordinal());
    }

    @Override public void enterBPassOperation(MalParser.BPassOperationContext ctx) {
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.EN_B.ordinal());
    }

    @Override public void enterNegOneOperation(MalParser.NegOneOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.INV_A.ordinal());
    }

    @Override public void enterZeroOperation(MalParser.ZeroOperationContext ctx) {
        currentInstruction.set(MIBits.F_1.ordinal());
    }

    @Override public void enterOneOperation(MalParser.OneOperationContext ctx) {
        currentInstruction.set(MIBits.F_0.ordinal());
        currentInstruction.set(MIBits.F_1.ordinal());
        currentInstruction.set(MIBits.INC.ordinal());
    }

    @Override public void enterWordMemoryStatement(MalParser.WordMemoryStatementContext ctx) {
        if (ctx.getText().equals("rd"))
            currentInstruction.set(MIBits.READ.ordinal());
        else if (ctx.getText().equals("wr"))
            currentInstruction.set(MIBits.WRITE.ordinal());
    }

    @Override public void enterByteMemoryStatement(MalParser.ByteMemoryStatementContext ctx) {
        if (ctx.getText().equals("fetch"))
            currentInstruction.set(MIBits.FETCH.ordinal());
    }

    @Override public void enterGotoStatement(MalParser.GotoStatementContext ctx) {
        currentNextAddress = ctx.NAME().getText();
    }

    @Override public void enterGotoMbrExprStatement(MalParser.GotoMbrExprStatementContext ctx) {
        currentInstruction.set(MIBits.JMPC.ordinal());
    }

    @Override public void enterMbrExpr(MalParser.MbrExprContext ctx) {
        if (ctx.ADDRESS() != null)
            currentNextAddress = ctx.ADDRESS().getText();
        else
            currentNextAddress = "0x0";
    }

    @Override public void enterIfStatement(MalParser.IfStatementContext ctx) {
        currentNextAddress = ctx.NAME().get(1).getText();
    }

    @Override public void enterCondition(MalParser.ConditionContext ctx) {
        if (ctx.getText().equals("N"))
            currentInstruction.set(MIBits.JAMN.ordinal());
        else if (ctx.getText().equals("Z"))
            currentInstruction.set(MIBits.JAMZ.ordinal());
    }
}
