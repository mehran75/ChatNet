package sample.Server;


public class Queue<type> {
    private LinkedList<type> queue;


    public Queue() {
        queue = new LinkedList<type>();
    }

    public int size() {
        return queue.size();
    }

    public type dequeue() {
        if (!queue.isEmpty()) {
            type tmp = queue.getLast();
            queue.remove(tmp);
            return tmp;
        }
        return null;
    }

    public void enqueue(type item) {
        queue.addFirst(item);
    }

    public type peek() {
        return queue.getLast();
    }

    public LinkedList<type> getAll() {
        return queue;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean contains(type tmp) {
        return queue.contains(tmp);
    }
}
