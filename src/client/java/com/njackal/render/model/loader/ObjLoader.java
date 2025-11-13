package com.njackal.render.model.loader;


import com.njackal.render.model.Model;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ObjLoader implements ModelLoader {
    private static ObjLoader instance;

    private ObjLoader() {}

    public static ObjLoader getInstance() {
        if (instance == null) {
            instance = new ObjLoader();
        }
        return instance;
    }

    @Override
    public Model load(Path path) throws IOException {
        // load file
        List<String> lines = readFile(path);
        // get vertices and face data
        List<Vector3f> vertices = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();// 1 face for every 3 vertices
        parse(vertices, faces, lines);

        // get faces and store them in a vertex list

        List<Vector3f> out = new LinkedList<>();
        for (Integer i: faces) {
            out.add(vertices.get(i));
        }




        return new Model(out);
    }

    private List<String> readFile(Path path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile())));
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = reader.readLine())!=null){
            lines.add(line);
        }
        reader.close();
        return lines;
    }

    private void parse(List<Vector3f> outVert, List<Integer> outFaces, List<String> lines) {
        for (String line : lines) {
            String[] tokens = line.split(" ");
            switch (tokens[0]) {
                case "v" -> outVert.add(new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                ));
                case "f" -> {
                    for (int i = 1; i < tokens.length; i++) {
                        String[] vertex = tokens[i].split("/");
                        outFaces.add(Integer.parseInt(vertex[0]) - 1);
                    }
                }
            }
        }
    }
}
