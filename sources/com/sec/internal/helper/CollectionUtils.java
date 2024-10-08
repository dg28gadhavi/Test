package com.sec.internal.helper;

import android.content.ContentValues;
import android.text.TextUtils;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
    public static boolean isNullOrEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Object[] objArr) {
        return objArr == null || objArr.length == 0;
    }

    public static boolean isNullOrEmpty(ContentValues contentValues) {
        return contentValues == null || contentValues.size() == 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0009, code lost:
        r1 = r1.getAsBoolean(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean getBooleanValue(android.content.ContentValues r1, java.lang.String r2, boolean r3) {
        /*
            if (r1 == 0) goto L_0x0014
            boolean r0 = r1.containsKey(r2)
            if (r0 != 0) goto L_0x0009
            goto L_0x0014
        L_0x0009:
            java.lang.Boolean r1 = r1.getAsBoolean(r2)
            if (r1 != 0) goto L_0x0010
            goto L_0x0014
        L_0x0010:
            boolean r3 = r1.booleanValue()
        L_0x0014:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.CollectionUtils.getBooleanValue(android.content.ContentValues, java.lang.String, boolean):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0009, code lost:
        r1 = r1.getAsInteger(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getIntValue(android.content.ContentValues r1, java.lang.String r2, int r3) {
        /*
            if (r1 == 0) goto L_0x0014
            boolean r0 = r1.containsKey(r2)
            if (r0 != 0) goto L_0x0009
            goto L_0x0014
        L_0x0009:
            java.lang.Integer r1 = r1.getAsInteger(r2)
            if (r1 != 0) goto L_0x0010
            goto L_0x0014
        L_0x0010:
            int r3 = r1.intValue()
        L_0x0014:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.CollectionUtils.getIntValue(android.content.ContentValues, java.lang.String, int):int");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0009, code lost:
        r1 = r1.getAsString(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getStringValue(android.content.ContentValues r1, java.lang.String r2, java.lang.String r3) {
        /*
            if (r1 == 0) goto L_0x0011
            boolean r0 = r1.containsKey(r2)
            if (r0 != 0) goto L_0x0009
            goto L_0x0011
        L_0x0009:
            java.lang.String r1 = r1.getAsString(r2)
            if (r1 != 0) goto L_0x0010
            goto L_0x0011
        L_0x0010:
            r3 = r1
        L_0x0011:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.CollectionUtils.getStringValue(android.content.ContentValues, java.lang.String, java.lang.String):java.lang.String");
    }

    public static class ArrayListMultimap<K, V> {
        private Map<K, Collection<V>> map = new HashMap();

        public Collection<V> get(Object obj) {
            Collection<V> collection = this.map.get(obj);
            return collection == null ? new ArrayList() : collection;
        }

        public void put(K k, V v) {
            Collection collection = this.map.get(k);
            if (collection == null) {
                collection = new ArrayList();
            }
            collection.add(v);
            this.map.put(k, collection);
        }
    }

    public static <K, V> ArrayListMultimap<K, V> createArrayListMultimap() {
        return new ArrayListMultimap<>();
    }

    public static class Partition<T> extends AbstractList<List<T>> {
        final List<T> list;
        final int size;

        Partition(List<T> list2, int i) {
            this.list = list2;
            this.size = i;
        }

        public List<T> get(int i) {
            int i2 = this.size;
            int i3 = i * i2;
            return this.list.subList(i3, Math.min(i2 + i3, this.list.size()));
        }

        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        public int size() {
            int size2 = this.list.size();
            int i = this.size;
            if (size2 % i == 0) {
                return size2 / i;
            }
            return (size2 / i) + 1;
        }
    }

    public static <T> Partition<T> partition(List<T> list, int i) {
        return new Partition<>(list, i);
    }
}
