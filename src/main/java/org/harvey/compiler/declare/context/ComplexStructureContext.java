package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.control.ExecutableBody;
import org.harvey.compiler.execute.control.ExecutableBodyFactory;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionFactory;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 复合体
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ComplexStructureContext extends DeclaredContext {

    private final List<CallableContext> methodTable = new ArrayList<>();
    private final List<ValueContext> fieldTable = new ArrayList<>();
    private final ArrayList<ExecutableBody> pool = new ArrayList<>();
    private final ExecutableBodyFactory executableBodyFactory = new ExecutableBodyFactory(pool);
    private final List<EnumConstant> enumConstants = new ArrayList<>();
    private final List<TypeAlias> typeAliases = new ArrayList<>();
    private final List<Expression> implementsList = new ArrayList<>();
    private final List<Integer> blocks = new ArrayList<>();
    private final List<Integer> staticBlocks = new ArrayList<>();
    private final List<Integer> innerStructureReferences = new ArrayList<>();
    /**
     * 指向外面(一层), null指外面一层是文件
     */
    private int outerReference;
    private StructureType type;
    /**
     * 不可以为null. 包含`<>`
     */
    private Expression genericMessage;
    private Expression superComplexStructure;
    private int outerStructure;


    private ComplexStructureContext() {
    }

    public void addMethod(CallableContext method) {
        this.methodTable.add(method);
    }

    public void addField(ValueContext field) {
        this.fieldTable.add(field);
    }

    public void addInnerStructure(int references) {
        this.innerStructureReferences.add(references);
    }

    public void addMethod(Collection<CallableContext> method) {
        this.methodTable.addAll(method);
    }

    public void addField(Collection<ValueContext> field) {
        this.fieldTable.addAll(field);
    }


    public void addInnerStructure(Collection<Integer> references) {
        this.innerStructureReferences.addAll(references);
    }

    public void addTypeAlias(Collection<TypeAlias> typeAliases) {
        this.typeAliases.addAll(typeAliases);
    }

    public void addBlocks(List<Integer> blocksReference) {
        this.blocks.addAll(blocksReference);
    }

    public void addStaticBlocks(List<Integer> blocksReference) {
        this.staticBlocks.addAll(blocksReference);
    }

    public void setOuterStructure(int outerStructure) {
        this.outerStructure = outerStructure;
    }

    public Environment getEnvironment() {
        return type.getEnvironment();
    }

    public int executableBodyDepart(SourceTextContext context) {
        return executableBodyFactory.depart(context);
    }

    public void addEnumConstantList(List<EnumConstant> enumConstants) {
        this.enumConstants.addAll(enumConstants);
    }

    @Getter
    @AllArgsConstructor
    public static class EnumConstant {
        private final IdentifierString identifier;
        private final List<Expression> arguments;

        public EnumConstant(EnumConstantDeclarable declarable) {
            this(new IdentifierString(declarable.getName()), declarable.getArgumentList() == null ? null :
                    ExpressionFactory.depart(declarable.getArgumentList()).splitWithComma((ex, sp) -> ex));
        }
    }

    public static class Builder {
        private final ComplexStructureContext product;

        public Builder() {
            product = new ComplexStructureContext();
        }


        public Builder genericMessage(Expression genericMessage) {
            product.genericMessage = genericMessage;
            return this;
        }

        public Builder superComplexStructure(Expression superComplexStructure) {
            product.superComplexStructure = superComplexStructure;
            return this;
        }

        public Builder addInterface(List<Expression> implementsList) {
            product.implementsList.addAll(implementsList);
            return this;
        }


        public Builder identifierReference(int identifierReference) {
            product.identifierReference = identifierReference;
            return this;
        }

        public Builder accessControl(AccessControl accessControl) {
            product.accessControl = accessControl;
            return this;
        }

        public Builder embellish(Embellish embellish) {
            product.embellish = embellish;
            return this;
        }

        public Builder type(StructureType type) {
            product.type = type;
            return this;
        }

        public ComplexStructureContext build(SourcePosition position) {
            assertValid(position);
            if (product.embellish.isMarkedAbstract()) {
                product.type = StructureType.embellishAbstract(product.type);
            }
            return product;
        }

        private void assertValid(SourcePosition position) {
            DeclaredContext.assertNotNull(position, product.accessControl, "accessControl");
            DeclaredContext.assertNotNull(position, product.embellish, "embellish");
            DeclaredContext.assertNotNull(position, product.type, "type");
            DeclaredContext.assertNotNull(position, product.fieldTable, "field table");
            DeclaredContext.assertNotNull(position, product.methodTable, "method table");
            DeclaredContext.assertNotNull(position, product.genericMessage, "generic message");
            if (product.identifierReference < -1) {
                throw new CompilerException("Illegal callable reference: " + product.identifierReference);
            }
        }

    }

    public static class Serializer implements StreamSerializer<ComplexStructureContext> {
        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public ComplexStructureContext in(InputStream is) {
            return null;
        }

        @Override
        public int out(OutputStream os, ComplexStructureContext src) {
            return 0;
        }
    }
}
