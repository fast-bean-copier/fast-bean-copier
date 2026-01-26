package com.github.jackieonway.copier.processor.config;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * PropertiesConfigLoader 单元测试。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class PropertiesConfigLoaderTest {

    @Mock
    private Filer filer;

    @Mock
    private FileObject fileObject;

    private PropertiesConfigLoader loader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        loader = new PropertiesConfigLoader(filer);
    }

    // ========== 配置文件读取测试 ==========

    @Test
    public void testLoadConfig_fileExists() throws IOException {
        // 准备配置文件内容
        String configContent = "fast.bean.copier.componentModel=SPRING\n" +
                "fast.bean.copier.nullValueStrategy=IGNORE\n";
        InputStream inputStream = new ByteArrayInputStream(configContent.getBytes());

        // Mock Filer 返回文件对象
        when(filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq("fast-bean-copier.properties")))
                .thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(inputStream);

        // 执行加载
        Properties props = loader.loadConfig();

        // 验证结果
        assertNotNull(props);
        assertEquals("SPRING", props.getProperty("fast.bean.copier.componentModel"));
        assertEquals("IGNORE", props.getProperty("fast.bean.copier.nullValueStrategy"));
    }

    @Test
    public void testLoadConfig_fileNotExists() throws IOException {
        // Mock Filer 抛出 IOException（文件不存在）
        when(filer.getResource(any(), any(), any())).thenThrow(new IOException("File not found"));

        // 执行加载
        Properties props = loader.loadConfig();

        // 验证返回空 Properties
        assertNotNull(props);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testLoadConfig_fallbackPath() throws IOException {
        // 主路径不存在
        when(filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq("fast-bean-copier.properties")))
                .thenThrow(new IOException("File not found"));

        // 备选路径存在
        String configContent = "fast.bean.copier.componentModel=CDI\n";
        InputStream inputStream = new ByteArrayInputStream(configContent.getBytes());
        when(filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq("META-INF/fast-bean-copier.properties")))
                .thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(inputStream);

        // 执行加载
        Properties props = loader.loadConfig();

        // 验证结果
        assertNotNull(props);
        assertEquals("CDI", props.getProperty("fast.bean.copier.componentModel"));
    }

    // ========== componentModel 解析测试 ==========

    @Test
    public void testParseComponentModel_validValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "SPRING");

        String result = loader.parseComponentModel(props);

        assertEquals("SPRING", result);
    }

    @Test
    public void testParseComponentModel_defaultValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "DEFAULT");

        String result = loader.parseComponentModel(props);

        assertEquals("DEFAULT", result);
    }

    @Test
    public void testParseComponentModel_cdiValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "CDI");

        String result = loader.parseComponentModel(props);

        assertEquals("CDI", result);
    }

    @Test
    public void testParseComponentModel_jsr330Value() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "JSR330");

        String result = loader.parseComponentModel(props);

        assertEquals("JSR330", result);
    }

    @Test
    public void testParseComponentModel_notConfigured() {
        Properties props = new Properties();

        String result = loader.parseComponentModel(props);

        assertNull(result);
    }

    @Test
    public void testParseComponentModel_emptyValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "");

        String result = loader.parseComponentModel(props);

        assertNull(result);
    }

    @Test
    public void testParseComponentModel_invalidValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "INVALID");

        String result = loader.parseComponentModel(props);

        // 无效值应返回 null
        assertNull(result);
    }

    @Test
    public void testParseComponentModel_caseInsensitive() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.componentModel", "spring");

        String result = loader.parseComponentModel(props);

        // 应该转换为大写
        assertEquals("SPRING", result);
    }

    // ========== nullValueStrategy 解析测试 ==========

    @Test
    public void testParseNullValueStrategy_ignoreValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.nullValueStrategy", "IGNORE");

        String result = loader.parseNullValueStrategy(props);

        assertEquals("IGNORE", result);
    }

    @Test
    public void testParseNullValueStrategy_setNullValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.nullValueStrategy", "SET_NULL");

        String result = loader.parseNullValueStrategy(props);

        assertEquals("SET_NULL", result);
    }

    @Test
    public void testParseNullValueStrategy_throwExceptionValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.nullValueStrategy", "THROW_EXCEPTION");

        String result = loader.parseNullValueStrategy(props);

        assertEquals("THROW_EXCEPTION", result);
    }

    @Test
    public void testParseNullValueStrategy_notConfigured() {
        Properties props = new Properties();

        String result = loader.parseNullValueStrategy(props);

        assertNull(result);
    }

    @Test
    public void testParseNullValueStrategy_emptyValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.nullValueStrategy", "");

        String result = loader.parseNullValueStrategy(props);

        assertNull(result);
    }

    @Test
    public void testParseNullValueStrategy_invalidValue() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.nullValueStrategy", "INVALID");

        String result = loader.parseNullValueStrategy(props);

        // 无效值应返回 null
        assertNull(result);
    }

    @Test
    public void testParseNullValueStrategy_caseInsensitive() {
        Properties props = new Properties();
        props.setProperty("fast.bean.copier.nullValueStrategy", "ignore");

        String result = loader.parseNullValueStrategy(props);

        // 应该转换为大写
        assertEquals("IGNORE", result);
    }

    // ========== 配置文件格式错误测试 ==========

    @Test
    public void testLoadConfig_malformedFile() throws IOException {
        // 准备格式错误的配置文件内容（完全无效的格式）
        String configContent = "this is not a valid properties file format\n" +
                "###invalid###\n";
        InputStream inputStream = new ByteArrayInputStream(configContent.getBytes());

        when(filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq("fast-bean-copier.properties")))
                .thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(inputStream);

        // 执行加载
        Properties props = loader.loadConfig();

        // Properties 会忽略格式错误的行，返回空或部分解析的结果
        assertNotNull(props);
        // 验证没有解析出我们期望的配置项
        assertNull(props.getProperty("fast.bean.copier.componentModel"));
    }
}
