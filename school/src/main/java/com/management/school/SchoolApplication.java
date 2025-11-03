package com.management.school;

import java.io.IOException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.management.school.core.SchoolJavaFXApp;
import javafx.application.Application;

@SpringBootApplication
public class SchoolApplication {
    
    public static String[] savedArgs;

	public static void main(String[] args) throws IOException {
		
		savedArgs = args;
		Application.launch(SchoolJavaFXApp.class, args);
	}
}