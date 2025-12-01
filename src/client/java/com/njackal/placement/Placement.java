package com.njackal.placement;

import com.njackal.render.model.Model;
import com.njackal.render.model.loader.ObjLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Placement {
    private Transform transform;
    private Model model;

    private Placement(Transform transform, Model model) {
        this.transform = transform;
        this.model = model;
    }

    public static Placement fromData(PlacementData data) throws IOException {
        Model model = ObjLoader.getInstance().load(Path.of(data.model()));
        return new Placement(data.transform(), model);
    }

    public Transform transform() {
        return transform;
    }

    public Model model() {
        return model;
    }
}
