package com.study.toy.item_26;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class UnKnownRawType {

    void test(){
        Collection<?> test = new ArrayList<>();
        //test.add("asd");
    }
    static int numElementsInCommon(Set s1, Set s2){
        int result = 0;
        for (Object o1:s1){
            if(s2.contains(o1)){
                result++;
            }
        }
        return result;
    }

    static int numElementsInCommon2(Set<?> s1, Set<?> s2){
        int result = 0;
        for (Object o1:s1){
            if(s2.contains(o1)){
                result++;
            }
        }
        return result;
    }
}
