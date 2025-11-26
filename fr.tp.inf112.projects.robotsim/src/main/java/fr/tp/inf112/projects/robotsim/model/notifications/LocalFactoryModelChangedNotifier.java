package fr.tp.inf112.projects.robotsim.model.notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.canvas.controller.Observer;

import java.util.ArrayList;
import java.util.List;

public class LocalFactoryModelChangedNotifier implements FactoryModelChangedNotifier{

    @JsonIgnore
    private transient List<Observer> observers;

    public LocalFactoryModelChangedNotifier() {
        observers = new ArrayList<Observer>();
    }

    @Override
    public void notifyObserver() {
        for (Observer observer : observers) {
            observer.modelChanged();
        }
    }

    @Override
    public boolean addObserver(Observer observer) {
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return observers.remove(observer);
    }
}
