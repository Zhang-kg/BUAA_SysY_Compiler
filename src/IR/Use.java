package IR;

import IR.Values.User;
import IR.Values.Value;

public class Use {
    private Value value;
    private User user;

    public Use(User user, Value value) {
        this.user = user;
        this.value = value;
    }
}
