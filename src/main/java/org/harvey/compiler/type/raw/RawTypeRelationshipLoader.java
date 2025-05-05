package org.harvey.compiler.type.raw;

import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.cache.FileCache;
import org.harvey.compiler.io.cache.node.FileNode;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.io.stage.CompileStage;
import org.harvey.compiler.type.RelationshipBuildStage;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import static org.harvey.compiler.io.cache.node.FileNode.GET_MEMBER;
import static org.harvey.compiler.type.raw.RelationshipPhaser.isUpper;

/**
 * TODO 1. alias 检查. 2. sealed 检查 3. static 检查
 * <p>
 * 有些type是sealed的, 这样就不能继承了, <br>
 * 但是alias来说, 是否sealed完全取决于origin, 如果origin是sealed的, alias也是sealed的...<br>
 * 有些alias, 的origin是sealed的,有些origin的就不是了... 咋办呢...
 * <p>
 * Upper是静态的->直接继承
 * Upper是非静态的, 当前Lower要继承这个类, 就一定要能够实例化这个类
 * - Lower一直往外走, 一直都是非静态的, 直到有一个类(as X), 是:
 * <pre>{@code
 * for X in queue[Lower.outer+Lower]:
 *     X is Upper.outer(无关static)->success
 *     X is static->break
 *     X is Upper->success
 *     X.upper is Upper
 *     X.upper is Upper.outer(无关static)->success
 *     continue
 * failed
 *
 * }</pre>
 * <p>
 * 要获取outer, 那么identifier set就是不一样了
 * Upper要获取outer, 只要获取一个identifier就行了
 * X.outer一直分析, 分析到Upper或者upper.outer为止
 * 如果outer也一样, 嵌套了非static , 咋办?
 * alias 和 static的关系呢? static的alias作为origin, 一定可以, 如果非static的alias作为origin, 要分类考虑
 * <pre>{@code
 * 如果alias.origin是interface. 而又是非static, 这是可能的, 因为要用到outer的泛型, 那么, 它即使是interface, 也是非static的了...
 * alias要获取非静态的对象, 就像structure一样获取
 * 其他对象要获取非静态的alias, 也是outer, upper来一套!
 * outer可以通过对路径的解析, 然后从fileCache中获取
 * 难点在于, 每一个outer要获取upper, 就需要担心递归的问题了
 * }</pre>
 * upper  ->    not static--------------\
 * |                   ↓               |
 * \-> static->[outer+]upper           |
 * |                            ↓
 * \----------------------->no upper 结束
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 22:03
 */
public class RawTypeRelationshipLoader {


    // 从需求出发, 想要一个类, 能直接访问外部类的非静态成员. 将这个类叫做非静态内部类, 其他的类叫做静态内部类.
    // 内部类要实例化对象的权限只有外部类吗?
    // 想访问非静态内部类的静态字段和静态方法, 可直接调用
    // 非静态内部类的静态方法和其他的静态方法完全一样
    // 想示例化非静态内部类的对象, 需要用已经实例化的Outer对象, outer.new NonStatic()来实例化;
    // 想继承非静态内部类, 实际上是实例化这个类, 因为继承一个类就是要实例化其父类的
    // 现在想知道的就是,静态内部类是怎么获取外部类的, 是一个引用吗? 好吧
    // 要示例化一个非静态内部类, 就看这个实例化的地方能不能给出这个非静态内部类的外部类


    private final RelationCache relationCache;
    private final PackageMessageFactory packageMessageFactory;
    private final FileCache fileCache;

    public RawTypeRelationshipLoader(
            RelationCache relationCache, FileCache fileCache, PackageMessageFactory packageMessageFactory) {
        this.relationCache = relationCache;
        this.fileCache = fileCache;
        this.packageMessageFactory = packageMessageFactory;
    }

    private static BuildToCacheQueueElement loadParamElement(
            File targetFromFile, FullIdentifierString targetIdentifier) {
        return new BuildToCacheQueueElement(targetIdentifier, targetFromFile, null, false, false, new HashSet<>());
    }


    // 向外暴露
    public RelationRawType load(File typeFromFile, FullIdentifierString identifier) throws IOException {
        RelationRawType rawType = loadToCache(typeFromFile, identifier);
        validOnVisitable(rawType);
        return rawType;
    }

    // ----------------------------------分析static-----------------------------------------
    private void validOnVisitable(RelationRawType rawType) throws IOException {
        LinkedList<RelationRawType> newUncheckedAtChecking = new LinkedList<>();
        newUncheckedAtChecking.add(rawType);
        while (!newUncheckedAtChecking.isEmpty()) {
            validOnVisitable(newUncheckedAtChecking);
        }
    }

    /**
     * @param newUncheckedAtChecking 在中产生的没有检查过validOnStatic的RelationRawType
     */
    private void validOnVisitable(LinkedList<RelationRawType> newUncheckedAtChecking) throws IOException {
        if (newUncheckedAtChecking.isEmpty()) {
            return;
        }
        LinkedList<RelationRawType> queue = new LinkedList<>();
        queue.addLast(newUncheckedAtChecking.removeFirst());
        while (!queue.isEmpty()) {
            RelationRawType each = queue.removeFirst();
            RelationRawType parent = each.getParent();
            if (parent != null) {
                validOnVisitable(each, parent, newUncheckedAtChecking);
                each.updateStage(RelationshipBuildStage.GENERIC_DEFINE_AND_UPPER_PARAMETERIZED_TYPE_CHECK);
                queue.add(parent);
            }
            for (RelationRawType eachInterface : each.getInterfaces()) {
                validOnVisitable(each, eachInterface, newUncheckedAtChecking);
                each.updateStage(RelationshipBuildStage.GENERIC_DEFINE_AND_UPPER_PARAMETERIZED_TYPE_CHECK);
                queue.add(eachInterface);
            }
        }
    }


    // ----------------------------------分析关系-----------------------------------------

    private void validOnVisitable(
            RelationRawType lower,
            RelationRawType upper,
            LinkedList<RelationRawType> newUncheckedAtChecking) throws IOException {
        if (upper == null) {
            return;
        }
        permissionVisitable(lower, upper);
        boolean visitable = staticVisitable(lower, upper, newUncheckedAtChecking);
        if (!visitable) {
            throw new CompileMultipleFileException(lower.getFromFile(), lower.getDeclareIdentifier().getPosition(),
                    " can not visit " + upper.getJoinedFullname() + " for static member can not visit non static member"
            );
        }
    }

    private void permissionVisitable(RelationRawType lower, RelationRawType upper) {
        AccessControl accessControl = getAccessControl(upper);
        boolean accessible = validOnPermission(lower, accessControl, upper);
        if (!accessible) {
            throw new CompileMultipleFileException(lower.getFromFile(), lower.getDeclareIdentifier().getPosition(),
                    " can not visit " +
                    upper.getJoinedFullname() +
                    " for member can not visit " +
                    accessControl +
                    " member"
            );
        }
    }

    /**
     * 由于这个方法都会
     * <pre>{@code
     * for X in queue[Lower.outer+Lower]:
     *      X is Upper.outer(无关static)->success
     *      X is static->break
     *      X is Upper->success
     *      X.upper is Upper->success
     *      X.upper is Upper.outer(无关static)->success
     *      continue
     * failed
     * }</pre>
     *
     * @param upper                  not static (确信)
     * @param newUncheckedAtChecking 检查static导致产生的新的, 未确定Upper static的对象, 全部都会加入此处
     */
    private boolean staticVisitable(
            RelationRawType lower,
            RelationRawType upper,
            LinkedList<RelationRawType> newUncheckedAtChecking) throws IOException {
        if (upper.isStatic()) {
            return true;
        }
        if (lower.isAlias()) {
            // lower是alias, 那么其upper, 不需要实例化, 也可以访问
            return true;
        }
        Stack<String[]> lowerOuters = packageMessageFactory.getOuterStructureFilenameStack(
                lower.getDeclareIdentifier());
        FullIdentifierString upperIdentifier = upper.getDeclareIdentifier();

        String[] upperPath = upperIdentifier.getFullname();
        String[] upperOuterPath = upperIdentifier.getRange(0, upperIdentifier.length() - 1);
        // 如果UpperOuter是file, 那么Upper一定是static, 由于Upper不是static, 所以UpperOuter是structure
        RelationRawType upperOuterRelationType = null;
        while (!lowerOuters.empty()) {
            String[] lowerOuterPath = lowerOuters.pop();
            // 需要获取lowerOuter的identifier
            // lowerOuter的identifier比较UpperOuter
            if (lowerOuterPath.length == upperOuterPath.length &&
                ArrayUtil.firstDifferenceIndex(lowerOuterPath, upperOuterPath) == lowerOuterPath.length) {
                return true;
            }
            // 需要获取lowerOuter的static
            FileNode lowerOuterFileNode = fileCache.getReadTargetOrCache(lowerOuterPath);
            if (!lowerOuterFileNode.isStructure()) {
                throw new IOException("expected a structure!", new CompilerException());
            }
            StatementResource resource = lowerOuterFileNode.getResource();
            if (resource.isStatic()) {
                break;
            }
            // lowerOuter的identifier比较Upper
            if (lowerOuterPath.length == upperPath.length &&
                ArrayUtil.firstDifferenceIndex(lowerOuterPath, upperPath) == lowerOuterPath.length) {
                return true;
            }
            // lowerOuter和UpperOuter是继承关系
            RelationRawType lowerRelationRawType = loadToCache(
                    lowerOuterFileNode.getFile(),
                    resource.getManager().getIdentifier(resource.getDeclareIdentifierReference())
            );
            addUncheckedVisibleChecking(newUncheckedAtChecking, lowerRelationRawType);

            if (isUpper(lowerRelationRawType, upper)) {
                return true;
            }
            if (upperOuterRelationType == null) {
                FileNode upperOuterFileNode = fileCache.getReadTargetOrCache(upperOuterPath);
                if (!upperOuterFileNode.isStructure()) {
                    throw new IOException("expected a structure!", new CompilerException());
                }
                StatementResource upperOuterResource = upperOuterFileNode.getResource();
                FullIdentifierString upperOuterIdentifier = upperOuterResource.getManager()
                        .getIdentifier(upperOuterResource.getDeclareIdentifierReference());
                upperOuterRelationType = loadToCache(upperOuterFileNode.getFile(), upperOuterIdentifier);
                addUncheckedVisibleChecking(newUncheckedAtChecking, lowerRelationRawType);
            }
            if (isUpper(lowerRelationRawType, upperOuterRelationType)) {
                return true;
            }
        }
        return false;
    }

    private void addUncheckedVisibleChecking(
            LinkedList<RelationRawType> newUncheckedAtChecking, RelationRawType lowerRelationRawType) {
        LinkedList<RelationRawType> queue = new LinkedList<>();
        queue.addLast(lowerRelationRawType);
        while (!queue.isEmpty()) {
            RelationRawType each = queue.removeFirst();
            RelationshipBuildStage stage = each.getStage();
            switch (stage) {
                case ALIAS_RAW_TYPE_MAP:
                case STRUCTURE_RAW_TYPE_RELATIONSHIP:
                    throw new CompilerException("too early to check static");
                case VISIBLE_CHECK:
                    RelationRawType parent = each.getParent();
                    if (parent != null) {
                        queue.addLast(parent);
                    }
                    for (RelationRawType anInterface : each.getInterfaces()) {
                        queue.addLast(anInterface);
                    }
                    newUncheckedAtChecking.add(each);
                case GENERIC_DEFINE_AND_UPPER_PARAMETERIZED_TYPE_CHECK:
                case ALIAS_MAP:
                case FINISHED:
                default:
            }

        }

    }

    private RelationRawType loadToCache(
            File targetFromFile, FullIdentifierString targetIdentifier) throws IOException {
        BuildToCacheQueueElement initElement = loadParamElement(targetFromFile, targetIdentifier);
        LinkedList<BuildToCacheQueueElement> queue = new LinkedList<>();
        queue.addLast(initElement);
        while (!queue.isEmpty()) {
            BuildToCacheQueueElement first = queue.getFirst();
            String key = first.targetIdentifier.joinFullnameString(GET_MEMBER);
            // 不在缓存中
            RelationRawType inCache = relationCache.get(key);
            if (inCache != null) {// cache
                if (inCache.needSetEndOrigin()) {
                    throw new CompilerException("in cache but not set origin");
                }
                if (first.getLower() == null) {
                    if (!queue.isEmpty()) {
                        throw new CompilerException("impossible");
                    }
                    return inCache;
                }
                setUpper(first.lower, inCache, first.wantSuper, first.wantInterface);
                continue;
            }
            // first 可能是alias, 可能是
            FileNode fileNode = fileCache.getOrCompileOrReadTargetOrCache(first.targetIdentifier);
            CompileStage fileNodeStage = fileNode.getStage();
            RelationshipBuildStage stage = RelationshipBuildStage.get(fileNodeStage);
            if (stage == null) {
                throw new CompilerException("impossible for file is package");
            }
            boolean isAlias = fileNode.typeIsAlias(first.targetIdentifier, first.requestFrom);
            // 这个alias可能是structure里的, 也有可能是file里的
            StatementResource resource = fileNode.getResource();

            if (isAlias) {
                // Alias 要先until origin
                RelationRawType origin = dealAliasWithOuter(key, first.targetIdentifier, first.lower, resource, stage,
                        fileNode.getFile(), first.thisLineIdentifier, queue
                );
                setUpper(first.lower, origin, first.wantSuper, first.wantInterface);
            } else {
                RelationRawType result = dealStructureWithOuter(
                        resource, first.thisLineIdentifier, stage, fileNode.getFile(), queue);
                setUpper(first.lower, result, first.wantSuper, first.wantInterface);

            }
        }
        String key = targetIdentifier.joinFullnameString(GET_MEMBER);
        return relationCache.get(key);
    }


    private void setUpper(RelationRawType lower, RelationRawType upper, boolean setSuper, boolean setInterface) {
        if (setInterface) {
            // interface 一定是static且not sealed的
            if (upper.getType() != StructureType.INTERFACE) {
                throw new CompileMultipleFileException(lower.getFromFile(), lower.getDeclareIdentifier().getPosition(),
                        "except an interface"
                );
            }
            lower.addInterfaces(upper);
        } else if (setSuper) {
            // 答案是需要对所有的alias先进行扫描映射, 信息设置, 先对RawType进行设置
            if (lower.isStructure() && upper.getType() != lower.getType()) {
                throw new CompileMultipleFileException(lower.getFromFile(), lower.getDeclareIdentifier().getPosition(),
                        "except " + lower.getType().name()
                );
            }
            // \ddl
            lower.setParent(upper);
        }

    }

    private RelationRawType dealStructureWithOuter(
            StatementResource resource,
            Set<String> lowerIdentifiers,
            RelationshipBuildStage stage,
            File resourceFile,
            LinkedList<BuildToCacheQueueElement> queue) {
        RelationRawType relationRawType = dealStructure(resource, lowerIdentifiers, stage, resourceFile, queue);
        dealOuterStructure(resource, queue);
        return relationRawType;
    }

    private RelationRawType dealAliasWithOuter(
            String joinedFullnameString,
            FullIdentifierString identifier,
            RelationRawType lower,
            StatementResource resource,
            RelationshipBuildStage stage,
            File resourceFile,
            Set<String> lowerIdentifiers,
            LinkedList<BuildToCacheQueueElement> queue) throws IOException {
        RelationRawType relationRawType = dealAlias(joinedFullnameString, identifier, lower, resource, stage,
                resourceFile, lowerIdentifiers, queue
        );
        DIdentifierManager manager = resource.getManager();
        FullIdentifierString outerIdentifier = manager.getIdentifier(resource.getDeclareIdentifierReference());
        if (!relationCache.containsKey(outerIdentifier)) {
            dealStructure(resource, lowerIdentifiers, stage, resourceFile, queue);
        }
        dealOuterStructure(resource, queue);
        return relationRawType;
    }

    private void dealOuterStructure(StatementResource resource, LinkedList<BuildToCacheQueueElement> queue) {
        FullIdentifierString identifier = resource.getManager().getIdentifier(resource.getDeclareIdentifierReference());
        packageMessageFactory.forEachOuterStructure(identifier, (path, outerFile) -> {
            String key = StringUtil.join(path, GET_MEMBER);
            if (!this.relationCache.containsKey(key)) {
                return null;
            }
            FileNode outerFileNode;
            try {
                outerFileNode = fileCache.getReadTargetOrCache(path);
            } catch (IOException e) {
                throw new CompilerFileReadException(e);
            }
            // 将其中的生成的rawType作为加入queue的孩子
            dealStructure(outerFileNode.getResource(), new HashSet<>(),
                    RelationshipBuildStage.get(outerFileNode.getStage()), outerFile, queue
            );


            return null;
        });
    }

    /**
     * @param stage 完全取决于CompileStage
     */
    private RelationRawType dealStructure(
            StatementResource resource,
            Set<String> lowerIdentifiers,
            RelationshipBuildStage stage,
            File resourceFile,
            LinkedList<BuildToCacheQueueElement> queue) {
        DIdentifierManager manager = resource.getManager();
        FullIdentifierString identifier = manager.getIdentifier(resource.getDeclareIdentifierReference());
        FullIdentifierString superIdentifier = manager.getIdentifier(resource.getSuperComplexStructure().getRawType());
        FullIdentifierString[] interfaceIdentifiers = resource.getInterfaceList()
                .stream()
                .map(ParameterizedType::getRawType)
                .map(manager::getIdentifier)
                .toArray(FullIdentifierString[]::new);
        StructureType type = resource.getStructureType();
        RelationRawType result = new StructureRelationRawType(
                identifier, type, stage, resourceFile, resource.isSealed(), resource.isStatic());
        putAfterLoad(result);
        if (superIdentifier != null) {
            queue.addLast(
                    new BuildToCacheQueueElement(superIdentifier, resourceFile, result, true, false, lowerIdentifiers));
        }
        for (FullIdentifierString interfaceRawIdentifier : interfaceIdentifiers) {
            queue.addLast(new BuildToCacheQueueElement(interfaceRawIdentifier, resourceFile, result, false, true,
                    lowerIdentifiers
            ));
        }
        return result;
    }

    private RelationRawType dealAlias(
            String joinedFullnameString,
            FullIdentifierString identifier,
            RelationRawType lower,
            StatementResource resource,
            RelationshipBuildStage stage,
            File resourceFile,
            Set<String> lowerIdentifiers,
            LinkedList<BuildToCacheQueueElement> queue) throws IOException {
        Stack<RelationRawType> aliases = new Stack<>();
        // lowerIdentifiers现阶段保存到的有alias的信息, 还没有origin的
        lowerIdentifiers = new HashSet<>(lowerIdentifiers);
        while (true) {
            TypeAlias typeAlias = resource.getTypeAlias(identifier, joinedFullnameString);
            Pair<RelationRawType, FullIdentifierString> aliasPair = getAliasPair(
                    resource, typeAlias, stage, resourceFile);
            RelationRawType curAlias = aliasPair.getKey();
            aliases.push(curAlias);
            // 处理映射问题
            FullIdentifierString originIdentifier = aliasPair.getValue();

            // 从缓存中拿
            String key = originIdentifier.joinFullnameString(GET_MEMBER);
            if (lowerIdentifiers.contains(key)) {
                // 检查 origin
                throw new CompileMultipleFileException(resourceFile, identifier.getPosition(),
                        "alias circular dependencies"
                );
            }

            // 不在缓存中
            RelationRawType inCache = relationCache.get(key);

            if (inCache != null) {// cache
                if (inCache.needSetEndOrigin()) {
                    throw new CompilerException("not deal origin, but in cache");
                }
                RelationRawType origin = inCache.getEndOrigin();
                while (!aliases.isEmpty()) {
                    RelationRawType top = aliases.pop();
                    top.setEndOrigin(origin);
                    putAfterLoad(top);
                }
                // lowerIdentifiers 的局部变量被舍弃
                return origin;
            }
            // get from file
            FileNode fileNode = fileCache.getOrCompileOrReadTargetOrCache(originIdentifier);
            stage = RelationshipBuildStage.get(fileNode.getStage());
            boolean isAlias = fileNode.typeIsAlias(originIdentifier, resourceFile);

            if (isAlias) {
                joinedFullnameString = key;
                identifier = originIdentifier;
                resourceFile = fileNode.getFile();
                resource = fileNode.getResource();
                lower.setParent(curAlias);
                lower = curAlias;
                lowerIdentifiers.add(key);
            } else {
                RelationRawType origin = dealStructureWithOuter(
                        resource, lowerIdentifiers, stage, fileNode.getFile(), queue);

                while (!aliases.isEmpty()) {
                    RelationRawType top = aliases.pop();
                    top.setEndOrigin(origin);
                    putAfterLoad(top);
                }
                // 此时的curAlias没有setParent
                BuildToCacheQueueElement structureElement = new BuildToCacheQueueElement(
                        originIdentifier, resourceFile, curAlias, true, false, lowerIdentifiers);
                queue.addLast(structureElement);
                return origin;
            }
        }

    }


    private boolean validOnPermission(RelationRawType lower, AccessControl accessControl, RelationRawType upper) {
        return packageMessageFactory.accessible(
                lower.getDeclareIdentifier(), accessControl, upper.getDeclareIdentifier(), (path, file) -> {
                    String key = StringUtil.join(path, GET_MEMBER);
                    RelationRawType lowerOuter = relationCache.get(key);
                    if (lowerOuter == null) {
                        throw new CompilerException("must load lower outer into cache, than ,check valid permission");
                    }
                    return RelationshipPhaser.isUpper(lowerOuter, upper);
                });
    }

    private AccessControl getAccessControl(RelationRawType upper) {
        FullIdentifierString upperDeclaration = upper.getDeclareIdentifier();
        FileNode fileNode = fileCache.getInCache(upperDeclaration);
        if (fileNode == null) {
            throw new CompilerException("declare identifier in relationship but not a structure in file cache.");
        }
        StatementResource upperResource = fileNode.getResource();
        AccessControl accessControl;
        if (upper.isAlias()) {
            // 也有可能是alias...
            TypeAlias typeAlias = fileNode.getResource()
                    .getTypeAlias(upper.getDeclareIdentifier(), upper.getJoinedFullname());
            accessControl = typeAlias.getAccessControl();
        } else if (upper.isStructure()) {
            accessControl = upperResource.getAccessControl();
        } else {
            throw new CompilerException("can not get access version2 of " + upper.getJoinedFullname());
        }
        return accessControl;
    }


    private Pair<RelationRawType, FullIdentifierString> getAliasPair(
            StatementResource resource, TypeAlias typeAlias, RelationshipBuildStage stage, File resourceFile) {
        DIdentifierManager manager = resource.getManager();
        FullIdentifierString declareIdentifier = manager.getIdentifier(typeAlias.getAliasNameReference());
        RelationRawType result = new AliasRelationRawType(declareIdentifier, stage, resourceFile, resource.isStatic());
        // 需要result向上去找, 找到alias为止
        FullIdentifierString originIdentifier = manager.getIdentifier(typeAlias.getOrigin().getRawType());
        return new Pair<>(result, originIdentifier);
    }


    // ----------------------------------使用缓存-----------------------------------------
    private RelationRawType putAfterLoad(RelationRawType result) {
        result.updateStage(RelationshipBuildStage.VISIBLE_CHECK);
        String key = result.getJoinedFullname();
        if (relationCache.containsKey(key)) {
            throw new CompilerException("can not update relation cache value: " + key);
        }
        return relationCache.put(key, result);
    }

    private static class BuildToCacheQueueElement {
        final FullIdentifierString targetIdentifier;
        final String targetIdentifierKey;
        final File requestFrom;
        final boolean wantSuper;
        final boolean wantInterface;
        final Set<String> thisLineIdentifier;
        @Getter
        RelationRawType lower;

        public BuildToCacheQueueElement(
                FullIdentifierString targetIdentifier,
                File requestFrom,
                RelationRawType lower,
                boolean wantSuper,
                boolean wantInterface,
                Set<String> lowerIdentifier) {
            this.targetIdentifier = targetIdentifier;
            targetIdentifierKey = targetIdentifier.joinFullnameString(GET_MEMBER);
            this.requestFrom = requestFrom;
            this.lower = lower;
            this.wantSuper = wantSuper;
            this.wantInterface = wantInterface;
            if (lowerIdentifier.contains(targetIdentifierKey)) {
                throw new CompileMultipleFileException(requestFrom, targetIdentifier.getPosition(),
                        "Circular inheritance"
                );
            }
            this.thisLineIdentifier = new HashSet<>(lowerIdentifier);
            this.thisLineIdentifier.add(targetIdentifier.joinFullnameString(targetIdentifierKey));
            if (lower != null && wantSuper == wantInterface) {
                throw new CompilerException("can not want super and want interface at the same time");
            }
        }

    }


}
