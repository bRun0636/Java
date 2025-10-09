import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static Auth auth = new Auth();
    private static Frame mainFrame;
    private static CardLayout cardLayout;
    private static Panel cardPanel;
    
    public static void main(String[] args) {
        // Создаем главное окно
        mainFrame = new Frame("Система авторизации");
        mainFrame.setSize(400, 300);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        
        // Создаем CardLayout для переключения между панелями
        cardLayout = new CardLayout();
        cardPanel = new Panel(cardLayout);
        
        // Создаем панели
        createMainMenuPanel();
        createLoginPanel();
        createRegisterPanel();
        createSuccessPanel();
        
        mainFrame.add(cardPanel);
        
        // Обработчик закрытия окна
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        mainFrame.setVisible(true);
    }
    
    private static void createMainMenuPanel() {
        Panel mainMenuPanel = new Panel();
        mainMenuPanel.setLayout(new BorderLayout());
        
        // Заголовок
        Label titleLabel = new Label("Добро пожаловать!", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLUE);
        
        // Панель с кнопками
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));
        
        Button loginButton = new Button("Вход");
        Button registerButton = new Button("Регистрация");
        Button exitButton = new Button("Выход");
        
        // Стилизация кнопок
        loginButton.setFont(new Font("Arial", Font.PLAIN, 14));
        registerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        loginButton.setBackground(Color.LIGHT_GRAY);
        registerButton.setBackground(Color.LIGHT_GRAY);
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);
        
        // Обработчики событий
        loginButton.addActionListener(e -> cardLayout.show(cardPanel, "LOGIN"));
        registerButton.addActionListener(e -> cardLayout.show(cardPanel, "REGISTER"));
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        mainMenuPanel.add(titleLabel, BorderLayout.NORTH);
        mainMenuPanel.add(buttonPanel, BorderLayout.CENTER);
        mainMenuPanel.add(exitButton, BorderLayout.SOUTH);
        
        cardPanel.add(mainMenuPanel, "MAIN");
    }
    
    private static void createLoginPanel() {
        Panel loginPanel = new Panel();
        loginPanel.setLayout(new BorderLayout());
        
        // Заголовок
        Label titleLabel = new Label("Вход в систему", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLUE);
        
        // Панель с полями ввода
        Panel inputPanel = new Panel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        
        Label loginLabel = new Label("Логин:");
        Label passwordLabel = new Label("Пароль:");
        TextField loginField = new TextField(20);
        TextField passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        
        inputPanel.add(loginLabel);
        inputPanel.add(loginField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(new Label()); // Пустая ячейка
        inputPanel.add(new Label()); // Пустая ячейка
        
        // Панель с кнопками
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout());
        
        Button loginButton = new Button("Войти");
        Button backButton = new Button("Назад");
        
        loginButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Обработчики событий
        loginButton.addActionListener(e -> {
            String login = loginField.getText();
            String password = passwordField.getText();
            
            if (auth.login(login, password)) {
                cardLayout.show(cardPanel, "SUCCESS");
            } else {
                // Показываем сообщение об ошибке
                Dialog errorDialog = new Dialog(mainFrame, "Ошибка", true);
                errorDialog.setSize(300, 150);
                errorDialog.setLocationRelativeTo(mainFrame);
                errorDialog.setLayout(new BorderLayout());
                
                Label errorLabel = new Label("Неверный логин или пароль!", Label.CENTER);
                errorLabel.setForeground(Color.RED);
                
                Button okButton = new Button("OK");
                okButton.addActionListener(ev -> errorDialog.dispose());
                
                errorDialog.add(errorLabel, BorderLayout.CENTER);
                errorDialog.add(okButton, BorderLayout.SOUTH);
                errorDialog.setVisible(true);
            }
        });
        
        backButton.addActionListener(e -> {
            loginField.setText("");
            passwordField.setText("");
            cardLayout.show(cardPanel, "MAIN");
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(backButton);
        
        loginPanel.add(titleLabel, BorderLayout.NORTH);
        loginPanel.add(inputPanel, BorderLayout.CENTER);
        loginPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        cardPanel.add(loginPanel, "LOGIN");
    }
    
    private static void createRegisterPanel() {
        Panel registerPanel = new Panel();
        registerPanel.setLayout(new BorderLayout());
        
        // Заголовок
        Label titleLabel = new Label("Регистрация", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLUE);
        
        // Панель с полями ввода
        Panel inputPanel = new Panel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        
        Label loginLabel = new Label("Логин:");
        Label passwordLabel = new Label("Пароль:");
        TextField loginField = new TextField(20);
        TextField passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        
        inputPanel.add(loginLabel);
        inputPanel.add(loginField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(new Label()); // Пустая ячейка
        inputPanel.add(new Label()); // Пустая ячейка
        
        // Панель с кнопками
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout());
        
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");
        
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Обработчики событий
        registerButton.addActionListener(e -> {
            String login = loginField.getText();
            String password = passwordField.getText();
            
            if (auth.register(login, password)) {
                cardLayout.show(cardPanel, "SUCCESS");
            } else {
                // Показываем сообщение об ошибке
                Dialog errorDialog = new Dialog(mainFrame, "Ошибка", true);
                errorDialog.setSize(300, 150);
                errorDialog.setLocationRelativeTo(mainFrame);
                errorDialog.setLayout(new BorderLayout());
                
                Label errorLabel = new Label("Ошибка регистрации!", Label.CENTER);
                errorLabel.setForeground(Color.RED);
                
                Button okButton = new Button("OK");
                okButton.addActionListener(ev -> errorDialog.dispose());
                
                errorDialog.add(errorLabel, BorderLayout.CENTER);
                errorDialog.add(okButton, BorderLayout.SOUTH);
                errorDialog.setVisible(true);
            }
        });
        
        backButton.addActionListener(e -> {
            loginField.setText("");
            passwordField.setText("");
            cardLayout.show(cardPanel, "MAIN");
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        registerPanel.add(titleLabel, BorderLayout.NORTH);
        registerPanel.add(inputPanel, BorderLayout.CENTER);
        registerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        cardPanel.add(registerPanel, "REGISTER");
    }
    
    private static void createSuccessPanel() {
        Panel successPanel = new Panel();
        successPanel.setLayout(new BorderLayout());
        
        // Заголовок
        Label titleLabel = new Label("Успешно!", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.GREEN);
        
        // Панель с кнопкой
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout());
        
        Button backButton = new Button("Вернуться в главное меню");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MAIN"));
        
        buttonPanel.add(backButton);
        
        successPanel.add(titleLabel, BorderLayout.CENTER);
        successPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        cardPanel.add(successPanel, "SUCCESS");
    }
}