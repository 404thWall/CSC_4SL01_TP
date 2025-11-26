package fr.tp.inf112.projects.robotsim.model.notifications;

import fr.tp.inf112.projects.canvas.controller.Observer;

public interface FactoryModelChangedNotifier {
    void notifyObserver();
    boolean addObserver(Observer observer);
    boolean removeObserver(Observer observer);
}
