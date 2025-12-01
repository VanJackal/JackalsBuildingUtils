package com.njackal.placement;

import java.io.Serializable;

/**
 * @param model model to be placed
 * @param transform transform to be applied to the model
 */
public record PlacementData(
        Transform transform,
        String model
) implements Serializable {
}
