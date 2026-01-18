package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.animation.Animatable;
import fr.perrier.hologramuilib.client.animation.AnimationManager;
import fr.perrier.hologramuilib.client.menu.MenuElement;

/**
 * Abstract base class for menu elements.
 * Provides common functionality for all element types.
 */
public abstract class AbstractMenuElement implements MenuElement, Animatable {

    protected String id;
    protected Bounds bounds;
    protected float width;
    protected float height;
    protected AnimationManager animationManager;

    protected AbstractMenuElement(String id) {
        this.id = id;
        this.bounds = new Bounds(0, 0, 0, 0);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean isPointInside(float x, float y) {
        return bounds.contains(x, y);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void onClick(int button) {
        // Default: do nothing
    }

    @Override
    public void onHoverStart() {
        // Default: do nothing
    }

    @Override
    public void onHoverEnd() {
        // Default: do nothing
    }

    @Override
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    @Override
    public void setAnimationManager(AnimationManager manager) {
        this.animationManager = manager;
    }
}

