package com.gadarts.engine.systems.spatial;

import com.gadarts.engine.elements.Line;

import java.util.ArrayList;

public class SpatialCell {
    //    private ArrayList<PoolableActorEntity> actors = new ArrayList();
    private ArrayList<Line> lines = new ArrayList<Line>();

//    public void removeActor(PoolableActorEntity element) {
//        actors.remove(element);
//    }

//    public ArrayList<PoolableActorEntity> getActors() {
//        return actors;
//    }

//    public boolean containsActor(PoolableActorEntity actorElement) {
//        return actors.contains(actorElement);
//    }

//    public void addActor(PoolableActorEntity actorElement) {
//        actors.add(actorElement);
//    }

    public void addLine(Line line) {
        if (!this.lines.contains(line)) {
            this.lines.add(line);
        }
    }

    public ArrayList<Line> getLines() {
        return lines;
    }
}
