package org.harvey.compiler.type.transform;


/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 21:07
 */
public interface CallableSignatureMatcher {

    /**
     * @return true for should
     */
    boolean shouldDeduplication(CallableSignature one, CallableSignature another);

    /**
     * @return -1 for can , else for not match param index(of another)
     */
    int match(CallableSignature to, CallableSignature from);
}
