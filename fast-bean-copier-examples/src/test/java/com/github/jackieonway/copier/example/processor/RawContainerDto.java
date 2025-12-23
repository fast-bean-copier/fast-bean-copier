package com.github.jackieonway.copier.example.processor;

import com.github.jackieonway.copier.annotation.CopyTarget;
import java.util.List;
import java.util.Map;

@CopyTarget(source = RawContainer.class)
public class RawContainerDto {

    private List rawList;
    private Map rawMap;
    private List<?> wildcardList;
    private Map<String, ?> wildcardMap;

    public List getRawList() {
        return rawList;
    }

    public void setRawList(List rawList) {
        this.rawList = rawList;
    }

    public Map getRawMap() {
        return rawMap;
    }

    public void setRawMap(Map rawMap) {
        this.rawMap = rawMap;
    }

    public List<?> getWildcardList() {
        return wildcardList;
    }

    public void setWildcardList(List<?> wildcardList) {
        this.wildcardList = wildcardList;
    }

    public Map<String, ?> getWildcardMap() {
        return wildcardMap;
    }

    public void setWildcardMap(Map<String, ?> wildcardMap) {
        this.wildcardMap = wildcardMap;
    }
}

