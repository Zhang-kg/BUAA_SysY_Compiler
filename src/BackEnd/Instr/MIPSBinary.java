package BackEnd.Instr;

import BackEnd.MIPSInstruction;
import BackEnd.Optimize.DivOptimizePair;
import BackEnd.RegAllocation;
import BackEnd.SymbolTableForMIPS.SymbolTypeForMIPS;
import BackEnd.VirtualReg;

import java.math.BigInteger;
import java.util.HashMap;

public class MIPSBinary extends MIPSInstruction {
    public MIPSBinary(InstructionType instructionType, VirtualReg rd, VirtualReg rs, VirtualReg rt) {
        super(instructionType);
        this.setRd(rd);
        this.setRs(rs);
        this.setRt(rt);
    }

    public MIPSBinary(InstructionType instructionType, VirtualReg rd, VirtualReg rs, int imm) {
        super(instructionType);
        this.setRd(rd);
        this.setRs(rs);
        this.setImm(imm);
    }

    public String genMIPSFromMIPSInst(HashMap<String, VirtualReg> globalStringVirtualRegHashMap,
                                      RegAllocation regAllocation) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbBack = new StringBuilder();
        VirtualReg rd = getRd();
        VirtualReg rs = getRs();
        String rdPhiRegName = "";
        String rsPhiRegName = "";
        if (rd.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rdPhiRegName = regAllocation.getVirToPhi().get(rd.getName());
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            // * 这里不仅要取出，而且最后要放回去，使用sbBack表示最后进行的操作
            int offsetRd = rd.getStackOffset();
//            sb.append("lw $t1, " + offsetRd + "($sp)\n");
            sbBack.append("sw $t1, " + offsetRd + "($sp)\n");   // * 最后进行的操作
            rdPhiRegName = "$t1";
        } else if (rd.getSymbolType() == SymbolTypeForMIPS.GlobalVariable) {
            sb.append("la $t1, " + rd.getName() + "\n");
            rsPhiRegName = "$t1";
        } else {    // ! wrong
            System.out.println("WRONG: BINARY RD NOT A PHYSICAL REG OR A SPILL REG OR GLOBAL VARIABLE");
            return "";
        }

        if (rs.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
            rsPhiRegName = regAllocation.getVirToPhi().get(rs.getName());
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
            int offsetRs = rs.getStackOffset();
            sb.append("lw $t2, " + offsetRs + "($sp)\n");
            sbBack.append("sw $t2, " + offsetRs + "($sp)\n");
            rsPhiRegName = "$t2";
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.StackReg) {
            int offsetRs = rs.getStackOffset();
            sb.append("addi $t2, $sp, " + offsetRs + "\n");
            rsPhiRegName = "$t2";
        } else if (rs.getSymbolType() == SymbolTypeForMIPS.GlobalVariable) {
            sb.append("la $t2, " + rs.getName() + "\n");
            rsPhiRegName = "$t2";
        } else {    // ! wrong
            System.out.println("WRONG: BINARY RS NOT A PHYSICAL REG, A SPILL REG OR GLOBAL VARIABLE");
            System.out.println(rs.getSymbolType());
        }
        if (isHasImm()) {
            int imm = getImm();
            if (getInstructionType() == InstructionType.srem) {
                sb.append("div " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
//                sb.append("divu " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
//                sb.append("divu " + rsPhiRegName + ", " + imm + "\n");
                sb.append("mfhi " + rdPhiRegName + "\n");
            } else {
                if (getInstructionType() == InstructionType.mul) {
                    // 乘法优化
                    int d = Math.abs(imm);
                    if (d == 0) {   // 如果是乘以 0 则直接优化
                        sb.append("li " + rdPhiRegName + ", 0\n");
                    } else {
                        if (check(d)) { // 如果 d 是 2 的幂次
                            int n = getPower(d);
                            sb.append("sll " + rdPhiRegName + ", " + rsPhiRegName + ", " + n + "\n");
                            if (imm < 0) {
                                sb.append("subu " + rdPhiRegName + ", $0, " + rdPhiRegName + "\n");
                            }
                        } else if (check(d - 1)) {  // 如果 d 是 2 的幂次 + 1，则可用移位指令和加法指令
                            int n = getPower(d - 1);
                            sb.append("sll $t3, " + rsPhiRegName + ", " + n + "\n");
                            sb.append("addu " + rdPhiRegName + ", $t3, " + rsPhiRegName + "\n");
                            if (imm < 0) {
                                sb.append("subu " + rdPhiRegName + ", $0, " + rdPhiRegName + "\n");
                            }
                        } else if (check(d + 1)) {  // 如果 d 是 2 的幂次 - 1，则可用移位指令和减法指令
                            int n = getPower(d + 1);
                            sb.append("sll $t3, " + rsPhiRegName + ", " + n + "\n");
                            sb.append("subu " + rdPhiRegName + ", $t3, " + rsPhiRegName + "\n");
                            if (imm < 0) {
                                sb.append("subu " + rdPhiRegName + ", $0, " + rdPhiRegName + "\n");
                            }
                        } else {    // 无法优化
                            sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
                        }
                    }
                } else if (getInstructionType() == InstructionType.div) {
                    long d = Math.abs(imm);
                    DivOptimizePair divOptimizePair = chooseMultiplier(new BigInteger(String.valueOf(d)), 31);
                    BigInteger m = divOptimizePair.getM();
                    int sh_post = divOptimizePair.getSh_post();
                    int l = divOptimizePair.getL();
                    if (d == 1) {
                        sb.append("addu " + rdPhiRegName + ", $0, " + rsPhiRegName + "\n");
                    } else if (d == (1L << l)) {
                        sb.append("sra $t3, " + rsPhiRegName + ", " + (l - 1) + "\n");
                        sb.append("srl $t3, $t3, " + (32 - l) + "\n");
                        sb.append("addu $t3, $t3, " + rsPhiRegName + "\n");
                        sb.append("sra " + rdPhiRegName + ", $t3, " + l + "\n");
                    } else if (m.compareTo(new BigInteger(Long.toString(1L << 31))) < 0) {
                        String v1 = rdPhiRegName;
                        if (rdPhiRegName.equals(rsPhiRegName)) {
                            v1 = "$t2";
                        }
                        sb.append("slt " + v1 + ", " + rsPhiRegName + ", " + "$0\n");
                        sb.append("mul $t3, " + rsPhiRegName + ", " + m + "\n");
                        sb.append("mfhi $t3\n");
                        sb.append("sra $t3, $t3, " + sh_post + "\n");
                        sb.append("add " + rdPhiRegName + ", $t3, " + v1 + "\n");
                    } else {
                        String v1 = rdPhiRegName;
                        if (rdPhiRegName.equals(rsPhiRegName)) {
                            v1 = "$t2";
                        }
                        sb.append("slt " + v1 + ", " + rsPhiRegName + ", $0\n");
                        sb.append("mul $t3, " + rsPhiRegName + ", " + m.subtract(BigInteger.ONE.shiftLeft(32)) + "\n");
                        sb.append("mfhi $t3\n");
                        sb.append("add " + "$t3" + ", " + "$t3" + ", " + rsPhiRegName + "\n");
                        sb.append("sra " + "$t3" + ", " + "$t3" + ", " + sh_post + "\n");
                        sb.append("add " + rdPhiRegName + ", " + "$t3" + ", " + v1 + "\n");
                    }
                    if (imm < 0) {
                        sb.append("sub " + rdPhiRegName + ", $0, " + rdPhiRegName + "\n");
                    }
                } else {
                    sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + imm + "\n");
                }

            }

            return sb.toString() + sbBack.toString();
        }
        if (isHasRt()) {
            VirtualReg rt = getRt();
            String rtPhiRegName = "";
            if (rt.getSymbolType() == SymbolTypeForMIPS.PhysicsReg) {
                rtPhiRegName = regAllocation.getVirToPhi().get(rt.getName());
            } else if (rt.getSymbolType() == SymbolTypeForMIPS.SpillReg) {
                int offsetRt = rt.getStackOffset();
                sb.append("lw $t3, " + offsetRt + "($sp)\n");
                sbBack.append("sw $t3, " + offsetRt + "($sp)\n");
                rtPhiRegName = "$t3";
            } else {    // ! wrong
                System.out.println("WRONG: BINARY RT NOT A PHYSICAL REG OR A SPILL REG");
            }
            if (getInstructionType() == InstructionType.srem) {
                sb.append("div " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
//                sb.append("divu " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
//                sb.append("divu " +  rsPhiRegName + ", " + rtPhiRegName + "\n");
                sb.append("mfhi " + rdPhiRegName + "\n");
            } else {
                sb.append(getInstructionType().toString() + " " + rdPhiRegName + ", " + rsPhiRegName + ", " + rtPhiRegName + "\n");
            }
            return sb.toString() + sbBack.toString();
        }
        // ! Wrong
        return "WRONG BINARY DONT HAVE IMM OR RT\n";
    }

    private boolean check(int x) {
        return (x > 0) && ((x & (x - 1)) == 0);
    }

    private int getPower(int x) {
        int ans = 0;
        while (x != 1) {
            x /= 2;
            ans++;
        }
        return ans;
    }

    private int muluh(int a, int b) {
        BigInteger bigA = new BigInteger(Integer.toUnsignedString(a));
        BigInteger bigB = new BigInteger(Integer.toUnsignedString(b));
        return Integer.parseInt(bigA.multiply(bigB).shiftRight(32).toString());
    }

    private int mulsh(int a, int b) {
        BigInteger bigA = new BigInteger(String.valueOf(a));
        BigInteger bigB = new BigInteger(String.valueOf(b));
        return Integer.parseInt(bigA.multiply(bigB).shiftRight(32).toString());
    }

    private DivOptimizePair chooseMultiplier(BigInteger d, int p) {
        assert d.compareTo(BigInteger.ZERO) > 0;
        int l = d.subtract(BigInteger.ONE).bitLength();
        int sh = l;
        BigInteger low = ((BigInteger.ONE).shiftLeft(32 + l)).divide(d);
        BigInteger high = BigInteger.ONE.shiftLeft(32 + l);
        high = (high.add(BigInteger.ONE.shiftLeft(32 + l - p))).divide(d);
        while (low.shiftRight(1).compareTo(high.shiftRight(1)) < 0 && sh > 0) {
            low = low.shiftRight(1);
            high = high.shiftRight(1);
            sh--;
        }
        return new DivOptimizePair(high, sh, l);
    }

    private int ctz(int n) {
        int ans = 0;
        while (n != 0) {
            n >>>= 1;
            ans ++;
        }
        return ans;
    }



}
