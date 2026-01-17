package com.github.jackieonway.copier.example.container;

/**
 * 库存状态转换器 - 用于测试容器模式下的 uses 属性。
 * 将库存数量转换为状态描述。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class StockStatusConverter {

    /**
     * 将库存数量转换为状态描述。
     *
     * @param stock 库存数量
     * @return 状态描述
     */
    public String toStatus(Integer stock) {
        if (stock == null || stock == 0) {
            return "OUT_OF_STOCK";
        } else if (stock < 10) {
            return "LOW_STOCK";
        } else if (stock < 100) {
            return "IN_STOCK";
        } else {
            return "HIGH_STOCK";
        }
    }
}
