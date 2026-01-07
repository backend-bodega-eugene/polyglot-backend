//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.util;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import java.io.Serializable;

@Data
public abstract class TreeEntity<T> implements Serializable {
    private String id;
    @JsonBackReference
    protected T parent;
    protected String parentIds;
    protected String name;
    protected Integer sort;
    protected Integer level;
    protected String parentId;
    protected boolean leaf;
    protected boolean expanded;
    protected boolean loaded;

    public TreeEntity() {
    }

    public TreeEntity(String id) {
        this.id = id;
    }
}
