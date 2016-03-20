package com.pheiffware.lib.and.touch;

import com.pheiffware.lib.geometry.Transform2D;

/**
 * Created by Steve on 3/18/2016.
 */
public interface TouchTransformListener
{
    /**
     * Called whenever a pointer moves on the screen.  Newly added pointers are processed, but do not generate events.
     *
     * @param transform The tranform generated by the last pointer motion event.
     */
    void touchTransformEvent(Transform2D transform);
}