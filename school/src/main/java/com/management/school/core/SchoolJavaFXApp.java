package com.management.school.core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.management.school.SchoolApplication;

public class SchoolJavaFXApp extends Application {

	private ConfigurableApplicationContext springContext;

	@Override
	public void init() {
		// Boot Spring, passing args and setting headless(false)
		springContext = new SpringApplicationBuilder(SchoolApplication.class)
                .headless(false) // Tell Spring it's a UI application
                .run(SchoolApplication.savedArgs); // Pass the saved args
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Get the Spring-managed FXML Loader bean
		SchoolSpringFXMLLoader loader = springContext.getBean(SchoolSpringFXMLLoader.class);
		
        Scene scene = new Scene(loader.load("/fxml/login.fxml"));

		primaryStage.setScene(scene);
		primaryStage.setTitle("School Management - Login");
		primaryStage.setMaximized(true);
		primaryStage.setResizable(true);
		primaryStage.setMinWidth(900);
		primaryStage.setMinHeight(600);
		primaryStage.show();
	}

	@Override
	public void stop() {
		springContext.close();
	}
}