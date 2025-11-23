package com.example.system;

import java.io.File;

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
        // Load from absolute file path in src/main/resources
        File fxmlFile = new File("src/main/resources/FXML/Login.fxml");
        System.out.println("Starting app with FXML at: " + fxmlFile.getAbsolutePath());
        System.out.println("File exists: " + fxmlFile.exists());
        
        if (!fxmlFile.exists()) {
            System.err.println("ERROR: Login.fxml not found at: " + fxmlFile.getAbsolutePath());
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
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