package Users;

import System.EVM;

public abstract class User {
    private String userName;

    public User(String userName) {
        this.userName = userName;
    }

    abstract void accessSystem(EVM evm, java.util.Scanner scan);

    public String getUserName() {
        return userName;
    }
} 