package org.harvey.compiler.analysis.text.type;

import org.junit.Test;

public class ComplexStructureTypeSingletonTest {
    private static void test(ComplexStructureTypeSingleton type1, ComplexStructureTypeSingleton type2) {
        /*Assert.assertSame(type1 == type2, ComplexStructureTypeSingleton.get(type1.getType()) ==
                ComplexStructureTypeSingleton.get(type2.getType()));*/
    }

    @Test
    public void testGet() {
        test(ComplexStructureTypeSingleton.CLASS, ComplexStructureTypeSingleton.CLASS);
        test(ComplexStructureTypeSingleton.ENUM, ComplexStructureTypeSingleton.ENUM);
        test(ComplexStructureTypeSingleton.STRUCT, ComplexStructureTypeSingleton.STRUCT);
        test(ComplexStructureTypeSingleton.INTERFACE, ComplexStructureTypeSingleton.INTERFACE);
        test(ComplexStructureTypeSingleton.ABSTRACT_CLASS, ComplexStructureTypeSingleton.ABSTRACT_CLASS);


        test(ComplexStructureTypeSingleton.CLASS, ComplexStructureTypeSingleton.ENUM);
        test(ComplexStructureTypeSingleton.ENUM, ComplexStructureTypeSingleton.STRUCT);
        test(ComplexStructureTypeSingleton.STRUCT, ComplexStructureTypeSingleton.INTERFACE);
        test(ComplexStructureTypeSingleton.INTERFACE, ComplexStructureTypeSingleton.ABSTRACT_CLASS);
        test(ComplexStructureTypeSingleton.ABSTRACT_CLASS, ComplexStructureTypeSingleton.CLASS);


        test(ComplexStructureTypeSingleton.CLASS, ComplexStructureTypeSingleton.STRUCT);
        test(ComplexStructureTypeSingleton.ENUM, ComplexStructureTypeSingleton.INTERFACE);
        test(ComplexStructureTypeSingleton.STRUCT, ComplexStructureTypeSingleton.ABSTRACT_CLASS);
        test(ComplexStructureTypeSingleton.INTERFACE, ComplexStructureTypeSingleton.CLASS);
        test(ComplexStructureTypeSingleton.ABSTRACT_CLASS, ComplexStructureTypeSingleton.ENUM);


        test(ComplexStructureTypeSingleton.CLASS, ComplexStructureTypeSingleton.INTERFACE);
        test(ComplexStructureTypeSingleton.ENUM, ComplexStructureTypeSingleton.ABSTRACT_CLASS);
        test(ComplexStructureTypeSingleton.STRUCT, ComplexStructureTypeSingleton.CLASS);
        test(ComplexStructureTypeSingleton.INTERFACE, ComplexStructureTypeSingleton.ENUM);
        test(ComplexStructureTypeSingleton.ABSTRACT_CLASS, ComplexStructureTypeSingleton.STRUCT);


        test(ComplexStructureTypeSingleton.CLASS, ComplexStructureTypeSingleton.ABSTRACT_CLASS);
        test(ComplexStructureTypeSingleton.ENUM, ComplexStructureTypeSingleton.CLASS);
        test(ComplexStructureTypeSingleton.STRUCT, ComplexStructureTypeSingleton.ENUM);
        test(ComplexStructureTypeSingleton.INTERFACE, ComplexStructureTypeSingleton.STRUCT);
        test(ComplexStructureTypeSingleton.ABSTRACT_CLASS, ComplexStructureTypeSingleton.INTERFACE);
    }
}