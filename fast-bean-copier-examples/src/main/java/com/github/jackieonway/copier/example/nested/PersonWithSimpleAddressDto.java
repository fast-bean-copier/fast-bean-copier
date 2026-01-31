package com.github.jackieonway.copier.example.nested;

import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = PersonWithSimpleAddress.class)
public class PersonWithSimpleAddressDto {
    private Long id;
    private String name;
    private SimpleAddressDto address;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SimpleAddressDto getAddress() { return address; }
    public void setAddress(SimpleAddressDto address) { this.address = address; }
}
