package com.hospital.model;

/**
 * Abstract base for every human in the system. Patient and Doctor both extend
 * it, which is what lets Hospital treat them uniformly where it needs to
 * (printing, validating shared fields) while keeping their differences apart.
 */
public abstract class Person {

    private final String id;
    private String name;
    private int age;
    private String gender;
    private String phone;

    protected Person(String id, String name, int age, String gender, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Label used in menus and reports. Forces every subclass to identify itself. */
    public abstract String getRole();

    /** Serialised form written by FileManager, one record per line. */
    public abstract String toCsv();

    @Override
    public String toString() {
        return String.format("%-6s %-20s %-5d %-8s %-12s", id, name, age, gender, phone);
    }
}
