package IR.Values.InstructionIR.TerminatorIR;

import IR.Values.BasicBlock;
import IR.Values.Function;
import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.Value;
import IR.types.FunctionType;
import IR.types.Type;
import IR.types.VoidType;

import java.util.ArrayList;

public class CallInst extends TerminatorInst {
    private static int CALL_INST_NUM = 0;
    private ArrayList<Value> arguments;
    private Function function;
    private String argumentsCache = "";
    private boolean hasArgumentCache = false;

    public CallInst(BasicBlock fatherBasicBlock, Function function, ArrayList<Value> arguments) {
        super(fatherBasicBlock, InstructionType.CALL, ((FunctionType)function.getType()).getReturnType(), allocName());
        this.arguments = arguments;
        this.function = function;
    }

    private static String allocName() {
        return "%CALL_INST_NO_" + CALL_INST_NUM++;
    }

    @Override
    public String toString() {
        if (getType() == VoidType.voidType) {
            return "call " + getType() + " " +function.getName() + "(" + getArgumentsCache() +  ")";
        }
        return getName() + " = call " +
                getType() + " " +function.getName() + "(" + getArgumentsCache() +  ")";
    }

    private String getArgumentsCache() {
        if (hasArgumentCache) {
            return argumentsCache;
        }
        hasArgumentCache = true;
        if (arguments == null) {
            argumentsCache = "";
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(arguments.get(i).getType()).append(" ").append(arguments.get(i).getName());
            if (i != arguments.size() - 1) {
                sb.append(", ");
            }
        }
        argumentsCache = sb.toString();
        return argumentsCache;
    }
}
