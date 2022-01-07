package com.example;

import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


public class Classification {
    public static Set<OWLNamedIndividual> classifyIndividuals(OWLReasoner reasoner, OWLClass targetActivityClass) {
        NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(targetActivityClass, true);
        return individualsNodeSet.getFlattened();
    }

    
}
