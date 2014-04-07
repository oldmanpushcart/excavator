package com.googlecode.excavator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Ring维护的是一个双向链表的环结构 线程安全
 *
 * @author vlinux
 *
 */
public class Ring<T> {

    /**
     * 数据节点
     *
     * @author vlinux
     * @param <T>
     */
    private class Node {

        Node front;			//前节点
        Node next;			//后节点
        T data;				//节点携带的数据
    }

    private Node current;	//当前节点
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(false);

    /**
     * 初始化链表循环链表
     */
    public Ring() {

    }

    /**
     * 判断是否是一个空的环
     *
     * @return
     */
    public boolean isEmpty() {
        return null == current;
    }

    /**
     * 环形的next
     *
     * @return
     */
    public T ring() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        rwLock.readLock().lock();
        try {
            current = current.next;
        } finally {
            rwLock.readLock().unlock();
        }
        return current.data;
    }

    /**
     * 插入一个数据
     *
     * @param t
     */
    public void insert(T t) {
        Node node = new Node();
        node.data = t;

        rwLock.writeLock().lock();
        try {
            // 第一个插入的节点要初始化环行头节点
            if (null == current) {
                current = node;
                current.front = current.next = current;
            } // 随后插入的就按规矩来
            else {
                node.front = current;
                node.next = current.next;
                current.next.front = node;
                current.next = node;
            }
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    /**
     * 清除环中所有数据
     */
    public void clean() {
        current = null;
    }

    public Iterator<T> iterator() {
        final Node _current = current;
        return new Iterator<T>() {

            private Node first = null;
            private Node itP = _current;

            @Override
            public boolean hasNext() {
                return first != itP;
            }

            @Override
            public T next() {
                if (null == first) {
                    first = itP;
                }
                itP = itP.next;
                return itP.data;
            }

            @Override
            public void remove() {
                rwLock.writeLock().lock();
                try {
                    // 要干掉最后一个元素，变成空环
                    if (itP.next == itP) {
                        first = itP = null;
                        clean();
                    } // 非最后一个元素，就按照规矩来
                    else {
                        itP.next.front = itP.front;
                        itP.front.next = itP.next;
                    }
                } finally {
                    rwLock.writeLock().unlock();
                }

            }

        };
    }

    public static void main(String... args) {

        Ring<String> ring = new Ring<String>();
        ring.insert("luanjia");

        {
            Iterator<String> it = ring.iterator();
            while (it.hasNext()) {
                String name = it.next();
                if (name.equals("luanjia")) {
                    it.remove();
                    System.out.println("nice!");
                }
            }
        }

        {
            for (int i = 0; i < 10; i++) {
                System.out.println("name=" + ring.ring());
            }
        }

    }

}
