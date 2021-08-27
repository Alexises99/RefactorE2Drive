package com.example.refactore2drive.call;

import androidx.annotation.NonNull;

import com.example.refactore2drive.R;

public class ContactEntry {
    public String name;
    public String number;
    public int resourceId;

    /**
     * Representa el modelo de un contacto
     * @param name nombre del contacto
     * @param number numero del contacto
     */
    public ContactEntry(String name, String number) {
        this.name = name;
        this.number = number;
        this.resourceId = R.drawable.ic_person;
    }

    @NonNull
    @Override
    public String toString() {
        return "ContactEntry{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", resourceId=" + resourceId +
                '}';
    }
}
