package fr.tp.inf112.projects.robotsim.model.path;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.graph.impl.GridVertex;
import fr.tp.inf112.projects.robotsim.model.Position;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class SquareVertex extends GridVertex {
	
	private final RectangularShape shape;

	public SquareVertex(final String label,
						final int xCoordinate,
						final int yCoordinate,
						final int size) {
		super(label, xCoordinate, yCoordinate);

		this.shape = new RectangularShape(xCoordinate, yCoordinate, size, size);
	}

    public SquareVertex() {
        super(null, -1, -1);
        shape = null;
    }

    @JsonGetter("shape")
	public RectangularShape getShape() {
		return shape;
	}

    @JsonIgnore
	public Position getPosition() {
		return getShape() == null ? null : getShape().getPosition();
	}
}
