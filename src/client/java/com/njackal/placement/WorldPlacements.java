package com.njackal.placement;

import java.io.Serializable;
import java.util.List;

public record WorldPlacements(List<PlacementData> placements) implements Serializable {
}
