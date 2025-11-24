package com.example.system;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        // Start Spring Boot without web server
        springContext = new SpringApplicationBuilder(SystemApplication.class)
            .headless(false)
            .web(org.springframework.boot.WebApplicationType.NONE)
            .run(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML from classpath resources (works in both dev and JAR)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/login.fxml"));
        loader.setControllerFactory(springContext::getBean);
        
        Parent root = loader.load();
        
        // Set up the scene
        Scene scene = new Scene(root);
        primaryStage.setTitle("Tuition Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    
    @Override
    public void stop() throws Exception {
        // Close Spring context when JavaFX closes
        springContext.close();
        Platform.exit();
    }
}