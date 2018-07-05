package sample.Server;


import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedList<DataType> implements Iterable<DataType> {
    private Node<DataType> head;

    /**
     * Constructs an empty list
     */
    public LinkedList() {
        head = null;
    }

    /**
     * Returns true if the list is empty
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Inserts a new node at the beginning of this list.
     */
    public void addFirst(DataType item) {
        head = new Node<DataType>(item, head);
    }

    /**
     * Returns the first element in the list.
     */
    public DataType getFirst() {
        if (head == null) throw new NoSuchElementException();

        return head.data;
    }

    /**
     * Removes the first element in the list.
     */
    public DataType removeFirst() {
        DataType tmp = getFirst();
        head = head.next;
        return tmp;
    }

    /**
     * Inserts a new node to the end of this list.
     */
    public void addLast(DataType item) {
        if (head == null)
            addFirst(item);
        else {
            Node<DataType> tmp = head;
            while (tmp.next != null) tmp = tmp.next;

            tmp.next = new Node<DataType>(item, null);
        }
    }

    /**
     * Returns the last element in the list.
     */
    public DataType getLast() {
        if (head == null) throw new NoSuchElementException();

        Node<DataType> tmp = head;
        while (tmp.next != null) tmp = tmp.next;

        return tmp.data;
    }

    /**
     * Removes all nodes from the list.
     */
    public void clear() {
        head = null;
    }

    /**
     * Returns true if this list contains the specified element.
     */
    public boolean contains(DataType x) {
        for (DataType tmp : this)
            if (tmp.equals(x))
                return true;

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        LinkedList<DataType> list = (LinkedList<DataType>) obj;

        for (DataType d : list){
            if (!list.contains(d))
                return false;
        }

        return true;
    }

    /**
     * Returns the data at the specified position in the list.
     */
    public DataType get(int pos) {
        if (head == null) throw new IndexOutOfBoundsException();

        Node<DataType> tmp = head;
        for (int k = 0; k < pos; k++) tmp = tmp.next;

        if (tmp == null) throw new IndexOutOfBoundsException();

        return tmp.data;
    }

    /**
     * Returns a string representation
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (DataType x : this)
            result.append(x.toString() + ",");

        return result.toString();
    }

    public int size() {
        int size = 0;
        for (Object x : this)
            size++;

        return size;
    }

    /**
     * Inserts a new node after a node containing the key.
     */
    public void insertAfter(DataType key, DataType toInsert) {
        Node<DataType> tmp = head;

        while (tmp != null && !tmp.data.equals(key)) tmp = tmp.next;

        if (tmp != null)
            tmp.next = new Node<DataType>(toInsert, tmp.next);
    }

    /**
     * Inserts a new node before a node containing the key.
     */
    public void insertBefore(DataType key, DataType toInsert) {
        if (head == null) return;

        if (head.data.equals(key)) {
            addFirst(toInsert);
            return;
        }

        Node<DataType> prev = null;
        Node<DataType> cur = head;

        while (cur != null && !cur.data.equals(key)) {
            prev = cur;
            cur = cur.next;
        }
        //insert between cur and prev
        if (cur != null)
            prev.next = new Node<DataType>(toInsert, cur);
    }

    /**
     * Removes the first occurrence of the specified element in this list.
     */
    public void remove(DataType key) {
        if (head == null)
            throw new RuntimeException("cannot delete");

        if (head.data.equals(key)) {
            head = head.next;
            return;
        }

        Node<DataType> cur = head;
        Node<DataType> prev = null;

        while (cur != null && !cur.data.equals(key)) {
            prev = cur;
            cur = cur.next;
        }

        if (cur == null)
            throw new RuntimeException("cannot delete");

        //delete cur node
        prev.next = cur.next;
    }

    /**
     * Returns a deep copy of the list
     * Complexity: O(n^2)
     */
    public LinkedList<DataType> copy1() {
        LinkedList<DataType> twin = new LinkedList<DataType>();
        Node<DataType> tmp = head;
        while (tmp != null) {
            twin.addLast(tmp.data);
            tmp = tmp.next;
        }

        return twin;
    }

    /**
     * Returns a deep copy of the list
     * Complexity: O(n)
     */
    public LinkedList<DataType> copy2() {
        LinkedList<DataType> twin = new LinkedList<DataType>();
        Node<DataType> tmp = head;
        while (tmp != null) {
            twin.addFirst(tmp.data);
            tmp = tmp.next;
        }

        return twin.reverse();
    }

    /**
     * Reverses the list
     * Complewxity: O(n)
     */
    public LinkedList<DataType> reverse() {
        LinkedList<DataType> list = new LinkedList<DataType>();
        Node<DataType> tmp = head;
        while (tmp != null) {
            list.addFirst(tmp.data);
            tmp = tmp.next;
        }
        return list;
    }

    /**
     * Returns a deep copy of the immutable list
     * It uses a tail reference.
     * Complexity: O(n)
     */
    public LinkedList<DataType> copy3() {
        LinkedList<DataType> twin = new LinkedList<DataType>();
        Node<DataType> tmp = head;
        if (head == null) return null;
        twin.head = new Node<DataType>(head.data, null);
        Node<DataType> tmpTwin = twin.head;
        while (tmp.next != null) {
            tmp = tmp.next;
            tmpTwin.next = new Node<DataType>(tmp.data, null);
            tmpTwin = tmpTwin.next;
        }

        return twin;
    }


    /*******************************************************
     *
     *  The Node class
     *
     ********************************************************/
    private static class Node<DataType> {
        private DataType data;
        private Node<DataType> next;

        public Node(DataType data, Node<DataType> next) {
            this.data = data;
            this.next = next;
        }
    }

    /*******************************************************
     *
     *  The Iterator class
     *
     ********************************************************/

    public Iterator<DataType> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<DataType> {
        private Node<DataType> nextNode;

        public LinkedListIterator() {
            nextNode = head;
        }

        public boolean hasNext() {
            return nextNode != null;
        }

        public DataType next() {
            if (!hasNext()) throw new NoSuchElementException();
            DataType res = nextNode.data;
            nextNode = nextNode.next;
            return res;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
