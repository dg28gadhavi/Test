package com.sec.internal.constants.ims;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class XmlElement {
    public List<Attribute> mAttributes;
    public List<XmlElement> mChildElements;
    public String mName;
    public String mNamespace;
    public String mValue;

    public static class Attribute {
        public String mName;
        public String mNamespace;
        public String mValue;

        public Attribute(String str, String str2) {
            this.mName = str;
            this.mValue = str2;
            this.mNamespace = null;
        }

        public Attribute(String str, String str2, String str3) {
            this.mName = str;
            this.mValue = str2;
            this.mNamespace = str3;
        }

        public int hashCode() {
            String str = this.mName;
            int i = 0;
            int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
            String str2 = this.mValue;
            int hashCode2 = (hashCode + (str2 == null ? 0 : str2.hashCode())) * 31;
            String str3 = this.mNamespace;
            if (str3 != null) {
                i = str3.hashCode();
            }
            return hashCode2 + i;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Attribute attribute = (Attribute) obj;
            String str = this.mName;
            if (str == null) {
                if (attribute.mName != null) {
                    return false;
                }
            } else if (!str.equals(attribute.mName)) {
                return false;
            }
            String str2 = this.mValue;
            if (str2 == null) {
                if (attribute.mValue != null) {
                    return false;
                }
            } else if (!str2.equals(attribute.mValue)) {
                return false;
            }
            String str3 = this.mNamespace;
            if (str3 == null) {
                if (attribute.mNamespace != null) {
                    return false;
                }
            } else if (!str3.equals(attribute.mNamespace)) {
                return false;
            }
            return true;
        }
    }

    public XmlElement(String str) {
        this.mName = str;
        this.mValue = null;
        this.mNamespace = null;
        this.mAttributes = new ArrayList();
        this.mChildElements = new ArrayList();
    }

    public XmlElement(String str, String str2) {
        this(str);
        this.mValue = str2;
    }

    public XmlElement(String str, String str2, String str3) {
        this(str, str2);
        this.mNamespace = str3;
    }

    public XmlElement setValue(String str) {
        this.mValue = str;
        return this;
    }

    public XmlElement setValue(int i) {
        this.mValue = Integer.toString(i);
        return this;
    }

    public XmlElement setNamespace(String str) {
        this.mNamespace = str;
        return this;
    }

    public XmlElement addAttribute(String str, String str2) {
        if (!TextUtils.isEmpty(str2)) {
            this.mAttributes.add(new Attribute(str, str2));
        }
        return this;
    }

    public XmlElement addAttribute(String str, String str2, String str3) {
        if (!TextUtils.isEmpty(str2)) {
            this.mAttributes.add(new Attribute(str, str2, str3));
        }
        return this;
    }

    public XmlElement addChildElement(XmlElement xmlElement) {
        this.mChildElements.add(xmlElement);
        return this;
    }

    public XmlElement addChildElements(List<XmlElement> list) {
        this.mChildElements.addAll(list);
        return this;
    }

    public XmlElement setParent(XmlElement xmlElement) {
        return xmlElement.addChildElement(this);
    }

    public int hashCode() {
        String str = this.mName;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        String str2 = this.mValue;
        int hashCode2 = (hashCode + (str2 == null ? 0 : str2.hashCode())) * 31;
        String str3 = this.mNamespace;
        int hashCode3 = (hashCode2 + (str3 == null ? 0 : str3.hashCode())) * 31;
        List<Attribute> list = this.mAttributes;
        int hashCode4 = (hashCode3 + (list == null ? 0 : list.hashCode())) * 31;
        List<XmlElement> list2 = this.mChildElements;
        if (list2 != null) {
            i = list2.hashCode();
        }
        return hashCode4 + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        XmlElement xmlElement = (XmlElement) obj;
        String str = this.mName;
        if (str == null) {
            if (xmlElement.mName != null) {
                return false;
            }
        } else if (!str.equals(xmlElement.mName)) {
            return false;
        }
        String str2 = this.mValue;
        if (str2 == null) {
            if (xmlElement.mValue != null) {
                return false;
            }
        } else if (!str2.equals(xmlElement.mValue)) {
            return false;
        }
        String str3 = this.mNamespace;
        if (str3 == null) {
            if (xmlElement.mNamespace != null) {
                return false;
            }
        } else if (!str3.equals(xmlElement.mNamespace)) {
            return false;
        }
        List<Attribute> list = this.mAttributes;
        if (list == null) {
            if (xmlElement.mAttributes != null) {
                return false;
            }
        } else if (!list.equals(xmlElement.mAttributes)) {
            return false;
        }
        List<XmlElement> list2 = this.mChildElements;
        if (list2 == null) {
            if (xmlElement.mChildElements != null) {
                return false;
            }
        } else if (!list2.equals(xmlElement.mChildElements)) {
            return false;
        }
        return true;
    }
}
