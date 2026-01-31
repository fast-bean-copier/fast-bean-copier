package com.github.jackieonway.copier.example.nested;

public class PersonWithSimpleAddress {
    private Long id;
    private String name;
    private SimpleAddress address;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SimpleAddress getAddress() { return address; }
    public void setAddress(SimpleAddress address) { this.address = address; }
}
