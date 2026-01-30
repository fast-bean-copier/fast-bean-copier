package com.github.jackieonway.copier.processor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * 嵌套对象类型兼容性测试。
 *
 * <p>测试 TypeUtils.isTypeCompatible() 方法对嵌套对象的兼容性判断。
 *
 * @author jackieonway
 * @since 1.3.2
 */
public class TypeUtilsNestedObjectTest {

    @Mock
    private DeclaredType addressType;

    @Mock
    private DeclaredType addressDtoType;

    @Mock
    private DeclaredType stringType;

    @Mock
    private DeclaredType dateType;

    @Mock
    private DeclaredType listType;

    @Mock
    private Element addressElement;

    @Mock
    private Element addressDtoElement;

    @Mock
    private Element stringElement;

    @Mock
    private Element dateElement;

    @Mock
    private Element listElement;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Mock Address type (custom object, no @CopyTarget)
        doReturn(TypeKind.DECLARED).when(addressType).getKind();
        doReturn("com.example.Address").when(addressType).toString();
        doReturn(addressElement).when(addressType).asElement();
        doReturn(null).when(addressElement).getAnnotation(com.github.jackieonway.copier.annotation.CopyTarget.class);

        // Mock AddressDto type (custom object, no @CopyTarget)
        doReturn(TypeKind.DECLARED).when(addressDtoType).getKind();
        doReturn("com.example.AddressDto").when(addressDtoType).toString();
        doReturn(addressDtoElement).when(addressDtoType).asElement();
        doReturn(null).when(addressDtoElement).getAnnotation(com.github.jackieonway.copier.annotation.CopyTarget.class);

        // Mock String type (JDK class)
        doReturn(TypeKind.DECLARED).when(stringType).getKind();
        doReturn("java.lang.String").when(stringType).toString();
        doReturn(stringElement).when(stringType).asElement();
        doReturn(null).when(stringElement).getAnnotation(com.github.jackieonway.copier.annotation.CopyTarget.class);

        // Mock Date type (JDK class)
        doReturn(TypeKind.DECLARED).when(dateType).getKind();
        doReturn("java.util.Date").when(dateType).toString();
        doReturn(dateElement).when(dateType).asElement();
        doReturn(null).when(dateElement).getAnnotation(com.github.jackieonway.copier.annotation.CopyTarget.class);

        // Mock List type (collection)
        doReturn(TypeKind.DECLARED).when(listType).getKind();
        doReturn("java.util.List<java.lang.String>").when(listType).toString();
        doReturn(listElement).when(listType).asElement();
        doReturn(null).when(listElement).getAnnotation(com.github.jackieonway.copier.annotation.CopyTarget.class);
    }

    @Test
    public void testDifferentCustomObjectsAreCompatible() {
        // Address and AddressDto are different types but both are custom objects
        // They should be compatible for nested object deep copy
        assertTrue("Different custom objects should be compatible",
                TypeUtils.isTypeCompatible(addressType, addressDtoType));
    }

    @Test
    public void testSameCustomObjectsAreCompatible() {
        // Same type should always be compatible
        assertTrue("Same custom objects should be compatible",
                TypeUtils.isTypeCompatible(addressType, addressType));
    }

    @Test
    public void testJdkClassesAreNotNestedObjects() {
        // String is a JDK class, not a nested object
        // String and custom object should not be compatible
        assertFalse("JDK class and custom object should not be compatible",
                TypeUtils.isTypeCompatible(stringType, addressType));
        assertFalse("Custom object and JDK class should not be compatible",
                TypeUtils.isTypeCompatible(addressType, stringType));
    }

    @Test
    public void testTwoJdkClassesAreNotCompatibleUnlessSame() {
        // Two different JDK classes should not be compatible
        assertFalse("Different JDK classes should not be compatible",
                TypeUtils.isTypeCompatible(stringType, dateType));

        // Same JDK class should be compatible
        assertTrue("Same JDK class should be compatible",
                TypeUtils.isTypeCompatible(stringType, stringType));
    }

    @Test
    public void testCollectionTypesNotAffected() {
        // Collection types should use their own compatibility logic
        // List<String> and Address should not be compatible
        assertFalse("Collection and custom object should not be compatible",
                TypeUtils.isTypeCompatible(listType, addressType));
    }

    @Test
    public void testNullTypesAreNotCompatible() {
        // Null types should not be compatible
        assertFalse("Null source should not be compatible",
                TypeUtils.isTypeCompatible(null, addressType));
        assertFalse("Null target should not be compatible",
                TypeUtils.isTypeCompatible(addressType, null));
        assertFalse("Both null should not be compatible",
                TypeUtils.isTypeCompatible(null, null));
    }
}
