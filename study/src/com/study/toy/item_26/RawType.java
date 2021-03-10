package com.study.toy.item_26;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RawType {
    private final Collection stamps = new ArrayList();
    //private final Collection<Stamp> stamps = new ArrayList();

    public void collectionRawType() {
        stamps.add(new Coin()); // unchecked call

        for (Iterator i = stamps.iterator(); i.hasNext(); ) {
            Stamp stamp = (Stamp)i.next(); // ClassCastException 발생
            stamp.cancel();
        }
    }

    static class Stamp {
        private String value;

        public void cancel() {
        }
    }

    static class Coin {
        private int value;
    }

    public void sentence() {
        List<String> wordList = Arrays.asList("가나", "초콜릿", "마카롱", "토스트");
        makeSentenceByList(wordList);
        //makeSentenceByObjectList(wordList);
        wordList.get(0);
    }

    private void makeSentenceByList(List wordList) {
    }

    private void makeSentenceByObjectList(List<Object> wordList) {
    }

}


