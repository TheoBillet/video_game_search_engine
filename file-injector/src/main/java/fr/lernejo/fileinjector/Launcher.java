package fr.lernejo.fileinjector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

@SpringBootApplication
public class Launcher {

    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }
        try (AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(Launcher.class)) {
            ArrayList<GameModel> games = new ArrayList<>();
            games = (new ObjectMapper()).readValue(Paths.get(args[0]).toFile(), new TypeReference<ArrayList<GameModel>>(){});
            var rabbitTemplate = springContext.getBean(RabbitTemplate.class);
            for (GameModel game : games) {
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.convertAndSend("", "game_info", game, m -> {
                    m.getMessageProperties().getHeaders().put("game_id", game.id());
                    return m;
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
