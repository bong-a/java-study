package com.study.toy.item_14;

import java.util.Comparator;

public class HashCodeCompareTest {
    static Comparator<Object> hashcodeOrder = new Comparator() {
        public int compare(Object o1, Object o2) {
            return o1.hashCode() - o2.hashCode();
        }
    };
    static Comparator<Object> hashcodeOrder2 = (o1, o2) -> Integer.compare(o1.hashCode(), o2.hashCode());

    public static void main(String[] args) {

        Float a = 123.123f;
        Float b = -0.12312312f;
        Float c = -10.000012312f;
        System.out.println("a :" + a.hashCode() + " b:" + b.hashCode() + " c:" + c.hashCode());

        System.out.println(hashcodeOrder.compare(a, b));
        System.out.println(hashcodeOrder.compare(b, c));
        System.out.println(hashcodeOrder.compare(a, c));

        System.out.println(hashcodeOrder2.compare(a, b));
        System.out.println(hashcodeOrder2.compare(b, c));
        System.out.println(hashcodeOrder2.compare(a, c));

    }
}
