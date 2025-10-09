public class MyUserDaoImpl implements MyUserDao {

    private Database database;

    public MyUserDaoImpl(Database database){
        this.database = database;
    }

    @Override
    public void save(MyUser user){
        database.addUser(user);
    }

    @Override
    public MyUser getUser(String login){
        return database.getUser(login);
    }
}
