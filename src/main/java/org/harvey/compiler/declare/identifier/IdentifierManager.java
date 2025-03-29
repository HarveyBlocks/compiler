package org.harvey.compiler.declare.identifier;


import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;

import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 22:56
 */
public interface IdentifierManager {
    IdentifierString getGenericIdentifier(ReferenceElement reference) ;

    ReferenceElement getReferenceAndAddIfNotExist(FullIdentifierString fullname);

    boolean isImport(FullIdentifierString fullname);

    int getPreLength();


    boolean isImport(ReferenceElement reference);


    boolean isDeclarationInFile(ReferenceElement index);

    /**
     * read only
     */
    boolean afterRead();

    /**
     * read only
     *
     * @return null for not found
     */
    ReferenceElement getFromDeclare(String[] fullname);

    FullIdentifierString getIdentifier(ReferenceElement reference);

    int getImportReferenceAfterIndex();

    List<FullIdentifierString> getAllIdentifierTable();

    Map<String, ImportString> getImportTable();
}
