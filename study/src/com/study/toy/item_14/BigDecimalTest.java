package com.study.toy.item_14;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class BigDecimalTest {
    public static void main(String[] args) {
        Set<BigDecimal> bigDecimalHashSet = new HashSet<>();
        Set<BigDecimal> bigDecimalTreeSet= new TreeSet<>();
        BigDecimal a = new BigDecimal("1.0");
        BigDecimal b = new BigDecimal("1.00");
        System.out.println(a.equals(b)); // false

        bigDecimalHashSet.add(a);
        bigDecimalHashSet.add(b);
        System.out.println(bigDecimalHashSet); //[1.0, 1.00]

        bigDecimalTreeSet.add(a);
        bigDecimalTreeSet.add(b);
        /*compareTo로 비교하면 두 BigDecimal 인스턴스가 똑같다.*/
        System.out.println(bigDecimalTreeSet); //[1.0]
    }
}
