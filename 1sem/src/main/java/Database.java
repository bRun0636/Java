import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String FILE_PATH = "users.json";
    private List<MyUser> users;
    private Gson gson;

    public Database(){
        gson = new Gson();
        loadUsers();
    }

    private void loadUsers(){
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                users = new ArrayList<>();
                return;
            }
            
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<MyUser>>(){}.getType();
                users = gson.fromJson(reader, listType);
                if (users == null) {
                    users = new ArrayList<>();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки файла: " + e.getMessage());
            users = new ArrayList<>();
        }
    }

    private void saveUsers(){
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения файла: " + e.getMessage());
        }
    }

    public void addUser(MyUser user){
        users.add(user);
        saveUsers();
    }

    public MyUser getUser(String login){
        for(MyUser user : users){
            if(user.getLogin().equals(login)){
                return user;
            }
        }
        return null;
    }
}
