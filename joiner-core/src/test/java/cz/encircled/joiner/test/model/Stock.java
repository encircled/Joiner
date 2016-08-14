package cz.encircled.joiner.test.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vlad on 14-Aug-16.
 */
public class Stock implements java.io.Serializable {

    private Long stockId;

    private Map<String, Object> custom = new HashMap<>();

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Map getCustom() {
        return custom;
    }

    public void setCustom(Map custom) {
        this.custom = custom;
    }
}