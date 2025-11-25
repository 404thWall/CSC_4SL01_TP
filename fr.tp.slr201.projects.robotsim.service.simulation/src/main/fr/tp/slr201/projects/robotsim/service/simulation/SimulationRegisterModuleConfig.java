package main.fr.tp.slr201.projects.robotsim.service.simulation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicVertexMixin;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@Configuration
public class SimulationRegisterModuleConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        final PolymorphicTypeValidator typeValidator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(PositionedShape.class.getPackageName())
                        .allowIfSubType(Component.class.getPackageName())
                        .allowIfSubType(BasicVertex.class.getPackageName())
                        .allowIfSubType(ArrayList.class.getName())
                        .allowIfSubType(LinkedHashSet.class.getName())
                        .build();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL)
                .addMixIn(BasicVertex.class, BasicVertexMixin.class);
    //            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return objectMapper;
    }
}

