package org.harvey.compiler.type.generic.relate;

import lombok.AllArgsConstructor;
import org.harvey.compiler.common.collecction.ListPoint;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.cache.FileCache;
import org.harvey.compiler.io.cache.node.FileNode;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.raw.RelationRawType;

import java.io.File;
import java.io.IOException;

/**
 * 已知关系后, 将GenericType中的有关类型的部分转换成关系, 以此来检查Generic是否正确
 * 从文件中读取GenericDefine
 * 本类不会把信息存入cache
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 15:44
 */
@AllArgsConstructor
public class GenericDefineReader {
    private final FileCache fileCache;
    private final PackageMessageFactory packageMessageFactory;

    // ---------------------------------获取GenericDefine-----------------------------------------
    public GenericDefine[] read(RelationRawType rawType) throws IOException {
        // GenericDefineList, 先注册所有的Identifier, 然后标注未解析, 然后开始解析, 如果解析到未解析的, 就是非法前向引用
        return read(rawType.getFromFile(), rawType.getDeclareIdentifier());
    }

    /**
     * @param genericDefinedPlace 泛型的Fullname. 只能使用在Structure和Alias的定义上
     */
    public GenericDefine[] read(
            File requestFrom, FullIdentifierString genericDefinedPlace) throws IOException {
        ListPoint<File> fileInTarget = packageMessageFactory.filePathForImport(genericDefinedPlace);
        if (fileInTarget.getElement() == null) {
            // package
            throw new CompileMultipleFileException(requestFrom,
                    genericDefinedPlace.getPositionAt(fileInTarget.getIndex()), "expect a structure, but not a package"
            );
        }
        FileNode fileNode = fileCache.getOrCompileOrReadTargetOrCache(genericDefinedPlace);
        if (fileNode.typeIsAlias(genericDefinedPlace, requestFrom)) {
            TypeAlias typeAlias = fileNode.getResource().getTypeAlias(genericDefinedPlace);
            return typeAlias.getAliasGenericMessage();
        }
        StatementResource resource = fileNode.getResource();
        if (resource.isStructure()) {
            return resource.getGenericMessage();
        }
        throw new CompileMultipleFileException(requestFrom, genericDefinedPlace.getPosition(),
                "expect a structure, but not a file"
        );
    }

}
