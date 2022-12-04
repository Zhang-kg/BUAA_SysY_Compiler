package IR.Values;

import IR.Values.InstructionIR.Instruction;
import IR.Values.InstructionIR.InstructionType;
import IR.Values.InstructionIR.PhiInst;
import IR.types.LabelType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions;
    private ArrayList<PhiInst> phiInstructions;
    private Function fatherFunction;
    private Value label = new Value(LabelType.getLabelType(), LabelType.getNewLabelName());
    private static int BASIC_BLOCK_NUM = 0;

    public BasicBlock() {
        super(null, BasicBlock.allocBasicBlockName());
        instructions = new ArrayList<>();
        phiInstructions = new ArrayList<>();
        fatherFunction = null;
    }

    public BasicBlock(Function fatherFunction) {
        super(null, BasicBlock.allocBasicBlockName());
        instructions = new ArrayList<>();
        this.fatherFunction = fatherFunction;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public void addPhiInstruction(PhiInst phiInst) {
        this.phiInstructions.add(phiInst);
    }

    public ArrayList<PhiInst> getPhiInstructions() {
        return phiInstructions;
    }

    public static String allocBasicBlockName() {
        return "BASIC_BLOCK_" + BASIC_BLOCK_NUM++;
    }

//    public void setLabel(Value label) {
//        this.label = label;
//    }

    public Value getLabel() {
        return label;
    }

    public boolean isTerminated() {
        if (instructions.size() == 0) {
            return false;
        }
        InstructionType instructionType = instructions.get(instructions.size() - 1).getInstructionType();
        return instructionType == InstructionType.BR ||
//                instructionType == InstructionType.CALL ||
                instructionType == InstructionType.RET;
    }
}
