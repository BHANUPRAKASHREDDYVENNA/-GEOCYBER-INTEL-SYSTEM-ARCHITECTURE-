package org.geocyber.graph;

import org.geocyber.domain.AptActor;
import org.geocyber.domain.CyberCampaign;
import org.geocyber.repository.AptActorRepository;
import org.geocyber.repository.CyberCampaignRepository;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ThreatGraphService {

    private static final Logger log = LoggerFactory.getLogger(ThreatGraphService.class);

    private final AptActorRepository actorRepository;
    private final CyberCampaignRepository campaignRepository;

    public ThreatGraphService(AptActorRepository actorRepository, CyberCampaignRepository campaignRepository) {
        this.actorRepository = actorRepository;
        this.campaignRepository = campaignRepository;
    }

    public static class GraphNode {
        private final String id;
        private final String label;
        private final String type; // ACTOR, CAMPAIGN, MALWARE, TECHNIQUE, COUNTRY, IOC
        private final double score;

        public GraphNode(String id, String label, String type, double score) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.score = score;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
        public String getType() { return type; }
        public double getScore() { return score; }
    }

    public static class GraphLink {
        private final String source;
        private final String target;
        private final String relationship;

        public GraphLink(String source, String target, String relationship) {
            this.source = source;
            this.target = target;
            this.relationship = relationship;
        }

        public String getSource() { return source; }
        public String getTarget() { return target; }
        public String getRelationship() { return relationship; }
    }

    public static class ThreatGraphModel {
        private final List<GraphNode> nodes = new ArrayList<>();
        private final List<GraphLink> links = new ArrayList<>();

        public List<GraphNode> getNodes() { return nodes; }
        public List<GraphLink> getLinks() { return links; }
    }

    /**
     * Builds an in-memory JGraphT directed graph of current threat relationships.
     */
    public Graph<String, DefaultEdge> buildJGraphTInstance() {
        Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

        List<AptActor> actors = actorRepository.findAll();
        List<CyberCampaign> campaigns = campaignRepository.findAll();

        for (AptActor actor : actors) {
            String actorNode = "actor:" + actor.getName();
            graph.addVertex(actorNode);

            if (actor.getCountry() != null) {
                String countryNode = "country:" + actor.getCountry();
                graph.addVertex(countryNode);
                graph.addEdge(actorNode, countryNode);
            }

            if (actor.getObservedMalware() != null) {
                for (String m : actor.getObservedMalware().split(",")) {
                    String mal = m.trim();
                    if (!mal.isBlank()) {
                        String malNode = "malware:" + mal;
                        graph.addVertex(malNode);
                        graph.addEdge(actorNode, malNode);
                    }
                }
            }

            if (actor.getObservedTtps() != null) {
                for (String t : actor.getObservedTtps().split(",")) {
                    String ttp = t.trim();
                    if (!ttp.isBlank()) {
                        String ttpNode = "ttp:" + ttp;
                        graph.addVertex(ttpNode);
                        graph.addEdge(actorNode, ttpNode);
                    }
                }
            }
        }

        for (CyberCampaign campaign : campaigns) {
            String campNode = "campaign:" + campaign.getName();
            graph.addVertex(campNode);

            if (campaign.getAttributedActorName() != null) {
                String actorNode = "actor:" + campaign.getAttributedActorName();
                if (graph.containsVertex(actorNode)) {
                    graph.addEdge(actorNode, campNode);
                }
            }

            if (campaign.getTargetNations() != null) {
                for (String target : campaign.getTargetNations().split(",")) {
                    String t = target.trim();
                    if (!t.isBlank()) {
                        String targetNode = "country:" + t;
                        graph.addVertex(targetNode);
                        graph.addEdge(campNode, targetNode);
                    }
                }
            }
        }

        return graph;
    }

    /**
     * Computes PageRank and Betweenness Centrality on the threat landscape.
     */
    public Map<String, Double> computeCentralityScores() {
        Graph<String, DefaultEdge> graph = buildJGraphTInstance();
        if (graph.vertexSet().isEmpty()) return Collections.emptyMap();

        PageRank<String, DefaultEdge> pageRank = new PageRank<>(graph);
        return pageRank.getScores();
    }

    /**
     * Finds the shortest attribution path between an indicator or malware and a suspected nation state.
     */
    public List<String> findAttributionPath(String sourceEntity, String targetCountry) {
        Graph<String, DefaultEdge> graph = buildJGraphTInstance();
        String src = graph.vertexSet().stream().filter(v -> v.equalsIgnoreCase(sourceEntity) || v.endsWith(":" + sourceEntity)).findFirst().orElse(null);
        String tgt = "country:" + targetCountry.toUpperCase();

        if (src == null || !graph.containsVertex(src) || !graph.containsVertex(tgt)) {
            return Collections.emptyList();
        }

        DijkstraShortestPath<String, DefaultEdge> dijkstra = new DijkstraShortestPath<>(graph);
        GraphPath<String, DefaultEdge> path = dijkstra.getPath(src, tgt);
        return path != null ? path.getVertexList() : Collections.emptyList();
    }

    /**
     * Serializes threat graph into a UI-friendly Graph Model for Vaadin visualization.
     */
    public ThreatGraphModel getVisualThreatGraph() {
        ThreatGraphModel model = new ThreatGraphModel();
        Map<String, Double> centrality = computeCentralityScores();

        Graph<String, DefaultEdge> graph = buildJGraphTInstance();
        for (String v : graph.vertexSet()) {
            String[] parts = v.split(":", 2);
            String type = parts.length > 1 ? parts[0].toUpperCase() : "ENTITY";
            String label = parts.length > 1 ? parts[1] : v;
            double score = centrality.getOrDefault(v, 0.05);

            model.getNodes().add(new GraphNode(v, label, type, score));
        }

        for (DefaultEdge edge : graph.edgeSet()) {
            String src = graph.getEdgeSource(edge);
            String tgt = graph.getEdgeTarget(edge);
            model.getLinks().add(new GraphLink(src, tgt, "CONNECTED_TO"));
        }

        return model;
    }
}
