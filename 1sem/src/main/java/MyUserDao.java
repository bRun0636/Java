public interface MyUserDao {
    void save(MyUser user);
    MyUser getUser(String login);
}
