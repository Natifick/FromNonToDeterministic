package com.nikit;

import java.util.LinkedList;

/**
 * Используется, потому что мне нужен был список,
 * где можно проверить, есть ли хотя бы 1 элемент
 */
class MyList<T> extends LinkedList<T> {
    public boolean containsOne(LinkedList<T> col) {
        for (T t : col) {
            if (super.contains(t)) {
                return true;
            }
        }
        return false;
    }
}
