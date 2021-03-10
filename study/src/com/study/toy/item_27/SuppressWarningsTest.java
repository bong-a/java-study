package com.study.toy.item_27;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuppressWarningsTest {
    private static int size;
    private static Object[] elements;

    public static void main(String[] args) {
        List<String> a = new ArrayList<>();
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] result = (T[])Arrays.copyOf(elements, size, a.getClass());
            return result;
        }
        System.arraycopy(elements, 0, a, 0, size);

        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
}
