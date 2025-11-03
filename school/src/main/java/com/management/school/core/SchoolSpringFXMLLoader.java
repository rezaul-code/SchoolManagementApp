package com.management.school.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class SchoolSpringFXMLLoader {
	 private final ConfigurableApplicationContext springContext;

	    public SchoolSpringFXMLLoader(ConfigurableApplicationContext springContext) {
	        this.springContext = springContext;
	    }

	    public Parent load(String fxmlPath) throws IOException {
	        URL resource = getClass().getResource(fxmlPath);
	        if (resource == null) {
	            throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
	        }

	        FXMLLoader loader = new FXMLLoader(resource);
	        loader.setControllerFactory(springContext::getBean);
	        return loader.load();
	    }
	    
	    public <T> FXMLView<T> loadWithController(String fxmlPath) throws IOException {
	        FXMLLoader loader = createLoader(fxmlPath);
	        Parent root = loader.load();
	        T controller = loader.getController();
	        return new FXMLView<>(root, controller);
	    }
	    
	    private FXMLLoader createLoader(String fxmlPath) {
	        URL resource = getClass().getResource(fxmlPath);
	        if (resource == null) {
	            throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
	        }
	        FXMLLoader loader = new FXMLLoader(resource);
	        loader.setControllerFactory(springContext::getBean);
	        return loader;
	    }
	    
	    public static class FXMLView<T> {
	        private final Parent root;
	        private final T controller;

	        public FXMLView(Parent root, T controller) {
	            this.root = root;
	            this.controller = controller;
	        }

	        public Parent getRoot() { return root; }
	        public T getController() { return controller; }
	    }
}
