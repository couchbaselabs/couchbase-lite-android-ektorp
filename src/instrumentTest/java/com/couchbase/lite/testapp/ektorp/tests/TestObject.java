package com.couchbase.lite.testapp.ektorp.tests;

import org.ektorp.support.OpenCouchDbDocument;

import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class TestObject extends OpenCouchDbDocument {

    private Integer foo;
    private Boolean bar;
    private String baz;
    private String status;
    private String key;
    private List<String> stuff;
    private Set<String> stuffSet;

    public Set<String> getStuffSet() {
        return stuffSet;
    }

    public void setStuffSet(Set<String> stuffSet) {
        this.stuffSet = stuffSet;
    }


    public List<String> getStuff() {
        return stuff;
    }

    public void setStuff(List<String> stuff) {
        this.stuff = stuff;
    }

    public Integer getFoo() {
        return foo;
    }

    public void setFoo(Integer foo) {
        this.foo = foo;
    }

    public Boolean getBar() {
        return bar;
    }

    public void setBar(Boolean bar) {
        this.bar = bar;
    }

    public String getBaz() {
        return baz;
    }

    public void setBaz(String baz) {
        this.baz = baz;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TestObject() {

    }

    public TestObject(Integer foo, Boolean bar, String baz) {
        this.foo = foo;
        this.bar = bar;
        this.baz = baz;
        this.status = null;
    }

    public TestObject(Integer foo, Boolean bar, String baz, String status) {
        this.foo = foo;
        this.bar = bar;
        this.baz = baz;
        this.status = status;
    }

    public TestObject(String id, String key) {
        this.setId(id);
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof TestObject) {
            TestObject other = (TestObject)o;
            if(getId() != null && other.getId() != null && getId().equals(other.getId())) {
                return true;
            }
        }
        return false;
    }

}
