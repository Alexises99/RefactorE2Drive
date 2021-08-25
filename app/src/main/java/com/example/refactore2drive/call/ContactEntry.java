package com.example.refactore2drive.call;

import com.example.refactore2drive.R;

public class ContactEntry {
    public String name;
    public String number;
    public int resourceId;

    public ContactEntry(String name, String number) {
        this.name = name;
        this.number = number;
        this.resourceId = R.drawable.ic_person;
    }

    @Override
    public String toString() {
        return "ContactEntry{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", resourceId=" + resourceId +
                '}';
    }
}
