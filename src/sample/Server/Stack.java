package sample.Server;



public class Stack<Type> {

    private LinkedList<Type> list;
    private LinkedList<Type> excludeList;

    public Stack() {
        list = new LinkedList<Type>();
        excludeList = new LinkedList<Type>();
    }

    public void push(Type data) {
        list.addFirst(data);
    }

    public Type pop() {
        if (list.isEmpty()) {
            return null;
        }
        Type tmp = list.getFirst();
        list.removeFirst();
        return tmp;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Type peek() {
        return list.getFirst();
    }

    public boolean contains(Type obj) {
        return list.contains(obj);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public void exclude(Type item){
        excludeList.addFirst(item);
    }

    public void unExclude(Type item){
        excludeList.remove(item);
    }

    public boolean excludeContains(Type item){
        return excludeList.contains(item);
    }
}
