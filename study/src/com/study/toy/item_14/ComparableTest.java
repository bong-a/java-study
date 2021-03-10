package com.study.toy.item_14;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class ComparableTest {
    public static void main(String[] args) {
        String[] input = new String[] {"b", "c", "z", "a", "v", "b"};
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, input);

        /*알파벳 순으로 출력*/
        System.out.println(s);
    }
}
