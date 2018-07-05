package sample.Server;


public class Group {

    private String name = "";
    private LinkedList<Person> users;
    private String massages = "";

    public Group(String name, LinkedList<Person> users) {
        this.name = name;


        this.users = users;

    }

    public String getName() {
        return name;
    }

    public LinkedList<Person> getUsers() {
        return users;
    }

    public String getMassages() {
        return massages;
    }

    public void setMassages(String massages) {
        this.massages = massages;
    }

    public void updateMassages(String new_massage) {
        if (!massages.equals(""))
            massages += "," + new_massage;
        else
            massages = new_massage;

        System.out.println(massages);
    }

    @Override
    public String toString() {
        return name;
    }
}
