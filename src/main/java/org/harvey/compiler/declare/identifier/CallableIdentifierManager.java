package org.harvey.compiler.declare.identifier;

import lombok.Getter;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 20:51
 */
public class CallableIdentifierManager implements IdentifierManager {
    private final IdentifierManager identifierManager;
    private final Map<String, Integer> genericMap;
    @Getter
    private final boolean[] visited;

    private boolean visitedGenericCheck;

    public CallableIdentifierManager(Map<String, Integer> genericMap, IdentifierManager identifierManager) {
        this.genericMap = genericMap;
        this.identifierManager = identifierManager;
        this.visited = new boolean[genericMap.size()];
    }

    public static ReferenceElement getFromDeclare(SourcePosition position, ReferenceElement referenceElement) {
        if (referenceElement.getType() == ReferenceType.CALLABLE_GENERIC_IDENTIFIER &&
            referenceElement.getPosition() == null) {
            return new ReferenceElement(position, referenceElement.getType(), referenceElement.getReference());
        }
        return referenceElement;
    }

    @Override
    public ReferenceElement getReferenceAndAddIfNotExist(FullIdentifierString fullname) {
        ReferenceElement genericReference = tryGeneric(fullname.getPosition(), fullname.getFullname());
        if (genericReference != null) {
            return genericReference;
        }
        return identifierManager.getReferenceAndAddIfNotExist(fullname);
    }

    @Override
    public boolean isImport(FullIdentifierString fullname) {
        return identifierManager.isImport(fullname);
    }

    @Override
    public int getPreLength() {
        return identifierManager.getPreLength();
    }

    @Override
    public IdentifierString getGenericIdentifier(ReferenceElement reference) {
        return identifierManager.getGenericIdentifier(reference);
    }

    private ReferenceElement tryGeneric(SourcePosition position, String[] fullname) {
        if (fullname.length != 1) {
            return null;
        }
        String mayGeneric = fullname[0];
        Integer genericReference = this.genericMap.get(mayGeneric);
        if (genericReference == null) {
            return null;
        }
        visited[genericReference] = true;
        return new ReferenceElement(
                position,
                ReferenceType.CALLABLE_GENERIC_IDENTIFIER,
                genericReference
        );
    }

    @Override
    public boolean isImport(ReferenceElement reference) {
        return identifierManager.isImport(reference);
    }

    @Override
    public boolean isDeclarationInFile(ReferenceElement reference) {
        return identifierManager.isDeclarationInFile(reference);
    }

    @Override
    public boolean afterRead() {
        return identifierManager.afterRead();
    }

    /**
     * {@link #getFromDeclare(SourcePosition, ReferenceElement)} warp
     */
    @Override
    public ReferenceElement getFromDeclare(String[] fullname) {
        ReferenceElement genericReference = tryGeneric(null, fullname);
        if (genericReference != null) {
            return genericReference;
        }
        return identifierManager.getFromDeclare(fullname);
    }

    @Override
    public FullIdentifierString getIdentifier(ReferenceElement reference) {
        return identifierManager.getIdentifier(reference);
    }

    @Override
    public int getImportReferenceAfterIndex() {
        return identifierManager.getImportReferenceAfterIndex();
    }

    @Override
    public List<FullIdentifierString> getAllIdentifierTable() {
        return identifierManager.getAllIdentifierTable();
    }

    @Override
    public Map<String, ImportString> getImportTable() {
        return identifierManager.getImportTable();
    }

    public void openVisitedGenericCheck() {
        Arrays.fill(this.visited, false);
        this.visitedGenericCheck = true;
    }

    public void closeVisitedGenericCheck() {
        this.visitedGenericCheck = false;
    }


    /**
     * @return -2 for not open, -1 for not find
     */
    public int notVisitedGeneric() {
        if (!visitedGenericCheck) {
            return -2;
        }
        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) {
                return i;
            }
        }
        return -1;
    }
}
