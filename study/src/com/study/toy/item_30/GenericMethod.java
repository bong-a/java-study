package com.study.toy.item_30;

import java.util.HashSet;
import java.util.Set;

public class GenericMethod {
    public static void main(String[] args) {

    }

    public static Set union2(Set s1, Set s2) {
        Set result = new HashSet(s1);
        result.addAll(s2);
        return result;
    }

    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> result = new HashSet<>(s1);
        result.addAll(s2);
        return result;
    }
}
