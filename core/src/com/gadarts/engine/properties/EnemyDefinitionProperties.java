package com.gadarts.engine.properties;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.engine.components.enemies.EnemyDefinitionAttributes;

public class EnemyDefinitionProperties {
    private String name;
    private TextureAtlas atlas;
    private EnemyDefinitionAttributes enemyDefinitionAttributes = new EnemyDefinitionAttributes();

    public EnemyDefinitionProperties(String name, TextureAtlas atlas) {
        this.name = name;
        this.atlas = atlas;
    }

    public int getHp() {
        return enemyDefinitionAttributes.getHp();
    }

    public float getRadius() {
        return enemyDefinitionAttributes.getRadius();
    }

    public float getFov() {
        return enemyDefinitionAttributes.getFov();
    }

    public void setSpeed(float speed) {
        enemyDefinitionAttributes.setSpeed(speed);
    }

    public float getSpeed() {
        return enemyDefinitionAttributes.getSpeed();
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void setRadius(float radius) {
        this.enemyDefinitionAttributes.setRadius(radius);
    }

    public void setFOV(float fov) {
        this.enemyDefinitionAttributes.setFov(fov);
    }

    public void setHP(int hp) {
        this.enemyDefinitionAttributes.setHp(hp);
    }

    public void setPainChance(float painChance) {
        this.enemyDefinitionAttributes.setPainChance(painChance);
    }

    public float getPainChance() {
        return enemyDefinitionAttributes.getPainChance();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnemyDefinitionAttributes getAttributes() {
        return enemyDefinitionAttributes;
    }
}
