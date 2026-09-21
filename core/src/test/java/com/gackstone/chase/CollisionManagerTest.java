package com.gackstone.chase;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gackstone.chase.physics.CollisionLayer;
import com.gackstone.chase.physics.CollisionManager;
import com.gackstone.chase.physics.ICollidable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CollisionManagerTest {

    private CollisionManager collisionManager;

    private static class TestCollidable implements ICollidable {
        private final BoundingBox box = new BoundingBox();
        private final CollisionLayer layer;
        private boolean active = true;
        final AtomicBoolean collided = new AtomicBoolean(false);

        TestCollidable(Vector3 center, Vector3 dimensions, CollisionLayer layer) {
            this.layer = layer;
            Vector3 half = new Vector3(dimensions).scl(0.5f);
            box.set(new Vector3(center).sub(half), new Vector3(center).add(half));
        }

        @Override
        public BoundingBox getBoundingBox() { return box; }
        @Override
        public CollisionLayer getCollisionLayer() { return layer; }
        @Override
        public boolean isCollisionActive() { return active; }
        @Override
        public void onCollision(ICollidable other) { collided.set(true); }
    }

    @BeforeEach
    public void setUp() {
        collisionManager = new CollisionManager();
    }

    @Test
    public void testPlayerEnemyCollision() {
        TestCollidable player = new TestCollidable(new Vector3(0, 0, 10), new Vector3(2, 2, 2), CollisionLayer.PLAYER);
        TestCollidable enemy = new TestCollidable(new Vector3(0, 0, 10.5f), new Vector3(2, 2, 2), CollisionLayer.ENEMY);

        collisionManager.register(player);
        collisionManager.register(enemy);

        collisionManager.update(0.016f);

        assertTrue(player.collided.get());
        assertTrue(enemy.collided.get());
    }

    @Test
    public void testNonIntersectingEntitiesDoNotCollide() {
        TestCollidable player = new TestCollidable(new Vector3(0, 0, 10), new Vector3(2, 2, 2), CollisionLayer.PLAYER);
        TestCollidable obstacle = new TestCollidable(new Vector3(10, 0, 50), new Vector3(2, 2, 2), CollisionLayer.OBSTACLE);

        collisionManager.register(player);
        collisionManager.register(obstacle);

        collisionManager.update(0.016f);

        assertFalse(player.collided.get());
        assertFalse(obstacle.collided.get());
    }
}
