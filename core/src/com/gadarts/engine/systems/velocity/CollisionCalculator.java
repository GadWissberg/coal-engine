package com.gadarts.engine.systems.velocity;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.engine.components.position.PositionComponent;
import com.gadarts.engine.elements.Line;
import elements.VertexElement;

public class CollisionCalculator {
    private Vector2 auxVector2_1 = new Vector2();
    private Vector2 auxVector2_2 = new Vector2();
    private Vector2 auxVector2_3 = new Vector2();
    private Vector3 auxVector3_1 = new Vector3();

    public float checkPositionCollisionWithMtv(Line line, Vector3 position, float radius, Vector2 displacement) {
        VertexElement srcElement = line.getSrc();
        VertexElement dstElement = line.getDst();
        Vector2 src = auxVector2_1.set(srcElement.getX(), srcElement.getY());
        Vector2 dst = auxVector2_2.set(dstElement.getX(), dstElement.getY());
        Vector2 center = auxVector2_3.set(position.x, position.y);
        float result = Intersector.intersectSegmentCircleDisplace(src, dst, center, radius, displacement);
        return result < Float.POSITIVE_INFINITY ? radius - result : result;
    }

    public boolean checkPositionCollision(Vector3 actor, float actorRadius, PositionComponent other) {
        Vector2 actorPosXY = auxVector2_1.set(actor.x, actor.y);
        Vector2 otherPosition = auxVector2_2.set(other.getX(), other.getY());
        float maxDistance = actorRadius + other.getRadius();
        boolean xyCollision = Intersector.distanceSegmentPoint(actorPosXY, actorPosXY, otherPosition) <= maxDistance;
        boolean zCollision = Math.abs(actor.z - other.getZ()) <= maxDistance;
        return xyCollision && zCollision;
    }

    public boolean checkVelocityVectorCollision(PositionComponent current, Vector3 nextPosition, Line line, Vector2 intersection) {
        boolean collision = Intersector.intersectSegments(current.getX(), current.getY(), nextPosition.x, nextPosition.y,
                line.getSrc().getX(), line.getSrc().getY(), line.getDst().getX(), line.getDst().getY(), intersection);
        return collision;
    }

    public boolean checkVelocityVectorCollision(PositionComponent currentPos, Vector3 nextPos, PositionComponent other) {
        Vector2 src = auxVector2_1.set(currentPos.getX(), currentPos.getY());
        Vector2 dst = auxVector2_2.set(nextPos.x, nextPos.y);
        Vector2 otherPosition = auxVector2_3.set(other.getX(), other.getY());

        boolean xyCollision = Intersector.intersectSegmentCircle(src, dst, otherPosition, other.getRadius());
        float maxAltitude = Math.max(currentPos.getZ(), nextPos.z);
        float minAltitude = Math.min(currentPos.getZ(), nextPos.z);
        boolean zCollision = other.getZ() <= maxAltitude && other.getZ() + other.getBodyAltitude() >= minAltitude;
        return xyCollision && zCollision;
    }
}
