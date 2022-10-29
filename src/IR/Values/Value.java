package IR.Values;

import IR.Use;
import IR.types.Type;

import java.util.ArrayList;

public class Value {
    private Type type;
    private String name;
    private ArrayList<User> userArrayList;
    public static int reg_number = 0;

    public Value(Type type, String name) {
        this.type = type;
        this.name = name;
        this.userArrayList = new ArrayList<>();
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addUser(User user) {
        this.userArrayList.add(user);
    }
}
