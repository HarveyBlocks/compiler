package org.harvey.compiler.io.cache.node;

import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.cache.ImportType;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.File;

/**
 * 文件结构, 引用, identifier path的映射节点
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 20:40
 */
public interface FileNode {
    String GET_MEMBER = Operator.GET_MEMBER.getName();

    StatementResource getResource();

    FileNode setResource(StatementResource resource);

    File getFile();

    String[] getFullname();

    String getJoinedFullname();

    ImportType getImportType();

    String getSimpleName();

    CompileStage getStage();

    boolean isFile();

    boolean isPackage();

    boolean isStructure();

    default boolean hasParent() {
        return getFullname().length > 1;
    }

    /**
     * @return 只有structure和alias的二选一, true for alias and false for structure, else is error
     */
    default boolean typeIsAlias(FullIdentifierString requestByIdentifier, File requestFrom) {
        if (this.isFile()) {
            String nameIfAlias = PackageMessageFactory.getNameIfAlias(
                    this.getFullname(), requestByIdentifier.getFullname());
            if (nameIfAlias == null) {
                // 不是alias
                throw new CompileMultipleFileException(requestFrom,
                        requestByIdentifier.getPositionAt(this.getFullname().length), "Unknown type"
                );
            }
            // 似乎是fileNode里的alias了
            // 递归地获取关系
            // file stage 与方法无关, 因为不会改变存储结构, 只会改变存储内容

            return true;
        }

        if (!this.isStructure()) {
            // 你想干嘛(ˉ▽ˉ；)...
            throw new CompileMultipleFileException(requestFrom,
                    requestByIdentifier.getPositionAt(requestByIdentifier.length() - 1), "is it a package?"
            );
        }
        String nameIfAlias = PackageMessageFactory.getNameIfAlias(
                this.getFullname(), requestByIdentifier.getFullname());
        // 是structure
        // 似乎是fileNode里的alias了
        // 考虑Identifier是Generic的情况
        // extends 一个 T 合适吗? 不合适.
        // implements 一个 T 合适吗? 不合适.
        //  = origin 是一个 T 合适吗 ? 不合适. 本来应该是可以的, 但是没有意义, 所以就禁止了
        if (nameIfAlias == null && !this.isStructure(requestByIdentifier)) {
            // 不是alias
            throw new CompileMultipleFileException(requestFrom,
                    requestByIdentifier.getPositionAt(this.getFullname().length), "Unknown type"
            );
        } else {
            return nameIfAlias != null;
        }
    }

    default boolean isStructure(FullIdentifierString requestByIdentifier) {
        return this.getJoinedFullname().equals(requestByIdentifier.joinFullnameString(GET_MEMBER));
    }
}
