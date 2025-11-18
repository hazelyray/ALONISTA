package com.example.system;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class SystemApplication extends Application {
    
    private static ConfigurableApplicationContext springContext;
    private static String[] args;
    
    public static void main(String[] args) {
        SystemApplication.args = args;
        // Launch JavaFX application
        Application.launch(SystemApplication.class, args);
    }
    
    @Override
    public void init() throws Exception {
        // Start Spring Boot in the background
        springContext = SpringApplication.run(SystemApplication.class, args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load your login FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        // If you need Spring beans in your controller, set the controller factory:
        loader.setControllerFactory(springContext::getBean);
        
        Parent root = loader.load();
        
        // Set up the scene
        Scene scene = new Scene(root);
        primaryStage.setTitle("Tuition Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    @Override
    public void stop() throws Exception {
        // Close Spring context when JavaFX closes
        springContext.close();
        Platform.exit();
    }
}