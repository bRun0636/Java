public class Main {
    public static void main(String[] args) {
        Auth auth = new Auth();

        while(true){
            System.out.println("1. Регистрация");
            System.out.println("2. Вход");
            System.out.println("3. Выход");

            String login, password;
            int choice = 0;
            try {
                choice = Integer.parseInt(System.console().readLine());
            } 
            catch (Exception e) {
                System.out.println("Вы выбрали не цифру");
            }
            switch(choice){
                case 1:
                    System.out.println("Введите логин: ");
                    login = System.console().readLine();
                    System.out.println("Введите пароль: ");
                    password = System.console().readLine();
                    auth.register(login, password);
                    break;
                    
                case 2:
                    System.out.println("Введите логин: ");
                    login = System.console().readLine();
                    System.out.println("Введите пароль: ");
                    password = System.console().readLine();
                    auth.login(login, password);
                    break;
                    
                case 3:
                    System.out.println("Выход из программы");
                    System.exit(0);
                    break;
                    
                default:
                    System.out.println("Неверный выбор");
                    break;
            }
        }
    }
}