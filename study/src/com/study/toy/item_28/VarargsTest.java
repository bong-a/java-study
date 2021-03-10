package com.study.toy.item_28;

import java.util.Collections;
import java.util.List;

public class VarargsTest {
    public static void main(String[] args) {

    }
    @SafeVarargs
    static void dangerous(List<String>... stringLists){
        List<Integer> integerList = Collections.singletonList(42);
        Object[] objects = stringLists;
        objects[0] = integerList;           //힙 오염 발생
        String s = stringLists[0].get(0);   //ClasCastException
    }
}
