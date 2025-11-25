package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {
	
	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

	private final Battery battery;

	private int speed;
	
	private List<Component> targetComponents;
	
	private transient Iterator<Component> targetComponentsIterator;
	
	private Component currTargetComponent;
	
	private transient ListIterator<Position> currentPathPositionsIter;

	private transient boolean blocked;
	
	private Position memorizedTargetPosition;
	
	private FactoryPathFinder pathFinder;

	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		memorizedTargetPosition = null;
	}

    public Robot() {
        this(null, null, null, null, null);
    }

	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}

    @JsonGetter("speed")
	protected int getSpeed() {
		return speed;
	}

	protected void setSpeed(final int speed) {
		this.speed = speed;
	}

    @JsonGetter("memorizedTargetPosition")
	public Position getMemorizedTargetPosition() {
		return memorizedTargetPosition;
	}

    @JsonGetter("targetComponents")
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}

    @JsonIgnore
	@Override
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
			
			computePathToCurrentTargetComponent();
		}

		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}

    @JsonIgnore
    private boolean isPositionValid(Position pos) {
        int x = pos.getxCoordinate();
        int y = pos.getyCoordinate();
        return (x>=0 && y >=0 && x <this.getFactory().getWidth() && y <this.getFactory().getHeight()
        && this.getFactory().getMobileComponentAt(pos, this) == null);
    }

    private Position findOptimalFreeNeighbouringPosition() {
        int currentX = this.getPosition().getxCoordinate();
        int currentY = this.getPosition().getyCoordinate();
        int delta = this.getSpeed();
        Position newPos1 = new Position(currentX + delta, currentY);
        Position newPos2 = new Position(currentX - delta, currentY);
        Position newPos3 = new Position(currentX, currentY + delta);
        Position newPos4 = new Position(currentX, currentY - delta);
        // Start by looking at the X axis :
        if (this.getFactory().getMobileComponentAt(newPos1, this) != null
            || this.getFactory().getMobileComponentAt(newPos2, this) != null) {
            //Check Y axis first :
            if (isPositionValid(newPos3)) { return newPos3;}
            if (isPositionValid(newPos4)) { return newPos4;}
            if (isPositionValid(newPos1)) { return newPos1;}
            if (isPositionValid(newPos2)) { return newPos2;}
        }
        else {
            if (isPositionValid(newPos1)) { return newPos1;}
            if (isPositionValid(newPos2)) { return newPos2;}
            if (isPositionValid(newPos3)) { return newPos3;}
            if (isPositionValid(newPos4)) { return newPos4;}
        }

        return null;
    }


    private Position findFreeNeighbouringPosition() {
        int currentX = this.getPosition().getxCoordinate();
        int currentY = this.getPosition().getyCoordinate();
        int delta = this.getSpeed();
        Position newPos1 = new Position(currentX + delta, currentY);
        Position newPos2 = new Position(currentX - delta, currentY);
        Position newPos3 = new Position(currentX, currentY + delta);
        Position newPos4 = new Position(currentX, currentY - delta);
        if (isPositionValid(newPos1)) { return newPos1;}
        if (isPositionValid(newPos2)) { return newPos2;}
        if (isPositionValid(newPos3)) { return newPos3;}
        if (isPositionValid(newPos4)) { return newPos4;}
        return null;
    }

    private int moveToNextPathPosition() {
        final Motion motion = computeMotion();
        int displacement = motion == null ? 0 : getFactory().moveComponent(motion, this);
        if (displacement != 0) {
            notifyObservers();
        }
        else if (isLivelyLocked()) {
            final Position freeNeighbouringPosition = findOptimalFreeNeighbouringPosition();
            if (freeNeighbouringPosition != null) {
                this.memorizedTargetPosition = freeNeighbouringPosition;
                displacement = moveToNextPathPosition();
                computePathToCurrentTargetComponent();
            }
        }
        return displacement;
    }


    private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.listIterator();
	}
	
	private Motion computeMotion() {
        if (currentPathPositionsIter == null) {
            computePathToCurrentTargetComponent();
        }
		if (!currentPathPositionsIter.hasNext()) {

			// There is no free path to the target
			blocked = true;

			return null;
		}
		
		
		final Position targetPosition = getTargetPosition();
		final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
														   targetPosition.getyCoordinate(),
				   										   2,
				   										   2);
		
		// If there is another robot, memorize the target position for the next run
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.memorizedTargetPosition = targetPosition;
			
			return null;
		}

		// Reset the memorized position
		this.memorizedTargetPosition = null;
			
		return new Motion(getPosition(), targetPosition);
	}

    @JsonIgnore
	private Position getTargetPosition() {
		// If a target position was memorized, it means that the robot was blocked during the last iteration 
		// so it waited for another robot to pass. So try to move to this memorized position otherwise move to  
		// the next position from the path
		return this.memorizedTargetPosition == null ? currentPathPositionsIter.next() : this.memorizedTargetPosition;
	}

    @JsonIgnore
    public Position getNextPositionWithoutIncrementing() {
        if (currentPathPositionsIter.hasNext()) {
            Position next = currentPathPositionsIter.next();
            currentPathPositionsIter.previous();
            return next;
        }
        return null;
    }

    @JsonIgnore
	public boolean isLivelyLocked() {
	    if (memorizedTargetPosition == null) {
	        return false;
	    }
			
	    final Component otherComponent = getFactory().getMobileComponentAt(memorizedTargetPosition,     
	                                                                   this);

	    if (otherComponent instanceof Robot)  {
            Position otherTarget = ((Robot) otherComponent).getMemorizedTargetPosition();
            if (otherTarget == null) {
                otherTarget = ((Robot) otherComponent).getNextPositionWithoutIncrementing();
            }
            return getPosition().equals(otherTarget);
        }
	    
	    return false;
	}

	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}

    @JsonIgnore
	@Override
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}

    @JsonGetter("battery")
    public Battery getBattery() {
        return battery;
    }

    @JsonGetter("currTargetComponent")
    public Component getCurrTargetComponent() {
        return currTargetComponent;
    }

    @JsonGetter("pathFinder")
    public FactoryPathFinder getPathFinder() {
        return pathFinder;
    }
}
