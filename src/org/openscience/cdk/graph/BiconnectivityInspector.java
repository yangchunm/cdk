package org.openscience.cdk.graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.alg.ConnectivityInspector;
import org._3pq.jgrapht.graph.SimpleGraph;
import org._3pq.jgrapht.graph.Subgraph;

/*
 * Created on May 2, 2004
 *
 */

/**
 * @author uli
 *
 */

public class BiconnectivityInspector {
	List          m_biconnectedSets;
	Map           m_vertexToConnectedSet;
	private UndirectedGraph graph;
	
	
	public BiconnectivityInspector(UndirectedGraph g) {
		
		graph = g;
	}
	
	private List lazyFindBiconnectedSets(  ) {
		
		if( m_biconnectedSets == null ) {
			m_biconnectedSets = new ArrayList();
			
			Iterator connectedSets = 
				new ConnectivityInspector(graph).connectedSets().iterator();
			
			while (connectedSets.hasNext()) {
				Graph subgraph = new Subgraph(graph, (Set) connectedSets.next(), null);
				
				// do DFS
				
				// Stack for the DFS
				Stack vertexStack = new Stack();
				
				Set visitedVertices = new HashSet();
				Map parent = new HashMap();
				List dfsVertices = new ArrayList();
				
				Set treeEdges = new HashSet();
				
				Object currentVertex = subgraph.vertexSet().toArray()[0];
				
				vertexStack.push(currentVertex);
				visitedVertices.add(currentVertex);			
				
				while (!vertexStack.isEmpty()) {
					
					currentVertex = vertexStack.pop();
					
					Object parentVertex = parent.get(currentVertex);
					
					if (parentVertex != null) {
						Edge edge = subgraph.getEdge(parentVertex, currentVertex);
						
						// tree edge
						treeEdges.add(edge);
					}
					
					visitedVertices.add(currentVertex);
					
					dfsVertices.add(currentVertex);
					
					Iterator edges = subgraph.edgesOf(currentVertex).iterator();
					while (edges.hasNext()) {
						// find a neighbour vertex of the current vertex 
						Edge edge = (Edge)edges.next();
						
						if (!treeEdges.contains(edge)) {
							
							Object nextVertex = edge.oppositeVertex(currentVertex);
							
							if (!visitedVertices.contains(nextVertex)) {
								
								vertexStack.push(nextVertex);
								
								parent.put(nextVertex, currentVertex);
								
							} else {
								// non-tree edge
							}
							
						}
						
					}
				}
				
				// DFS is finished. Now create the auxiliary graph h
				// Add all the tree edges as vertices in h
				SimpleGraph h = new SimpleGraph();
				
				h.addAllVertices(treeEdges);
				
				visitedVertices.clear();
				
				Set connected = new HashSet();
				
				
				for (Iterator it = dfsVertices.iterator(); it.hasNext();) {
					Object v = it.next();
					
					visitedVertices.add(v);
					
					// find all adjacent non-tree edges
					for (Iterator adjacentEdges = subgraph.edgesOf(v).iterator();
					adjacentEdges.hasNext();) {
						Edge l = (Edge) adjacentEdges.next();
						if (!treeEdges.contains(l)) {
							h.addVertex(l);
							Object u = l.oppositeVertex(v);
							
							// we need to check if (u,v) is a back-edge
							if (!visitedVertices.contains(u)) {
								
								
								while (u != v) {
									Object pu = parent.get(u);
									Edge f = subgraph.getEdge(u, pu);
									
									h.addEdge(f, l);
									
									if (!connected.contains(f)) {
										connected.add(f);
										u = pu;
									} else {
										u = v;
									}
								}
							}
						}
					}
					
					
				}
				
				
				ConnectivityInspector connectivityInspector = 
					new ConnectivityInspector(h);
				
				m_biconnectedSets.addAll(connectivityInspector.connectedSets());
				
			}
		}
		
		return m_biconnectedSets;
	}
	
	public List biconnectedSets(  ) {
		return lazyFindBiconnectedSets(  );
	}
	
	public List hopcroftTarjanKnuthFindBiconnectedSets() {
		Map rank;
		Map parent;
		Map untagged;
		Map link;
		Stack activeStack;
		Map min;
		
		int nn;
		
		
		
		
		
		return m_biconnectedSets;
	}
	
}
