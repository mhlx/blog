package me.qyh.blog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan(value = "me.qyh.blog")
public class Blog extends SpringBootServletInitializer implements CommandLineRunner {

    private static ApplicationContext ctx;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Blog.class);
    }

    @Override
    protected WebApplicationContext run(SpringApplication application) {
        WebApplicationContext webApplicationContext = super.run(application);
        ctx = webApplicationContext;
        return webApplicationContext;
    }

    public static void main(String[] args) throws Exception {
        ctx = SpringApplication.run(Blog.class, args);
    }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    @Override
    public void run(String... args) throws Exception {

    }
}