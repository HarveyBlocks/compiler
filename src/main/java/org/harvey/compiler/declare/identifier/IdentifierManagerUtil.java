package org.harvey.compiler.declare.identifier;

import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.cache.FileCache;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-13 20:34
 */
public class IdentifierManagerUtil {
    public static final String GET_MEMBER = Operator.GET_MEMBER.getName();
    private final IdentifierManager identifierManager;
    private final FileCache fileCache;
    private final PackageMessageFactory packageMessageFactory;

    public IdentifierManagerUtil(
            IdentifierManager identifierManager,
            FileCache fileCache,
            PackageMessageFactory packageMessageFactory) {
        this.identifierManager = identifierManager;
        this.fileCache = fileCache;
        this.packageMessageFactory = packageMessageFactory;
    }

    /**
     * class or package or file except class, function, field, method
     *
     * @param iterator start with get member, point to next of file
     */
    public File tillNonStaticResource(File first, ListIterator<SourceString> iterator) {
        if (first.isFile()) {
            return first;
        }
        File cur = first;
        // 1. 也有可能不在target, 而在源码
        // first 是 package, 下一个, 合起来, 再找
        // first 是 文件, 停止
        // first 是 非静态资源, 直接返回null
        // first 或许是 从 import 来, 要么是 直接的package而来
        List<IdentifierString> is = new ArrayList<>();
        boolean exceptIdentifier = true;
        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            exceptIdentifier = !exceptIdentifier;
            if (exceptIdentifier) {
                if (next.getType() != SourceType.IDENTIFIER) {
                    throw new AnalysisDeclareException(next.getPosition(), "except identifier");
                }
            } else {
                if (GET_MEMBER.equals(next.getValue())) {
                    continue;
                } else {
                    throw new AnalysisDeclareException(next.getPosition(), "except " + GET_MEMBER);
                }
            }
            // identifier
            // null for not find
            // package
            // source
            // compile
            // not file name equals dictionary
            cur = filterFile(cur, next.getValue());
            if (cur == null) {
                throw new AnalysisDeclareException(next.getPosition(), "not find");
            }
            if (!cur.exists()) {
                throw new AnalysisDeclareException(next.getPosition(), "can not find resource");
            } else if (cur.isFile()) {
                return cur;
            } else if (cur.isDirectory()) {
                continue;
            }
        }
        throw new AnalysisDeclareException(iterator.previous().getPosition(), "can not find resource");
    }

    private File filterFile(File cur, String value) {
        return null;
    }
}
