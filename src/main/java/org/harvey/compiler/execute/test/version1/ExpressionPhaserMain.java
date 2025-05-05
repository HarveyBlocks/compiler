package org.harvey.compiler.execute.test.version1;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.declare.identifier.DeprecatedIdentifierManager;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.IExpressionElement;
import org.harvey.compiler.execute.test.SourceContextTestCreator;
import org.harvey.compiler.execute.test.version0.ExpressionPhaser0;
import org.harvey.compiler.execute.test.version1.element.CallableInvokeResultSupplier;
import org.harvey.compiler.execute.test.version1.element.ComplexExpressionWrap;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironmentBuilder;
import org.harvey.compiler.execute.test.version1.fake.FakeCallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.fake.FakeMemberManager;
import org.harvey.compiler.execute.test.version1.fake.Int32Type;
import org.harvey.compiler.execute.test.version1.handler.*;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionPhaser;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 21:57
 */
public class ExpressionPhaserMain {

    public static void main(String[] args) {
        ExpressionPhaser phaser = new ExpressionPhaser();
        phaser.register(new ConstantExpressionHandler())
                .register(new ArrayInitHandler())
                // .register(new LambdaExpressionHandler())
                .register(new NewInstanceHandler())
                .register(new ArgumentsListHandler())
                .register(new TypeCastHandler())
                .register(new GetMemberExpressionHandler())
                .register(new IdentifierExpressionHandler())
                .register(new NormalOperatorHandler());
        // 贪心啊,
        // 不是, 一趟式, how to deal ?
        SourceTextContext source = SourceContextTestCreator.newSource(" a = 1 + b - x ( c + d , e + c ) ");
        source = CoreCompiler.registerChain().execute(source);
        Expression phase = phaser.phase(
                source, new OuterEnvironmentBuilder().setIdentifierManager(getIdentifierManager())
                        .setMemberManager(getFakeMemberManager())
                        .build());
        sequential(phase);

        // Expression 这个结构适合上下文分析, 而sequential的结构, 则适合作为序列化, 也就是命令的形式,
        // 而命令的形式, 能保存的信息很少, 所以, 所以不适合上下文分析
        // 如果要在制作expression的时候, 就完成对Sequential的制作, 有一个坏处
        // 如果要在分析Expression就完成对sequential的构建,
        // 坏处, 增加了耦合, 不利于代码的维护
        // 好处, 一趟式
    }

    private static void sequential(Expression phase) {
        Stack<Pair<ListIterator<ExpressionElement>, ExpressionPhaser0>> phases = new Stack<>();
        phases.push(new Pair<>(phase.listIterator(), new ExpressionPhaser0()));
        while (!phases.empty()) {
            Pair<ListIterator<ExpressionElement>, ExpressionPhaser0> peek = phases.peek();
            ListIterator<ExpressionElement> top = peek.getKey();
            ExpressionPhaser0 phaser0 = peek.getValue();
            if (!top.hasNext()) {
                phaser0.end();
                phases.pop();
                continue;
            }
            while (top.hasNext()) {
                ExpressionElement element = top.next();
                if (element instanceof CallableInvokeResultSupplier) {
                    // 一定是item string
                    // 函数名 invoke 参数列表
                    ComplexExpressionWrap[] inner = ((CallableInvokeResultSupplier) element).getArgumentWraps();
                    for (ComplexExpressionWrap wrap : inner) {
                        phases.push(new Pair<>(wrap.getExpression().listIterator(), new ExpressionPhaser0()));
                    }
                    phaser0.addItem((ItemString) element);
                    break;
                } else if (element instanceof ItemString) {
                    phaser0.addItem((ItemString) element);
                } else if (element instanceof OperatorString) {
                    phaser0.addOperator((OperatorString) element);
                } else {
                    throw new UnknownTypeException(IExpressionElement.class, element);
                }
            }

        }
    }

    private static DIdentifierManager getIdentifierManager() {
        return new DeprecatedIdentifierManager(new HashMap<>(), new ArrayList<>(), 0);
    }

    private static FakeMemberManager getFakeMemberManager() {
        Map<String, MemberType> memberSupplierMap = new HashMap<>();
        Map<String, CallableRelatedDeclare[]> possibleCallableSupplierHashMap = new HashMap<>();
        memberSupplierMap.put("a", Int32Type.INT32);
        memberSupplierMap.put("b", Int32Type.INT32);
        memberSupplierMap.put("c", Int32Type.INT32);
        memberSupplierMap.put("d", Int32Type.INT32);
        memberSupplierMap.put("e", Int32Type.INT32);
        possibleCallableSupplierHashMap.put("x", new CallableRelatedDeclare[]{new FakeCallableRelatedDeclare()});
        return new FakeMemberManager(memberSupplierMap, possibleCallableSupplierHashMap);
    }
}
