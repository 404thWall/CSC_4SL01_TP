package fr.tp.inf112.projects.robotsim.model.shapes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;

public abstract class BasicVertexMixin {

    @JsonCreator
    public BasicVertexMixin(
            @JsonProperty("xCoordinate") int xCoordinate,
            @JsonProperty("yCoordinate") int yCoordinate
    ) {}
}