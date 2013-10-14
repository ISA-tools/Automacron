package org.isatools.macros.gui;

import org.isatools.macros.motiffinder.Motif;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

public class DBGraph {

    private Node correspondingNodeInDB;
    private boolean updating;

    private Set<Integer> associatedMotifs;

    public DBGraph(Node correspondingNodeInDB) {
        this.correspondingNodeInDB = correspondingNodeInDB;
        associatedMotifs = new HashSet<Integer>();
        updating = true;
    }

    public boolean isUpdating() {
        return updating;
    }

    public synchronized void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public Node getCorrespondingNodeInDB() {
        return correspondingNodeInDB;
    }

    public Set<Integer> getAssociatedMotifs() {
        return associatedMotifs;
    }

    public void addAssociatedMotif(int hashCode) {
        associatedMotifs.add(hashCode);
    }

    @Override
    public String toString() {
        String nodeValue = correspondingNodeInDB.getProperty("value").toString();
        return nodeValue.substring(0, nodeValue.indexOf("."));
    }
}
