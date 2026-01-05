package com.github.jackieonway.copier.processor.extractor;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.model.CopyFieldConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * AnnotationExtractor 单元测试。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class AnnotationExtractorTest {

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Elements elementUtils;

    @Mock
    private Types typeUtils;

    @Mock
    private Messager messager;

    @Mock
    private TypeElement targetType;

    private ProcessorContext context;
    private AnnotationExtractor extractor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getMessager()).thenReturn(messager);
        context = new ProcessorContext(processingEnv);
        extractor = new AnnotationExtractor(context);
    }

    // ========== extractIgnoreFields 测试 ==========

    @Test
    public void extractIgnoreFields_withValidFields_shouldReturnAllFields() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.ignore()).thenReturn(new String[]{"field1", "field2", "field3"});

        Set<String> result = extractor.extractIgnoreFields(annotation);

        assertEquals(3, result.size());
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2"));
        assertTrue(result.contains("field3"));
    }

    @Test
    public void extractIgnoreFields_withEmptyArray_shouldReturnEmptySet() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.ignore()).thenReturn(new String[]{});

        Set<String> result = extractor.extractIgnoreFields(annotation);

        assertTrue(result.isEmpty());
    }

    @Test
    public void extractIgnoreFields_withNullArray_shouldReturnEmptySet() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.ignore()).thenReturn(null);

        Set<String> result = extractor.extractIgnoreFields(annotation);

        assertTrue(result.isEmpty());
    }

    @Test
    public void extractIgnoreFields_withBlankFields_shouldFilterThem() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.ignore()).thenReturn(new String[]{"field1", "", "  ", "field2"});

        Set<String> result = extractor.extractIgnoreFields(annotation);

        assertEquals(2, result.size());
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2"));
    }

    // ========== extractComponentModel 测试 ==========

    @Test
    public void extractComponentModel_withDefault_shouldReturnDefault() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.componentModel()).thenReturn(ComponentModel.DEFAULT);

        ComponentModel result = extractor.extractComponentModel(annotation);

        assertEquals(ComponentModel.DEFAULT, result);
    }

    @Test
    public void extractComponentModel_withSpring_shouldReturnSpring() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.componentModel()).thenReturn(ComponentModel.SPRING);

        ComponentModel result = extractor.extractComponentModel(annotation);

        assertEquals(ComponentModel.SPRING, result);
    }

    @Test
    public void extractComponentModel_withCdi_shouldReturnCdi() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.componentModel()).thenReturn(ComponentModel.CDI);

        ComponentModel result = extractor.extractComponentModel(annotation);

        assertEquals(ComponentModel.CDI, result);
    }

    @Test
    public void extractComponentModel_withJsr330_shouldReturnJsr330() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.componentModel()).thenReturn(ComponentModel.JSR330);

        ComponentModel result = extractor.extractComponentModel(annotation);

        assertEquals(ComponentModel.JSR330, result);
    }

    @Test
    public void extractComponentModel_withNull_shouldReturnDefault() {
        CopyTarget annotation = mock(CopyTarget.class);
        when(annotation.componentModel()).thenReturn(null);

        ComponentModel result = extractor.extractComponentModel(annotation);

        assertEquals(ComponentModel.DEFAULT, result);
    }

    // ========== extractCopyFieldConfig 测试 ==========

    @Test
    public void extractCopyFieldConfig_withNull_shouldReturnNull() {
        CopyFieldConfig result = extractor.extractCopyFieldConfig(null);

        assertNull(result);
    }

    @Test
    public void extractCopyFieldConfig_withValidAnnotation_shouldExtractAllFields() {
        CopyField annotation = mock(CopyField.class);
        when(annotation.source()).thenReturn(new String[]{"sourceField"});
        when(annotation.target()).thenReturn("targetField");
        when(annotation.expression()).thenReturn("source.getValue()");
        when(annotation.qualifiedByName()).thenReturn("convertMethod");
        when(annotation.format()).thenReturn("yyyy-MM-dd");
        // Mock converter to return None class directly using doReturn
        doReturn(com.github.jackieonway.copier.converter.TypeConverter.None.class).when(annotation).converter();

        CopyFieldConfig result = extractor.extractCopyFieldConfig(annotation);

        assertNotNull(result);
        assertArrayEquals(new String[]{"sourceField"}, result.getSourceNames());
        assertEquals("targetField", result.getTarget());
        assertEquals("source.getValue()", result.getExpression());
        assertEquals("convertMethod", result.getQualifiedByName());
        assertEquals("yyyy-MM-dd", result.getFormat());
        assertNull(result.getConverterClassName()); // None converter should be null
    }

    @Test
    public void extractCopyFieldConfig_withEmptyValues_shouldHandleCorrectly() {
        CopyField annotation = mock(CopyField.class);
        when(annotation.source()).thenReturn(new String[]{});
        when(annotation.target()).thenReturn("");
        when(annotation.expression()).thenReturn("");
        when(annotation.qualifiedByName()).thenReturn("");
        when(annotation.format()).thenReturn("");
        doReturn(com.github.jackieonway.copier.converter.TypeConverter.None.class).when(annotation).converter();

        CopyFieldConfig result = extractor.extractCopyFieldConfig(annotation);

        assertNotNull(result);
        assertFalse(result.hasSourceNames());
        assertFalse(result.hasTarget());
        assertFalse(result.hasExpression());
        assertFalse(result.hasQualifiedByName());
        assertFalse(result.hasFormat());
        assertFalse(result.hasConverter());
    }

    // ========== validateCopyFieldConfig 测试 ==========

    @Test
    public void validateCopyFieldConfig_withNull_shouldReturnTrue() {
        boolean result = extractor.validateCopyFieldConfig(null);

        assertTrue(result);
    }

    @Test
    public void validateCopyFieldConfig_withManyToOneWithoutExpression_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1", "field2"}, // 多对一
                null,
                "", // 没有表达式
                null,
                null,
                null
        );

        boolean result = extractor.validateCopyFieldConfig(config);

        assertFalse(result);
    }

    @Test
    public void validateCopyFieldConfig_withManyToOneWithExpression_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1", "field2"}, // 多对一
                null,
                "source.getField1() + source.getField2()", // 有表达式
                null,
                null,
                null
        );

        boolean result = extractor.validateCopyFieldConfig(config);

        assertTrue(result);
    }

    @Test
    public void validateCopyFieldConfig_withSingleSource_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1"},
                null,
                null,
                null,
                null,
                null
        );

        boolean result = extractor.validateCopyFieldConfig(config);

        assertTrue(result);
    }
}
