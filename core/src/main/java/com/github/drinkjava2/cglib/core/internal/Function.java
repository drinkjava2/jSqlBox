package com.github.drinkjava2.cglib.core.internal;

public interface Function<K, V> {
    V apply(K key);
}
