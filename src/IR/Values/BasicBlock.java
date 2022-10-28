package IR.Values;

import IR.Values.InstructionIR.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions;
    private Function fatherFunction;
    private static int BASIC_BLOCK_NUM = 0;

    public BasicBlock() {
        super(null, BasicBlock.allocBasicBlockName());
        instructions = new ArrayList<>();
        fatherFunction = null;
    }

    public BasicBlock(Function fatherFunction) {
        super(null, BasicBlock.allocBasicBlockName());
        instructions = new ArrayList<>();
        this.fatherFunction = fatherFunction;
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public static String allocBasicBlockName() {
        return "BASIC_BLOCK_" + BASIC_BLOCK_NUM++;
    }
}
