package common.models.Person;

import common.models.Entity;
import java.io.Serializable;

public class User implements Serializable, Entity {
    // Thuộc tính private để đóng gói (Encapsulation)
    private String username;
    private String password;
    private String id;

    // Constructor (Hàm khởi tạo)

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return username;
    }

    public String getUsername(){return username;}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}