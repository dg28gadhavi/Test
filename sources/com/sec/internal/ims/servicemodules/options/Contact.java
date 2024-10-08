package com.sec.internal.ims.servicemodules.options;

import java.util.ArrayList;
import java.util.Iterator;

public class Contact {
    private String mContactId;
    private ArrayList<ContactNumber> mContactNumberList = new ArrayList<>();
    private Object mContactNumberListLock = new Object();
    private String mName = null;
    private String mRawContactId = null;

    public static class ContactNumber {
        String mContactNormalizedNumber;
        String mContactNumber;

        public ContactNumber(String str, String str2) {
            this.mContactNumber = str;
            this.mContactNormalizedNumber = str2;
        }

        public String getNumber() {
            return this.mContactNumber;
        }

        public String getNormalizedNumber() {
            return this.mContactNormalizedNumber;
        }
    }

    public Contact(String str, String str2) {
        this.mContactId = str;
        this.mRawContactId = str2;
    }

    public String getId() {
        return this.mContactId;
    }

    public void setId(String str) {
        this.mContactId = str;
    }

    public String getRawId() {
        return this.mRawContactId;
    }

    public void setName(String str) {
        this.mName = str;
    }

    public String getName() {
        return this.mName;
    }

    public void insertContactNumberIntoList(ContactNumber contactNumber) {
        synchronized (this.mContactNumberListLock) {
            try {
                Iterator<ContactNumber> it = this.mContactNumberList.iterator();
                while (it.hasNext()) {
                    if (it.next().getNumber().equals(contactNumber.getNumber())) {
                        return;
                    }
                }
                this.mContactNumberList.add(contactNumber);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<ContactNumber> getContactNumberList() {
        ArrayList<ContactNumber> arrayList;
        synchronized (this.mContactNumberListLock) {
            arrayList = (ArrayList) this.mContactNumberList.clone();
        }
        return arrayList;
    }

    public int hashCode() {
        String str = this.mContactId;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        String str2 = this.mRawContactId;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Contact contact = (Contact) obj;
        String str = this.mContactId;
        if (str == null) {
            if (contact.mContactId != null) {
                return false;
            }
        } else if (!str.equals(contact.mContactId)) {
            return false;
        }
        String str2 = this.mRawContactId;
        if (str2 == null) {
            if (contact.mRawContactId != null) {
                return false;
            }
        } else if (!str2.equals(contact.mRawContactId)) {
            return false;
        }
        return true;
    }
}
