package org.harvey.compiler.declare.identifier;


import lombok.Getter;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-26 21:15
 */
@Deprecated
public class DeprecatedIdentifierManager implements DIdentifierManager {
    public static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();
    public static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    private final Map<String, Integer> declaredIdentifierMap;
    @Getter
    private final Map<String, ImportString> importTable;
    /**
     * 其value表示第一个import的identifier
     */
    @Getter
    private final int importReferenceAfterIndex;
    @Getter
    private final List<FullIdentifierString> allIdentifierTable;
    private final Map<String, Integer> allIdentifierCache;
    @Getter
    private final int preLength;
    private boolean canGetGenericDefine = true;

    /**
     * TODO 是否需要当前文件路径, 用来分析import到当前文件的情况
     *
     * @param importTable             不会重复
     * @param declaredIdentifierTable 不会重复
     */
    public DeprecatedIdentifierManager(
            Map<String, ImportString> importTable,
            List<IdentifierString> declaredIdentifierTable,
            int preLength) {
        this.preLength = preLength;
        this.importTable = importTable;
        this.allIdentifierTable = declaredIdentifierTable.stream()
                .map(DeprecatedIdentifierManager::identifier2Full)
                .collect(Collectors.toList());
        importReferenceAfterIndex = allIdentifierTable.size();
        allIdentifierCache = new HashMap<>();
        this.declaredIdentifierMap = new HashMap<>();
        initDeclareMapAndCache();
    }

    public DeprecatedIdentifierManager(
            Collection<ImportString> importTable,
            int importReferenceAfterIndex,
            int preLength,
            ArrayList<FullIdentifierString> allIdentifierTable) {
        this.preLength = preLength;
        this.importTable = importTable.stream().collect(Collectors.toMap(i -> i.getTarget().getValue(), i -> i));
        this.allIdentifierTable = allIdentifierTable;
        this.importReferenceAfterIndex = importReferenceAfterIndex;
        allIdentifierCache = new HashMap<>();
        this.declaredIdentifierMap = new HashMap<>();
        initDeclareMapAndCache();
    }


    /**
     * @param identifier 是文件级别的identifier
     */
    public static boolean isFileDeclaration(String identifier) {
        return !identifier.contains(DIdentifierPoolFactory.MEMBER);
    }

    private static FullIdentifierString identifier2Full(IdentifierString e) {
        String[] strings = StringUtil.simpleSplit(e.getValue(), DIdentifierPoolFactory.MEMBER);
        SourcePosition[] sp = new SourcePosition[strings.length];
        Arrays.fill(sp, e.getPosition());
        return new FullIdentifierString(sp, strings);
    }

    private static String joinFullname(String[] fullname) {
        return StringUtil.join(fullname, DIdentifierPoolFactory.MEMBER);
    }

    private void initDeclareMapAndCache() {
        for (int i = 0; i < importReferenceAfterIndex; i++) {
            FullIdentifierString element = allIdentifierTable.get(i);
            // 从需求除法思考什么identifier能重名, 什么不能
            // 方法重写->允许
            // import和identifier->?如果允许, 一个只能写全类名
            // outer和inner的关系
            // 答案是, 让import失效, Identifier总是优先
            StringJoiner joiner = new StringJoiner(DIdentifierPoolFactory.MEMBER);
            for (int j = element.length() - 1; j >= preLength - 1/*一定有一个文件名, 文件名开头这是可行的*/; j--) {
                String item = element.get(j);
                joiner.add(item);
                String key = joiner.toString();

                if (!declaredIdentifierMap.containsKey(key)) {
                    declaredIdentifierMap.put(key, i);
                } else {
                    declaredIdentifierMap.put(key, null);
                }
                if (!allIdentifierCache.containsKey(key)) {
                    allIdentifierCache.put(key, i);
                } else {
                    allIdentifierCache.put(key, null);
                }
            }

        }
    }

    @Override
    public ReferenceElement getReferenceAndAddIfNotExist(FullIdentifierString fullname) {
        ReferenceElement reference = getReference(fullname);
        if (reference != null) {
            return reference;
        }
        return addFullname(fullname);
    }

    /**
     * @return null for not exist
     */
    @Override
    public ReferenceElement getReference(FullIdentifierString fullname) {
        if (afterRead()) {
            throw new CompilerException("read reference only");
        }
        ReferenceElement reference = getFromDeclare(fullname.getFullname());
        if (reference != null) {
            return reference;
        }
        return importThenRegister(fullname);
    }


    @Override
    public boolean isImport(FullIdentifierString fullname) {
        if (afterRead()) {
            throw new CompilerException("read reference only");
        }
        String fullPre = fullname.get(0);
        return importTable.containsKey(fullPre);
    }

    @Override
    public boolean isImport(ReferenceElement reference) {
        if (reference.getType() != ReferenceType.IDENTIFIER) {
            return false;
        }
        return reference.getReference() >= importReferenceAfterIndex;
    }

    @Override
    public boolean isDeclarationInFile(ReferenceElement reference) {
        if (reference.getType() != ReferenceType.IDENTIFIER) {
            return false;
        }
        int id = reference.getReference();
        return 0 <= id && id < importReferenceAfterIndex;
    }

    @Override
    public boolean afterRead() {
        return importTable == null || declaredIdentifierMap == null || allIdentifierCache == null;
    }


    private ReferenceElement importThenRegister(FullIdentifierString fullname) {
        if (!isImport(fullname)) {
            return null;
        }
        // 要比较不少, 合适吗? 不太合适
        ImportString importPre = importTable.get(fullname.get(0));
        FullIdentifierString joinedIdentifier = joinImport(importPre, fullname);
        String joinedFullname = joinFullname(joinedIdentifier.getFullname());
        int index = allIdentifierCache.get(joinedFullname);
        if (index >= 0) {
            return new ReferenceElement(fullname.getPosition(), ReferenceType.IDENTIFIER, index);
        }
        int reference = allIdentifierTable.size();
        allIdentifierCache.put(joinedFullname, reference);
        allIdentifierTable.add(joinedIdentifier);
        // 判断你是哪位, 是import的, 还是Declare的
        return new ReferenceElement(fullname.getPosition(), ReferenceType.IDENTIFIER, reference);
    }

    private ReferenceElement addFullname(FullIdentifierString fullname) {
        int reference = allIdentifierTable.size();
        allIdentifierTable.add(fullname);
        return new ReferenceElement(fullname.getPosition(), ReferenceType.IDENTIFIER, reference);
    }

    private FullIdentifierString joinImport(ImportString importPre, FullIdentifierString fullname) {
        List<SourcePosition> sps = new ArrayList<>();
        List<String> ss = new ArrayList<>();
        for (IdentifierString identifierString : importPre.getPath()) {
            sps.add(identifierString.getPosition());
            ss.add(identifierString.getValue());
        }
        for (int i = 1/*忽略第一个, 在import中*/; i < fullname.length(); i++) {
            sps.add(fullname.getPositionAt(i));
            ss.add(fullname.get(i));
        }
        return new FullIdentifierString(sps.toArray(SourcePosition[]::new), ss.toArray(String[]::new));
    }

    /**
     * @param fullname 应当已经根据调用所在位置重新组件过, 例如在类里调用, 该参数应该先补全outer
     */
    @Override
    public ReferenceElement getFromDeclare(String[] fullname) {
        String joinFullnameString = joinFullname(fullname);
        Integer reference = declaredIdentifierMap.get(joinFullnameString);
        if (reference != null) {
            SourcePosition position = allIdentifierTable.get(reference).getPosition();
            return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
        }
        // 是泛型的可能性
        if (fullname.length != 1) {
            return null;
        }
        if (!canGetGenericDefine) {
            return null;
        }
        reference = tryGeneric(fullname[fullname.length - 1]);
        if (reference == null) {
            return null;
        }
        SourcePosition position = allIdentifierTable.get(reference).getPosition();

        return new ReferenceElement(position, ReferenceType.GENERIC_IDENTIFIER, reference);
    }


    public Integer tryGeneric(String mayGeneric) {
        String genericMark = GENERIC_LIST_PRE_NAME +
                             mayGeneric +
                             GENERIC_LIST_POST_NAME;
        List<Integer> collect = declaredIdentifierMap.entrySet()
                .stream()
                .filter(e -> e.getKey().endsWith(genericMark))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        if (collect.size() > 1) {
            throw new CompilerException("repeated generic name: " + declaredIdentifierMap);
        } else if (collect.size() == 1) {
            return collect.get(0);
        }
        return null;
    }

    @Override
    public IdentifierString getGenericIdentifier(ReferenceElement reference) {
        if (reference.getType() != ReferenceType.GENERIC_IDENTIFIER) {
            throw new CompilerException(
                    "not a generic define reference",
                    new IllegalArgumentException(reference.getType().name())
            );
        }
        FullIdentifierString identifier = getIdentifier(reference);
        String lastName = identifier.get(identifier.length() - 1);
        if (lastName.startsWith(GENERIC_LIST_PRE_NAME) ||
            lastName.equals(GENERIC_LIST_POST_NAME)) {
            throw new CompilerException(
                    "not a generic identifier",
                    new IllegalArgumentException(identifier.toString())
            );
        }
        // 掐头去尾
        String genericName = lastName.substring(1, identifier.length() - 1);
        return new IdentifierString(identifier.getPosition(), genericName);
    }

    @Override
    public FullIdentifierString getIdentifier(ReferenceElement reference) {
        if (reference.getType() != ReferenceType.IDENTIFIER) {
            return null;
        }
        return allIdentifierTable.get(reference.getReference());
    }

    @Override
    public void canGetGenericDefineOnStructure(boolean can) {
        this.canGetGenericDefine = can;
    }

    @Override
    public Set<Integer> getDisableSet() {
        return Collections.emptySet();
    }


}
