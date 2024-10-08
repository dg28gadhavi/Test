package com.sec.internal.constants.ims.servicemodules.im;

import android.util.SparseArray;
import com.sec.internal.constants.ims.servicemodules.im.IEnumerationWithId;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import java.lang.Enum;

public class ReverseEnumMap<E extends Enum<E> & IEnumerationWithId<E>> {
    private final SparseArray<E> map = new SparseArray<>();

    public ReverseEnumMap(Class<E> cls) {
        if (cls.getEnumConstants() != null) {
            Enum[] enumArr = (Enum[]) cls.getEnumConstants();
            int length = enumArr.length;
            int i = 0;
            while (i < length) {
                Enum enumR = enumArr[i];
                IEnumerationWithId iEnumerationWithId = (IEnumerationWithId) enumR;
                Enum enumR2 = (Enum) this.map.get(iEnumerationWithId.getId());
                if (enumR2 == null) {
                    this.map.put(iEnumerationWithId.getId(), enumR);
                    i++;
                } else {
                    throw new IllegalStateException(Constants.ID + iEnumerationWithId.getId() + " already set to constant " + enumR2.name());
                }
            }
            return;
        }
        throw new IllegalStateException("Trying to make ReverseEnumMap with non-enum class: " + cls);
    }

    public E get(Integer num) {
        E e = (Enum) this.map.get(num.intValue());
        if (e != null) {
            return e;
        }
        throw new IllegalArgumentException("Id " + num + " unknown in reverse enumeration map");
    }
}
