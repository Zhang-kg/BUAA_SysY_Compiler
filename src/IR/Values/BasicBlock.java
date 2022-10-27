package IR.Values;

import IR.Values.InstructionIR.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions;
    private Function fatherFunction;

    public BasicBlock() {
        super(null, null);
        instructions = new ArrayList<>();
        fatherFunction = null;
    }

    public BasicBlock(Function fatherFunction) {
        super(null, null);
        instructions = new ArrayList<>();
        this.fatherFunction = fatherFunction;
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }
}
