package ch.ethz.tik.graphgenerator.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import ch.ethz.tik.graphgenerator.elements.Graph;

import com.google.common.base.Preconditions;

public class GraphSerializerUtil {

    @SuppressWarnings("unused")
    private static final String TAG = "GraphLoaderUtil";

    public static void serializeGraph(Graph graph, String path) {
        try {
            Files.write(Paths.get(path), serializeObject(graph));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] serializeObject(Externalizable object)
            throws Exception {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos = null;
        byte[] res = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            object.writeExternal(oos);
            oos.flush();

            res = baos.toByteArray();

        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (oos != null)
                    oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static Graph loadGraph(InputStream inputStream) {
        return deSerialize(new BufferedInputStream(inputStream));
    }

    public static Graph deSerialize(BufferedInputStream inputStream) {
        Graph graph = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            graph = deSerialize(buffer.toByteArray(), Graph.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(graph);
        return graph;
    }

    public static Graph deSerialize(byte[] byteArray,
                                    Class<? extends Externalizable> clazz) {
        ObjectInputStream ois;
        Graph graph = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
            graph = (Graph) clazz.newInstance();
            graph.readExternal(ois);
            ois.close();
        } catch (IOException | ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(graph);
        return graph;
    }

}
