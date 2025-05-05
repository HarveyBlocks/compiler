package org.harvey.compiler.execute.test.version0;

import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-03 14:46
 */
public abstract class PhasingOperator {

    public abstract OperandCount getOperandCount();

    public abstract int getPriority();


    public abstract String getName();

    public abstract boolean isPre();

    static class Normal extends PhasingOperator {
        private final Operator operator;

        Normal(Operator operator) {
            this.operator = operator;
        }

        @Override
        public OperandCount getOperandCount() {
            return operator.getOperandCount();
        }

        @Override
        public int getPriority() {
            return operator.getPriority();
        }


        @Override
        public String getName() {
            return operator.getName();
        }

        @Override
        public boolean isPre() {
            return Operators.isPre(this.operator);
        }
    }


    static class Call extends PhasingOperator {
        Call() {
        }

        @Override
        public OperandCount getOperandCount() {
            return OperandCount.BINARY;
        }

        @Override
        public int getPriority() {
            return Operator.CALL_PRE.getPriority();
        }


        @Override
        public String getName() {
            return "__compile_operator_call__";
        }

        @Override
        public boolean isPre() {
            return false;
        }
    }


}
