package cn.hurrican.common.model;

import java.util.Objects;

/**
 * @Author: Hurrican
 * @Description:
 * @Date 2018/3/24
 * @Modified 10:32
 */
public class Entry<K, V> {

    private K key;
    private V value;

    public Entry() {
    }

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;
        Entry<?, ?> entry = (Entry<?, ?>) o;
        return Objects.equals(getKey(), entry.getKey()) &&
                Objects.equals(getValue(), entry.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue());
    }
}
