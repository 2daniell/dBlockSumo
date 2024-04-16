package com.daniel.blocksumo.storage;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Cache<T> extends ArrayList<T> {

    protected T find(Predicate<T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) return t;
        }
        return null;
    }

    protected List<T> findList(Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        for (T t : this) {
            if (predicate.test(t)) list.add(t);
        }
        return list;
    }

    protected List<T> getAll() {
        return new ArrayList<>(this);
    }

}
