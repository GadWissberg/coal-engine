package com.gadarts.engine.systems.spatial;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.elements.Line;
import com.gadarts.engine.utils.C;
import com.vividsolutions.jts.geom.Envelope;
import elements.LevelElementDataStructure;

import javax.swing.text.html.parser.Entity;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Spatial {
    private HashMap<Integer, SpatialCell> cells;
    private Rectangle2D levelBoundaries;
    private Array alreadyCheckCorners = new Array();
    private Array<Integer> cellsToRemoveFrom = new Array();
    private boolean ready;
    private ArrayList<Entity> actorsToAdd = new ArrayList<Entity>();

    public void init(LevelElementDataStructure lines, Rectangle2D levelBoundaries) {
        this.levelBoundaries = levelBoundaries;
        cells = new HashMap<Integer, SpatialCell>();
        organizeLines(lines);
        ready = true;
//        for (PoolableActorEntity entity : actorsToAdd) {
//            placeActor(entity);
//        }
    }

    private void organizeLines(LevelElementDataStructure lines) {
        Vector2 pointer = initializePointerForCells();
        Envelope auxEnvelope = new Envelope();
        while (pointer.y < levelBoundaries.getMaxY()) {
            organizeLinesForCell(lines, pointer, auxEnvelope);
        }
    }

    private void organizeLinesForCell(LevelElementDataStructure lines, Vector2 pointer, Envelope auxEnvelope) {
        lookForLines(pointer, lines, auxEnvelope);
        float nextX = pointer.x + C.SPATIAL_CELL_SIZE;
        float nextY = pointer.y;
        if (nextX >= levelBoundaries.getMaxX()) {
            nextX = (float) levelBoundaries.getX();
            nextY = nextY + C.SPATIAL_CELL_SIZE;
        }
        pointer.set(nextX, nextY);
    }

    private Vector2 initializePointerForCells() {
        Vector2 pointer = new Vector2();
        pointer.set((float) levelBoundaries.getX(), (float) levelBoundaries.getY());
        return pointer;
    }

    private void lookForLines(Vector2 position, LevelElementDataStructure lines, Envelope auxEnvelope) {
        auxEnvelope.init(position.x, position.x + C.SPATIAL_CELL_SIZE, position.y, position.y + C.SPATIAL_CELL_SIZE);
        Rectangle2D.Float cellRect = new Rectangle2D.Float(position.x, position.y, C.SPATIAL_CELL_SIZE, C.SPATIAL_CELL_SIZE);
        List<Line> cellLines = lines.getQuadTree().query(auxEnvelope);
        if (!cellLines.isEmpty()) {
            insertLinesIntoCell(cellLines, calculateCellId(position.x, position.y), cellRect);
        }
    }


//    public void placeActor(PoolableActorEntity element) {
//        if (ready) {
//            PositionComponent pc = ComponentsMapper.position.get(element);
//            makeSureActorInsideLevel(pc);
//        } else if (!actorsToAdd.contains(element)) actorsToAdd.add(element);
//    }


//    private void removeActorFromOldCells(PoolableActorEntity entity) {
//        for (Integer id : cellsToRemoveFrom) {
//            cells.get(id).removeActor(entity);
//        }
//    }

    private void resetCornersCheck() {
        alreadyCheckCorners.clear();
    }

    private void updateSpatialActorProperty(PositionComponent pc) {
        cellsToRemoveFrom.clear();
        updateSpatialActorBottomCorners(pc);
        updateSpatialActorTopCorners(pc);
    }

    private void updateSpatialActorTopCorners(PositionComponent pc) {
        float radius = pc.getRadius();
    }

    private void updateSpatialActorBottomCorners(PositionComponent pc) {
        float radius = pc.getRadius();
    }

    private int getNewCellIdAndMarkOldCellToRemoveFrom(PositionComponent pc, float xOffset, float yOffset, int currentId) {
        int newId = getCellId(pc.getX() + xOffset, pc.getY() + yOffset);
        if (newId != currentId && !cellsToRemoveFrom.contains(currentId, true)) {
            cellsToRemoveFrom.add(currentId);
        }
        return newId;
    }

//    private void insertActorIntoCell(PoolableActorEntity actorElement, int id) {
//        if (alreadyCheckCorners.contains(id, true)) return;
//        SpatialCell currentCell = cells.get(id);
//        if (currentCell == null) currentCell = createNewCell(id);
//        if (!currentCell.containsActor(actorElement)) currentCell.addActor(actorElement);
//        alreadyCheckCorners.add(id);
//    }


    private void insertLinesIntoCell(List<Line> lines, int cellId, Rectangle2D.Float cellRect) {
        SpatialCell currentCell = cells.get(cellId);
        for (Line l : lines) {
            if (cellRect.intersectsLine(l.getSrc().getX(), l.getSrc().getY(), l.getDst().getX(), l.getDst().getY())) {
                if (currentCell == null) currentCell = createNewCell(cellId);
                currentCell.addLine(l);
            }
        }
    }


    private void makeSureActorInsideLevel(PositionComponent pc) {
        if (!levelBoundaries.contains(pc.getX(), pc.getY()))
            throw new ElementOutsideLevelException();
    }

    private SpatialCell createNewCell(int cellId) {
        SpatialCell currentCell;
        currentCell = new SpatialCell();
        cells.put(cellId, currentCell);
        return currentCell;
    }


    private int calculateCellId(float x, float y) {
        int cellsInRow = MathUtils.ceil((float) (levelBoundaries.getWidth() / C.SPATIAL_CELL_SIZE));
        int col = (int) x / C.SPATIAL_CELL_SIZE;
        int row = ((int) y / C.SPATIAL_CELL_SIZE) * (cellsInRow);
        return col + row;
    }

    public void print(SpatialActorProperty spatialActorProperty) {
//        Gdx.app.log("!", "Cell0:" + cells.get(0).getActors().size() + ", " + "Cell1:" + cells.get(1).getActors().size() + "Cell2:" + cells.get(2).getActors().size());
    }

    public boolean isReady() {
        return ready;
    }

    public int getCellId(float x, float y) {
        x -= levelBoundaries.getMinX();
        y -= levelBoundaries.getMinY();
        int cellsInRow = MathUtils.ceil((float) (levelBoundaries.getWidth() / C.SPATIAL_CELL_SIZE));
        int col = (int) x / C.SPATIAL_CELL_SIZE;
        int row = ((int) y / C.SPATIAL_CELL_SIZE) * (cellsInRow);
        return col + row;
    }

    public SpatialCell getCell(int id) {
        return cells.get(id);
    }

//    public void removeActor(PoolableActorEntity entity) {
//        PositionComponent pc = ComponentsMapper.position.get(entity);
//    }
}
