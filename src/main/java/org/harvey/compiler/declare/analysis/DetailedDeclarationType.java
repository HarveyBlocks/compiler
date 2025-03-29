package org.harvey.compiler.declare.analysis;


/**
 * 详细的
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 22:58
 */
public enum DetailedDeclarationType {
    FILE_INTERFACE,
    FIELD,
    METHOD,
    OPERATOR,
    INNER_STRUCTURE,
    FUNCTION,
    INTERFACE_METHOD,
    INTERFACE_OPERATOR,
    INTERFACE_FIELD,
    INTERFACE_INNER_STRUCTURE,
    STRUCTURE_INNER_INTERFACE,
    FILE_STRUCT,
    STRUCT_FIELD,
    STRUCT_METHOD,
    STRUCT_OPERATOR,
    CALLABLE,
    STRUCTURE,
    GENERIC_DEFINITION,
    FILE_STRUCTURE, INTERFACE_METHOD_OR_OPERATOR, STRUCT_METHOD_OR_OPERATOR;


    /**
     * @param beInterface true for interface, false for other structure
     * @param atFile      true for at file, false for at structure
     * @param atInterface true for at interface, false for at other structure
     * @see Embellish#defaultWord(DetailedDeclarationType)
     */
    public static DetailedDeclarationType onDefaultForStructure(
            boolean beInterface, boolean atFile,
            boolean atInterface) {
        if (atFile) {
            if (beInterface) {
                return DetailedDeclarationType.FILE_INTERFACE;
            } else {
                return DetailedDeclarationType.FILE_STRUCTURE;
            }
        }
        if (atInterface) {
            return DetailedDeclarationType.INTERFACE_INNER_STRUCTURE;
        } else {
            if (beInterface) {
                return DetailedDeclarationType.STRUCTURE_INNER_INTERFACE;
            } else {
                return null;
            }
        }
    }

    /**
     * @param memberType  0 for structure, 1 for method or operator, 2 for field
     * @param environment for in interface or for in struct(not structure)
     * @return nullable
     * @see Embellish#defaultWord(DetailedDeclarationType)
     */
    public static DetailedDeclarationType onDefaultForMember(int memberType, Environment environment) {
        if (environment == Environment.INTERFACE) {
            switch (memberType) {
                case 0:
                    return DetailedDeclarationType.INTERFACE_INNER_STRUCTURE;
                case 1:
                    return DetailedDeclarationType.INTERFACE_METHOD_OR_OPERATOR;
                case 3:
                    return DetailedDeclarationType.INTERFACE_FIELD;
                default:
                    return null;
            }
        } else if (environment == Environment.STRUCT) {
            switch (memberType) {
                case 1:
                    return DetailedDeclarationType.STRUCT_FIELD;
                case 2:
                    return DetailedDeclarationType.STRUCT_METHOD_OR_OPERATOR;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param memberType 0 for structure(except interface), 1 for function, 2 for interface
     */
    public static DetailedDeclarationType onIllegalInFile(int memberType) {
        switch (memberType) {
            case 0:
                return DetailedDeclarationType.FILE_STRUCTURE;
            case 1:
                return DetailedDeclarationType.FUNCTION;
            case 2:
                return DetailedDeclarationType.FILE_INTERFACE;
            default:
                return null;
        }
    }

    /**
     * @param memberType 0 for inner structure,1 for field, 2 for method, 3 for operator
     * @return nullable
     * @see Embellish#defaultWord(DetailedDeclarationType)
     */
    public static DetailedDeclarationType onIllegalInStructure(int memberType) {
        switch (memberType) {
            case 0:
                return DetailedDeclarationType.INNER_STRUCTURE;
            case 1:
                return DetailedDeclarationType.FIELD;
            case 2:
                return DetailedDeclarationType.METHOD;
            case 3:
                return DetailedDeclarationType.OPERATOR;
            default:
                return null;
        }
    }

}
