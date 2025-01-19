package org.harvey.compiler.execute.expression;


/**
 * TODO  
 *
 * @date 2025-01-09 00:17
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public interface ComplexExpressionPredicate<T> {
    ArrayInitPredicate ARRAY_INIT = new ArrayInitPredicate();
    StructClonePredicate STRUCT_CLONE = new StructClonePredicate();

    T tryToCast(ComplexExpression expression);

    boolean isPre(T expression);// 不是pre, 就是post

    class ArrayInitPredicate implements ComplexExpressionPredicate<ArrayInitExpression> {
        private ArrayInitPredicate() {
        }

        @Override
        public ArrayInitExpression tryToCast(ComplexExpression expression) {
            return expression instanceof ArrayInitExpression ? (ArrayInitExpression) expression : null;
        }

        @Override
        public boolean isPre(ArrayInitExpression expression) {
            return expression.isStart();
        }
    }

    class StructClonePredicate implements ComplexExpressionPredicate<StructCloneExpression> {
        private StructClonePredicate() {
        }

        @Override
        public StructCloneExpression tryToCast(ComplexExpression expression) {
            return expression instanceof StructCloneExpression ? (StructCloneExpression) expression : null;
        }

        @Override
        public boolean isPre(StructCloneExpression expression) {
            return expression.isStart();
        }
    }
}
