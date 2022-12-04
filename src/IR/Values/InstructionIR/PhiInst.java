package IR.Values.InstructionIR;

import IR.Values.BasicBlock;
import IR.Values.Value;
import IR.types.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PhiInst extends Instruction {
    private String originVariableName;
//    private HashMap<Value, BasicBlock> phiOperands = new HashMap<>();
    private ArrayList<BasicBlock> bbList = new ArrayList<>();

    public PhiInst(BasicBlock fatherBasicBlock, Type type, String name) {
        super(fatherBasicBlock, InstructionType.PHI, type, name);
        fatherBasicBlock.addPhiInstruction(this);
        this.originVariableName = name;
    }

    public String getOriginVariableName() {
        return originVariableName;
    }

    public void addPhiOperand(Value value, BasicBlock basicBlock) {
        addOperand(value);
        bbList.add(basicBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        Iterator<Map.Entry<Value, BasicBlock>> iterator = phiOperands.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<Value, BasicBlock> entry = iterator.next();
//            sb.append("[" + entry.getKey().getName() + ", " + entry.getValue().getLabel().getName() + "]");
//            if (iterator.hasNext()) sb.append(", ");
//        }
        for (int i = 0; i < getOperands().size(); i++) {
            sb.append("[" + getOperands().get(i).getName() + ", " + bbList.get(i).getLabel().getName() + "]");
            if (i != getOperands().size() - 1) {
                sb.append(", ");
            }
        }
        return this.getName() + " = phi " + this.getType() + " " + sb;
    }
}
