package com.study.toy.item_26;

import java.util.ArrayList;
import java.util.List;

public class RawTypeTest {
    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        //unsafeAdd(strings,Integer.valueOf(42));
        String s = strings.get(0);
    }

    public static void unsafeAdd(List<Object> list,Object o){
        list.add(o);
    }
}
