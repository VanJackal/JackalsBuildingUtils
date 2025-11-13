package com.njackal.render.model.loader;

import com.njackal.render.model.Model;

import java.io.IOException;
import java.nio.file.Path;

public interface ModelLoader {
    Model load(Path path) throws IOException;
}
