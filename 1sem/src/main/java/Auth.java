public class Auth {
    private final Database database = new Database();
    private final MyUserDao userDao = new MyUserDaoImpl(database);
    
    public boolean register(String login, String password){
        if(login.isEmpty() || password.isEmpty()){
            System.out.println("Логин и пароль не могут быть пустыми");
            return false;
        }

        MyUser existingUser = userDao.getUser(login);
        if(existingUser != null){
            System.out.println("Пользователь с таким логином уже существует");
            return false;
        }

        MyUser user = new MyUser(login, password);
        userDao.save(user);
        System.out.println("Пользователь успешно зарегистрирован");
        return true;
    }

    public boolean login(String login, String password){
        if(login.isEmpty() || password.isEmpty()){
            System.out.println("Логин и пароль не могут быть пустыми");
            return false;
        }

        MyUser user = userDao.getUser(login);
        if(user == null){
            System.out.println("Пользователь с таким логином не найден");
            return false;
        }

        if(!user.getPassword().equals(password)){
            System.out.println("Неверный пароль");
            return false;
        }

        System.out.println("Вход выполнен успешно");
        return true;

    }
}
