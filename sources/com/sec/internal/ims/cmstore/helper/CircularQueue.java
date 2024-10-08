package com.sec.internal.ims.cmstore.helper;

import java.util.ArrayDeque;
import java.util.Deque;

public class CircularQueue<T> {
    private int capacity;
    public Deque<T> queue = new ArrayDeque();

    public CircularQueue(int i) {
        this.capacity = i;
    }

    public CircularQueue() {
    }

    public void add(T t) {
        while (this.queue.size() >= this.capacity) {
            this.queue.removeFirst();
        }
        this.queue.add(t);
    }

    public int size() {
        return this.queue.size();
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (T t : this.queue) {
            stringBuffer.append(t + "  \r\n");
        }
        return stringBuffer.toString();
    }
}
