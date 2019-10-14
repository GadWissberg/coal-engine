package com.gadarts.engine.modelers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gadarts.engine.elements.Sector;
import com.gadarts.engine.elements.SubSector;
import com.gadarts.engine.utils.Utils;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import utils.SharedC;

import java.util.ArrayList;
import java.util.List;

public class SurfaceModeler extends LevelModeler {
    private final ModelBuilder modelBuilder;
    private Vector3 corner00 = new Vector3();
    private Vector3 corner01 = new Vector3();
    private Vector3 corner10 = new Vector3();
    private MeshPartBuilder.VertexInfo info0 = new MeshPartBuilder.VertexInfo();
    private MeshPartBuilder.VertexInfo info1 = new MeshPartBuilder.VertexInfo();
    private MeshPartBuilder.VertexInfo info2 = new MeshPartBuilder.VertexInfo();
    private TextureRegion ceilingTexture;
    private TextureRegion floorTexture;
    private Material ceilingMaterial;
    private Material floorMaterial;

    public SurfaceModeler(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public void modelSurface(Sector sector, int containerId, SubSector subSector) {
        List<PolygonPoint> surfacePolygonPoints = convertToPolygonPoints(subSector);
        Array<Polygon> holes = createHoles(sector, surfacePolygonPoints, containerId);
        Polygon ceilingPolygon = addHolesToSector(surfacePolygonPoints, holes);
        Poly2Tri.triangulate(ceilingPolygon);
        List<DelaunayTriangle> triangles = ceilingPolygon.getTriangles();
        modelTriangles(sector, triangles);
    }

    private void modelTriangles(Sector sector, List<DelaunayTriangle> triangles) {
        if (ceilingMaterial != null) {
            modelCeiling(sector, triangles);
        }
        if (floorMaterial != null) {
            modelFloor(sector, triangles);
        }
    }

    private void modelFloor(Sector sector, List<DelaunayTriangle> triangles) {
        MeshPartBuilder floorMeshBuilder = createMeshBuilder(modelBuilder, sector.getFloorTexture(), floorTexture);
        for (int i = 0; i < triangles.size(); i++) {
            DelaunayTriangle triangle = triangles.get(i);
            createFloorSurface(sector, triangle, floorMeshBuilder);
        }
    }

    private void modelCeiling(Sector sector, List<DelaunayTriangle> triangles) {
        MeshPartBuilder ceilingMeshBuilder = createMeshBuilder(modelBuilder, sector.getFloorTexture(), ceilingTexture);
        for (int i = 0; i < triangles.size(); i++) {
            DelaunayTriangle triangle = triangles.get(i);
            createCeilingSurface(sector, triangle, ceilingMeshBuilder);
        }
    }

    private void createCeilingSurface(Sector s, DelaunayTriangle triangle, MeshPartBuilder ceilingMeshBuilder) {
        initializeVectorsForSurface(triangle.points[0], triangle.points[1], triangle.points[2], s.getCeilingAltitude());
        Vector3 normal = new Vector3(0, 0, -1);
        ceilingMeshBuilder.triangle(
                info0.set(createVertexInfoForCeiling(s, normal, corner10)),
                info1.set(createVertexInfoForCeiling(s, normal, corner01)),
                info2.set(createVertexInfoForCeiling(s, normal, corner00)));
        addRegionAttributes(ceilingTexture, 3);
    }

    private MeshPartBuilder.VertexInfo createVertexInfoForCeiling(Sector s, Vector3 normal, Vector3 position) {
        float hor = ceilingTexture.getRegionWidth() / SharedC.WORLD_UNIT;
        float ver = ceilingTexture.getRegionHeight() / SharedC.WORLD_UNIT;
        float horOffset = s.getCeilingTexture().getHorizontalOffset();
        float verOffset = s.getCeilingTexture().getVerticalOffset();
        MeshPartBuilder.VertexInfo info = new MeshPartBuilder.VertexInfo();
        Vector2 uv = new Vector2((position.x / hor) + horOffset, (position.y / ver) + verOffset);
        info.setPos(position).setNor(normal).setUV(uv);
        return info;
    }

    private MeshPartBuilder.VertexInfo createVertexInfoForFloor(Sector s, Vector3 normal, Vector3 position) {
        float hor = floorTexture.getRegionWidth() / SharedC.WORLD_UNIT;
        float ver = floorTexture.getRegionHeight() / SharedC.WORLD_UNIT;
        float horOffset = s.getFloorTexture().getHorizontalOffset();
        float verOffset = s.getFloorTexture().getVerticalOffset();
        MeshPartBuilder.VertexInfo info = new MeshPartBuilder.VertexInfo();
        Vector2 uv = new Vector2((position.x / hor) + horOffset, (position.y / ver) + verOffset);
        info.setPos(position).setNor(normal).setUV(uv);
        return info;
    }

    private void createFloorSurface(Sector s, DelaunayTriangle triangle, MeshPartBuilder floorMeshBuilder) {
        initializeVectorsForSurface(triangle.points[0], triangle.points[1], triangle.points[2], s.getFloorAltitude());
        Vector3 normal = new Vector3(0, 0, 1);
        floorMeshBuilder.triangle(
                info0.set(createVertexInfoForFloor(s, normal, corner00)),
                info1.set(createVertexInfoForFloor(s, normal, corner01)),
                info2.set(createVertexInfoForFloor(s, normal, corner10)));
        addRegionAttributes(floorTexture, 3);
    }


    private void initializeVectorsForSurface(TriangulationPoint v1, TriangulationPoint v2, TriangulationPoint v3,
                                             float altitude) {
        corner00.set((float) v1.getX() / SharedC.WORLD_UNIT, (float) v1.getY() / SharedC.WORLD_UNIT, altitude);
        corner01.set((float) v2.getX() / SharedC.WORLD_UNIT, (float) v2.getY() / SharedC.WORLD_UNIT, altitude);
        corner10.set((float) v3.getX() / SharedC.WORLD_UNIT, (float) v3.getY() / SharedC.WORLD_UNIT, altitude);
    }

    private Polygon addHolesToSector(List<PolygonPoint> surfacePolygonPoints, Array<Polygon> holes) {
        Polygon ceilingPolygon = new Polygon(surfacePolygonPoints);
        for (Polygon hole : holes) {
            ceilingPolygon.addHole(hole);
        }
        return ceilingPolygon;
    }

    private Array<Polygon> createHoles(Sector sector, List<PolygonPoint> surfacePolygonPoints, int containerId) {
        Array<Polygon> holes = new Array();
        for (SubSector subSector : sector.getSubSectors()) {
            if (subSector.getContainerId() != containerId) continue;
            holes.add(createHole(surfacePolygonPoints, subSector));
        }
        return holes;
    }

    private Polygon createHole(List<PolygonPoint> surfacePolygonPoints, SubSector subSector) {
        ArrayList<PolygonPoint> holePolygonPoints = new ArrayList<PolygonPoint>();
        ArrayList<PolygonPoint> pointsToRemove = new ArrayList<PolygonPoint>();
        float[] points = subSector.getPoints();
        for (int i = 0; i < points.length; i += 2)
            holePolygonPoints.add(removePointIfIsPartOfWholeSurface(surfacePolygonPoints, pointsToRemove, points, i));
        surfacePolygonPoints.removeAll(pointsToRemove);
        return new Polygon(holePolygonPoints);
    }

    private PolygonPoint removePointIfIsPartOfWholeSurface(List<PolygonPoint> surfacePolygonPoints,
                                                           ArrayList<PolygonPoint> pointsToRemove, float[] points, int i) {
        float currentX = points[i];
        float currentY = points[i + 1];
        for (PolygonPoint polygonPoint : surfacePolygonPoints) {
            if (polygonPoint.getXf() == currentX && polygonPoint.getYf() == currentY) pointsToRemove.add(polygonPoint);
        }
        PolygonPoint v = new PolygonPoint(currentX, currentY);
        return v;
    }

    private List<PolygonPoint> convertToPolygonPoints(SubSector sectorArea) {
        List<PolygonPoint> surfacePolygonPoints = new ArrayList<PolygonPoint>();
        float[] points = sectorArea.getPoints();
        for (int i = 0; i < points.length; i += 2) {
            surfacePolygonPoints.add(new PolygonPoint(points[i], points[i + 1]));
        }
        return surfacePolygonPoints;
    }

    public void initializeTextures(TextureRegion ceilingTexture, TextureRegion floorTexture) {
        this.ceilingTexture = ceilingTexture;
        this.floorTexture = floorTexture;
        ceilingMaterial = Utils.materializeTexture(ceilingTexture, null);
        floorMaterial = Utils.materializeTexture(floorTexture, null);
    }
}
