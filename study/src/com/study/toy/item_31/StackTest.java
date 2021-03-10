package com.study.toy.item_31;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

public class StackTest {

    public static void main(String[] args) {
        CustomStack<Number> numbers = new CustomStack<>();
        Iterable<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        numbers.pushAll(integers);

        Collection<Object> objects = new ArrayList<>();
        numbers.popAll(objects);

        List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
    }

    static class CustomStack<E> extends Stack<E>{
        public void pushAll(Iterable<? extends E> src) {
            for (E e : src) {
                push(e);
            }
        }
        public void popAll(Collection<? super E> dst){
            while (!isEmpty()){
                dst.add(pop());
            }
        }
    }
}
