package sample.Server;


public class Person {

    private LinkedList<String> numbers;
    private LinkedList<Person> friends;
    private String name = "";


    private String password = "";

    public Person(String name) {
        this.name = name;
        numbers = new LinkedList<>();
        friends = new LinkedList<>();
    }

    public Person() {
        numbers = new LinkedList<>();
        friends = new LinkedList<>();
    }


    public LinkedList<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(LinkedList<String> numbers) {
        this.numbers = numbers;
    }

    public LinkedList<Person> getFriends() {
        return friends;
    }

    public void setFriends(LinkedList<Person> friends) {
        this.friends = friends;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addFriend(Person friend) {

        if (!friends.contains(friend)) {
            friends.addFirst(friend);
            if (!friend.getFriends().contains(this))
                friend.addFriend(this);
        } else
            System.out.println(friend.getName() + " is already added as a friend!");
    }

    public void addNumber(String number) {
        if (!numbers.contains(number))
            numbers.addFirst(number);
        else
            System.out.println("this number added before");

    }

    @Override
    public boolean equals(Object obj) {
        Person person = (Person) obj;

        if (person.getName().equals(name)) {

            if (!getNumbers().equals(person.getNumbers())) {
                return false;
            }

        } else {
            return false;
        }
        return true;

    }

    public String friendListToString() {
        String result = "";
        for (Person p : getFriends()) {
            result += p.getName() + "@";
            for (String s : p.getNumbers()) {
                result += s + "@";
            }
            result = result.substring(0, result.length() - 1);
            result += ",";
        }
        if (!result.equals(""))
            result = result.substring(0, result.length() - 1);
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
