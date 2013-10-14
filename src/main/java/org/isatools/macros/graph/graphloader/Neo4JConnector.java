package org.isatools.macros.graph.graphloader;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by the ISA team
 */
public class Neo4JConnector {

    private static String dbPath;
    
    static GraphDatabaseService graphDB;
    static boolean doShutdown = true;

    public Neo4JConnector() {
         this(System.getProperty("java.io.tmpdir") + "/macro" +
                 System.currentTimeMillis() + ".db");
    }

    public Neo4JConnector(String dbPath) {
        Neo4JConnector.dbPath = dbPath;
        doShutdown = false;
        graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        registerShutdownHook();
    }

    public GraphDatabaseService getGraphDB() {
        return graphDB;
    }

    private static void registerShutdownHook() {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed) and deletes the database when it's done.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public static void shutdown() {
        graphDB.shutdown();
        try {
            if(doShutdown) FileUtils.deleteRecursively(new File(dbPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
