package com.study.toy.item_32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VarArgsTest {
    static void dangerous(List<String>... stringLists) {
        List<Integer> integers = Arrays.asList(42);
        Object[] objects = stringLists;
        objects[0] = integers;              // 힙 오염 발생
        String s = stringLists[0].get(0);   // ClassCastException
    }

    static <T> T[] toArray(T... args) {
        return args;
    }

    public static void main(String[] args) {
        //dangerous(Arrays.asList("a", "b", "c"));
        //String[] attributes = pickTwo("좋은", "빠른", "저렴한");
        List<String> attributes = pickTwo("좋은", "빠른", "저렴한");
    }

    /*static <T> T[] pickTwo(T a, T b, T c) {
        Random random = new Random();
        switch (random.nextInt(3)) {
            case 0:
                return toArray(a, b);
            case 1:
                return toArray(a, c);
            case 2:
                return toArray(b, c);
        }
        throw new AssertionError();
    }*/
    static <T> List<T> pickTwo(T a, T b, T c) {
        Random random = new Random();
        Arrays.asList();
        switch (random.nextInt(3)) {
            case 0:
                return Arrays.asList(a, b);
            case 1:
                return Arrays.asList(a, c);
            case 2:
                return Arrays.asList(b, c);
        }
        throw new AssertionError();
    }

    @SafeVarargs
    static <T> List<T> flatten(List<? extends T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists) {
            result.addAll(list);
        }
        return result;
    }

    static <T> List<T> flatten(List<List<? extends T>> lists) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists) {
            result.addAll(list);
        }
        return result;
    }
}
