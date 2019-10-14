package com.gadarts.engine.components.enemies;

public class EnemyDefinitionAttributes {
    float fov;
    float radius;
    float speed;
    int hp;
    float painChance;

    public EnemyDefinitionAttributes() {
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getPainChance() {
        return painChance;
    }

    public void setPainChance(float painChance) {
        this.painChance = painChance;
    }
}