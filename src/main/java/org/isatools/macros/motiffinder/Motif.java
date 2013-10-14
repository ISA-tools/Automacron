package org.isatools.macros.motiffinder;

import org.isatools.macros.utils.MotifProcessingUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.io.Serializable;
import java.util.*;

/**
 * Created by the ISA team
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 06/06/2012
 *         Time: 11:57
 */
public class Motif implements Comparable<Motif>, Serializable {

    private long inputNode, outputNode;
    private String relationshipType, outputNodeType, inputNodeType;
    private String stringRepresentation, uniqueString;

    private List<Motif> subMotifs;

    private Set<Set<Long>> relatedNodeIds;
    private int cumulativeUsage = 1;
    private int workflowOccurrence = 0;
    private int totalNodesInvolved = 2;

    private int depth;

    private double score = 0;


    public Motif(Node inputNode, RelationshipType relationshipType, Node outputNode) {
        this.inputNode = inputNode.getId();
        this.outputNode = outputNode.getId();
        this.relationshipType = relationshipType.toString();

        this.outputNodeType = outputNode.getProperty("type").toString();
        this.inputNodeType = inputNode.getProperty("type").toString();

        subMotifs = new ArrayList<Motif>();
        relatedNodeIds = new HashSet<Set<Long>>();
    }

    public Motif(Long inputNode, String inputNodeType, String relationshipType, String outputNodeType, Long outputNode) {
        this.inputNode = inputNode;
        this.outputNode = outputNode;
        this.relationshipType = relationshipType;

        this.outputNodeType = outputNodeType;
        this.inputNodeType = inputNodeType;

        subMotifs = new ArrayList<Motif>();
        relatedNodeIds = new HashSet<Set<Long>>();
    }

    public Motif(Motif motif) {
        this.inputNode = motif.getInputNode();
        this.outputNode = motif.getOutputNode();
        this.relationshipType = motif.getRelationship();
        this.outputNodeType = motif.getOutputNodeType();
        this.inputNodeType = motif.getInputNodeType();
        this.depth = motif.getDepth();

        this.subMotifs = motif.getSubMotifs();

        this.relatedNodeIds = new HashSet<Set<Long>>(motif.getRelatedNodeIds());
    }

    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public Set<Set<Long>> getRelatedNodeIds() {
        return relatedNodeIds;
    }

    public Long getInputNode() {
        return inputNode;
    }

    public Long getOutputNode() {
        return outputNode;
    }

    public String getRelationship() {
        return relationshipType;
    }

    public void incrementUsage() {
        cumulativeUsage++;
    }

    public int getCumulativeUsage() {
        // may need to do some fixes to this if branch events are in the motif. Otherwise we end up with larger counts than there actually are.
        return cumulativeUsage;
    }

    public void setCumulativeUsage(int cumulativeUsage) {
        this.cumulativeUsage = cumulativeUsage;
    }

    public int getWorkflowOccurrence() {
        return workflowOccurrence;
    }

    public void incrementWorkflowOccurrence() {
        workflowOccurrence++;
    }

    public void addRelatedMotif(Motif motif) {
        Set<Long> nodeIdsInMotif = MotifProcessingUtils.getNodeIdsInString(motif.getStringRepresentation());
        totalNodesInvolved += nodeIdsInMotif.size();
        relatedNodeIds.add(nodeIdsInMotif);
    }

    public List<Motif> getSubMotifs() {
        List<Motif> motifs = new ArrayList<Motif>();
        motifs.addAll(subMotifs);
        return motifs;
    }

    public boolean addSubMotif(Motif motif) {
        if (!doesMotifAlreadyExist(motif)) {
            totalNodesInvolved += 2;
            subMotifs.add(motif);
            return true;
        }
        return false;
    }

    private boolean doesMotifAlreadyExist(Motif motif) {
        String uniqueString = motif.getUniqueString();

        if (getUniqueString().equals(uniqueString)) return true;
        for (Motif subMotif : subMotifs) {
            if (uniqueString.equals(subMotif.getUniqueString())) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        stringRepresentation = toString(true).toString();
        return stringRepresentation;
    }

    public String getStringRepresentation() {
        if (stringRepresentation != null) return stringRepresentation;

        return toString();
    }

    public StringBuilder toString(boolean outputTopNode) {
        // we want to check both the motif on its own and the motif in combination with others.

        StringBuilder builder = new StringBuilder();

        boolean firstNodeIsExperiment = getInputNodeType().equals("start");

        if (outputTopNode && !firstNodeIsExperiment) {
            builder.append(getInputNodeType()).append(":").append(getInputNode()).append(":{");
        }

        if (firstNodeIsExperiment) {
            builder.append(getOutputNodeType()).append(":").append(getOutputNode());
        } else {
            builder.append(getRelationship()).append("#").append(getOutputNodeType()).append(":").append(getOutputNode());
        }

        int motifSize = subMotifs.size();

        if (motifSize > 0) {
            builder.append(":").append("{");
        }

        int count = 0;
        int limit = motifSize - 1;

        for (Motif subMotif : subMotifs) {
            builder.append(subMotif.toString(false));
            if (count != limit) {
                builder.append(",");
            }
            count++;
        }

        if (motifSize > 0) {
            builder.append("}");
        }

        if (outputTopNode && !firstNodeIsExperiment) builder.append("}");

        return builder;
    }

    public String getOutputNodeType() {
        return outputNodeType;
    }

    public String getInputNodeType() {
        return inputNodeType;
    }

    public int getDepth() {
        return depth;
    }

    public synchronized Collection<Set<Long>> getNodesInMotif() {
        Set<Long> nodeIdsInMotif = MotifProcessingUtils.getNodeIdsInString(getStringRepresentation());
        relatedNodeIds.add(nodeIdsInMotif);
        return relatedNodeIds;
    }

    public int compareTo(Motif motif) {
        return motif.getScore() < getScore() ? 1 : motif.getScore() > getScore() ? -1 : 0;
    }

    public String getUniqueString() {
        if (uniqueString == null) {
            StringBuilder motifAsUniqueString = new StringBuilder(getOutputNodeType() + "(" + getOutputNode() + "):>" + getRelationship() + ":>" + getInputNodeType() + "(" + getInputNode() + ")");

            for (Motif subMotif : subMotifs) {
                motifAsUniqueString.append(subMotif.getUniqueString());
            }
            uniqueString = motifAsUniqueString.toString();

        }

        return uniqueString;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getTotalNodesInvolved() {
        return totalNodesInvolved;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Motif motif = (Motif) o;

        if (depth != motif.depth) return false;
        if (inputNode != motif.inputNode) return false;
        if (outputNode != motif.outputNode) return false;
        if (inputNodeType != null ? !inputNodeType.equals(motif.inputNodeType) : motif.inputNodeType != null)
            return false;
        if (outputNodeType != null ? !outputNodeType.equals(motif.outputNodeType) : motif.outputNodeType != null)
            return false;
        if (relationshipType != null ? !relationshipType.equals(motif.relationshipType) : motif.relationshipType != null)
            return false;
        if (stringRepresentation != null ? !stringRepresentation.equals(motif.stringRepresentation) : motif.stringRepresentation != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (inputNode ^ (inputNode >>> 32));
        result = 31 * result + (int) (outputNode ^ (outputNode >>> 32));
        result = 31 * result + (relationshipType != null ? relationshipType.hashCode() : 0);
        result = 31 * result + (outputNodeType != null ? outputNodeType.hashCode() : 0);
        result = 31 * result + (inputNodeType != null ? inputNodeType.hashCode() : 0);
        result = 31 * result + depth;
        result = 31 * result + (stringRepresentation != null ? stringRepresentation.hashCode() : 0);
        return result;
    }
}
