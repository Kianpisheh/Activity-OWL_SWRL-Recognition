package com.example;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import openllet.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;

import org.semanticweb.owlapi.apibinding.OWLManager;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        PelletExplanation.setup();
        // The renderer is used to pretty print clashExplanation
        final ManchesterSyntaxExplanationRenderer renderer = new ManchesterSyntaxExplanationRenderer();
        // The writer used for the clashExplanation rendered
        final PrintWriter out = new PrintWriter(System.out);
        renderer.startRendering(out);

        Map<String, String> props = new HashMap<String, String>();
        props.putIfAbsent("usingObject", "Refrigerator");
        props.putIfAbsent("hasHandGesture", "ShakingFrontRL");
        props.putIfAbsent("hasCurrentSymbolicLocation", "Kitchen");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        ActivityManager activityManager = new ActivityManager();
        try {
            // load the ontolgy
            File file = new File("act_ont_011.owl.xml");
            ontology = manager.loadOntologyFromOntologyDocument(file);

            // setup the reasoner
            OWLReasonerFactory rf = new StructuralReasonerFactory();
            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
            OWLReasoner reasoner = rf.createReasoner(ontology, config);
            final OpenlletReasoner reasoner2 = OpenlletReasonerFactory.getInstance().createReasoner(ontology);

            // inference task
            // reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            PrefixManager pm = new DefaultPrefixManager(
                    "http://www.semanticweb.org/kian/ontologies/2022/0/act_ont_01#");

            Set<SWRLRule> rules1 = ontology.getAxioms(AxiomType.SWRL_RULE);
            List<SWRLRule> list1 = new ArrayList<>();
            list1.addAll(rules1);
            List<SWRLVariable> list2 = new ArrayList<>();
            list2.addAll(list1.get(0).getVariables());
            list2.forEach(e -> System.out.println(e.getIRI()));

            SWRLRule rule1 = new SWRLRuleFactory().getSWRLAtomRule(manager, ontology, pm);

            OWLClass activityClass = manager.getOWLDataFactory().getOWLClass(":Activity", pm);
            OWLClass mkClass = manager.getOWLDataFactory().getOWLClass(":MakingCoffee", pm);

            reasoner2.getInstances(mkClass).forEach(e -> System.out.println(e));

            reasoner2.getSubClasses(activityClass, true).getFlattened().forEach(clazz -> {
                System.out.println(clazz);
                reasoner2.getInstances(clazz, true).forEach(ind -> {
                    System.out.println(ind);
                });
            });
            activityClass.individualsInSignature().forEach(e -> System.out.println(e));

            ontology = activityManager.addActivity(pm, "BrushingTeethKitchen", props, manager, ontology, reasoner2);

            // DONE
            // Set<OWLNamedIndividual> individuals =
            // Classification.classifyIndividuals(reasoner, activityClass);
            // System.out.println("Instances of brushing teeth: ");
            // for (OWLNamedIndividual ind : individuals) {
            // System.out.println(" " + ind);
            // }

            // System.out.println("\n");
            // boolean consistent = reasoner.isConsistent();
            // System.out.println("Consistent: " + consistent);
            // System.out.println("\n");

            // hasEvent -toothbrush hasEvent -coffee_machine -st -et -d_min_100_max_1000
            // time_distance -toothbrush -coffee_machine -d_min_20_max_400

            ontology.getLogicalAxioms().forEach(e -> System.out.println(e.toString()));

            List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
            gens.add(new InferredSubClassAxiomGenerator());
            OWLOntology infOnt = manager.createOntology();
            InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner2, gens);
            iog.fillOntology(manager.getOWLDataFactory(), infOnt);

            // DefaultExplanationGenerator explainer = new
            // DefaultExplanationGenerator(manager, OpenlletReasonerFactory.getInstance(),
            // ontology, null);
            final PelletExplanation explainer = new PelletExplanation(reasoner2);

            InferredClassAssertionAxiomGenerator gen = new InferredClassAssertionAxiomGenerator();
            OWLDataFactory df = OWLManager.getOWLDataFactory();
            Set<OWLClassAssertionAxiom> assertionClass = gen.createAxioms(df, reasoner2);

            List<OWLAxiom> list = new ArrayList<>();
            list.addAll(assertionClass);
            final OWLAxiom axiom = list.get(7);
            System.out.println(axiom.getAxiomType());

            final OWLClass brClass = manager.getOWLDataFactory().getOWLClass(":BrushingTeeth", pm);
            final OWLClass arClass = manager.getOWLDataFactory().getOWLClass(":Activity", pm);
            OWLNamedIndividual ind = manager.getOWLDataFactory().getOWLNamedIndividual(":activity_01", pm);
            // Set<OWLAxiom> axs = explainer.getInstanceExplanation(ind, brClass);
            Set<Set<OWLAxiom>> axs = explainer.getEntailmentExplanations(axiom);
            // Set<OWLAxiom> axioms = explainer.getExplanation(axiom);

            try {
                renderer.render(axs);
            } catch (OWLException | IOException e) {
                e.printStackTrace();
            }

            renderer.endRendering();

            IRI IOR = manager.getOntologyDocumentIRI(ontology);
            OWLClass person = df.getOWLClass(IOR + "#Person");
            OWLDeclarationAxiom da = df.getOWLDeclarationAxiom(person);
            ontology.add(da);
            OWLAxiom ax;

            ontology.logicalAxioms().forEach(sss -> {
                System.out.println(sss);
            });

            // ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }
}

// final PelletExplanation expGen = new PelletExplanation(reasoner);

// // Create some concepts
// final OWLClass madCow = OWL.Class(NS + "mad+cow");
// final OWLClass animalLover = OWL.Class(NS + "animal+lover");
// final OWLClass petOwner = OWL.Class(NS + "pet+owner");

// // Explain why mad cow is an unsatisfiable concept
// Set<Set<OWLAxiom>> exp = expGen.getUnsatisfiableExplanations(madCow);
// out.println("Why is " + madCow + " concept unsatisfiable?");
// renderer.render(exp);

// // Now explain why animal lover is a sub class of pet owner
// exp = expGen.getSubClassExplanations(animalLover, petOwner);
// out.println("Why is " + animalLover + " subclass of " + petOwner + "?");
// renderer.render(exp);

// renderer.endRendering();
