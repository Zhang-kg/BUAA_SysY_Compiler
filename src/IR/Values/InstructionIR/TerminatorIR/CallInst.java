package IR.Values.InstructionIR.TerminatorIR;

import IR.Values.BasicBlock;
import IR.Values.Function;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.Value;
import IR.types.FunctionType;
import IR.types.Type;

import java.util.ArrayList;

public class CallInst extends TerminatorInst {
    private static int CALL_INST_NUM = 0;
    private ArrayList<Value> arguments;

    public CallInst(BasicBlock fatherBasicBlock, Function function, ArrayList<Value> arguments) {
        super(fatherBasicBlock, InstructionType.CALL, ((FunctionType)function.getType()).getReturnType(), allocName());
        this.arguments = arguments;
    }

    private static String allocName() {
        return "CALL_INST_NO_" + CALL_INST_NUM++;
    }
}
