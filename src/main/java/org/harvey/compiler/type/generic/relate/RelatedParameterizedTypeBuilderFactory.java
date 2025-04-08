package org.harvey.compiler.type.generic.relate;

import lombok.AllArgsConstructor;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.cache.FileCache;
import org.harvey.compiler.io.cache.node.FileNode;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.io.stage.CompileStage;
import org.harvey.compiler.type.raw.RawTypeRelationshipLoader;
import org.harvey.compiler.type.raw.RelationRawType;
import org.harvey.compiler.type.transform.AssignManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.harvey.compiler.io.cache.node.FileNode.GET_MEMBER;


/**
 * 全局唯一的生产工具, 用缓存提高效率
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-25 18:46
 */
public class RelatedParameterizedTypeBuilderFactory {
    private final FileCache fileCache;
    private final AssignManager assignManager;
    private final GenericDefineReader genericDefineReader;
    private final RawTypeRelationshipLoader rawTypeRelationshipLoader;
    private final RelatedGenericDefineCache relatedGenericDefineCache;
    private final Map<String, RelatedParameterizedTypeBuilder> productCache = new HashMap<>();
    private volatile ParameterizedRelationLoader parameterizedRelationLoaderSingleton;

    @AllArgsConstructor
    public static final class Material {
        private final String fullIdentifier;
        private final File typeFromFile;
        private final IdentifierManager manager;
        private final CompileStage compileStage;

        public Material(
                FullIdentifierString fullIdentifier,
                File typeFromFile,
                IdentifierManager manager,
                CompileStage compileStage) {
            this(fullIdentifier.joinFullnameString(GET_MEMBER), typeFromFile, manager, compileStage);
        }

        public Material(
                String[] fullIdentifier, File typeFromFile, IdentifierManager manager, CompileStage compileStage) {
            this(StringUtil.join(fullIdentifier, GET_MEMBER), typeFromFile, manager, compileStage);
        }
    }

    public RelatedParameterizedTypeBuilderFactory(
            FileCache fileCache,
            AssignManager assignManager,
            GenericDefineReader genericDefineReader,
            RawTypeRelationshipLoader rawTypeRelationshipLoader,
            RelatedGenericDefineCache relatedGenericDefineCache) {
        this.fileCache = fileCache;
        this.assignManager = assignManager;
        this.genericDefineReader = genericDefineReader;
        this.rawTypeRelationshipLoader = rawTypeRelationshipLoader;
        this.relatedGenericDefineCache = relatedGenericDefineCache;
    }


    public RelatedParameterizedTypeBuilder create(Material material) {
        RelatedParameterizedTypeBuilder inCache = productCache.get(material.fullIdentifier);
        if (inCache != null) {
            // 缓存命中
            return inCache;
        }
        RelatedParameterizedTypeBuilder result = create0(
                material.typeFromFile, material.compileStage, material.manager);
        productCache.put(material.fullIdentifier, result);
        return result;
    }

    private RelatedParameterizedTypeBuilder create0(
            File typeFromFile, CompileStage compileStage, IdentifierManager manager) {
        return new RelatedParameterizedTypeBuilder(assignManager, genericDefineReader, rawTypeRelationshipLoader,
                relatedGenericDefineCache, typeFromFile, compileStage, manager
        );
    }

    /**
     * 读取文件, 然后获取identifierMessage和compileStage
     */
    public RelatedParameterizedTypeBuilder create(
            FullIdentifierString declareIdentifier, File fromFile) throws IOException {
        FileNode fileNode = fileCache.getOrCompileOrReadTargetOrCache(declareIdentifier);
        StatementResource resource = fileNode.getResource();
        return create(
                new Material(declareIdentifier.getFullname(), fromFile, resource.getManager(), fileNode.getStage()));
    }

    public RelatedParameterizedTypeBuilder create(RelationRawType relationRawType) {
        FileNode fileNode = fileCache.getInCache(relationRawType.getJoinedFullname());
        if (fileNode == null) {
            throw new CompilerException(
                    "raw type '" + relationRawType.getClass().getSimpleName() + "' created but not in cache.");
        }
        StatementResource resource = fileNode.getResource();
        return create(
                new Material(
                        relationRawType.getFullname(), relationRawType.getFromFile(), resource.getManager(),
                        fileNode.getStage()
                ));
    }

    public ParameterizedRelationLoader singletonLoader() {
        if (parameterizedRelationLoaderSingleton == null) {
            synchronized (this) {
                if (parameterizedRelationLoaderSingleton == null) {
                    parameterizedRelationLoaderSingleton = new ParameterizedRelationLoader(fileCache,
                            relatedGenericDefineCache, this
                    );
                }

            }
        }

        return parameterizedRelationLoaderSingleton;
    }
}
