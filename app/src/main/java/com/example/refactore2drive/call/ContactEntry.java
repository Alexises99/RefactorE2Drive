package com.example.refactore2drive.call;

import com.example.refactore2drive.R;
import com.example.refactore2drive.controlpanel.InfoEntry;

import java.util.ArrayList;
import java.util.List;

public class ContactEntry {
    public String name;
    public String number;
    public int resourceId;

    public ContactEntry(String name, String number) {
        this.name = name;
        this.number = number;
        this.resourceId = R.drawable.ic_person;
    }

    public static List<ContactEntry> initList() {
        List<ContactEntry> list = new ArrayList<>();
        ContactEntry c1 = new ContactEntry("React", "123123123");
        ContactEntry c2 = new ContactEntry("Express", "123123123");
        ContactEntry c3 = new ContactEntry("JavaScript", "123123123");
        ContactEntry c4 = new ContactEntry("Java", "123123123h");
        list.add(c1);
        list.add(c2);
        list.add(c3);
        list.add(c4);
        return list;
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
