package com.gadarts.engine.components.enemies;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.gadarts.engine.components.BaseDefinitionComponent;

public class EnemyDefinitionComponent extends BaseDefinitionComponent {
    private EnemyDefinitionAttributes enemyDefinitionAttributes = new EnemyDefinitionAttributes();
    private Model model;
    private TextureAtlas atlas;

    public int getHp() {
        return enemyDefinitionAttributes.getHp();
    }

    public float getRadius() {
        return enemyDefinitionAttributes.getRadius();
    }

    public void setRadius(float radius) {
        this.enemyDefinitionAttributes.radius = radius;
    }

    public float getFov() {
        return enemyDefinitionAttributes.getFov();
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public float getSpeed() {
        return enemyDefinitionAttributes.getSpeed();
    }

    public void setSpeed(float speed) {
        this.enemyDefinitionAttributes.speed = speed;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public float getPainChance() {
        return enemyDefinitionAttributes.getPainChance();
    }

    public void setAttributes(EnemyDefinitionAttributes attributes) {
        this.enemyDefinitionAttributes = attributes;
    }
}
