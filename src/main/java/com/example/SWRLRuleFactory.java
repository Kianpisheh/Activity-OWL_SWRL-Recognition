package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.vocabulary.OWL;
import org.checkerframework.checker.units.qual.Length;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;
import org.semanticweb.owlapitools.builders.BuilderLiteral;
import org.semanticweb.owlapitools.builders.BuilderSWRLDataPropertyAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLDataRangeAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLLiteralArgument;
import org.semanticweb.owlapitools.builders.BuilderSWRLObjectPropertyAtom;
import org.semanticweb.owlapitools.builders.BuilderSWRLRule;
import org.semanticweb.owlapitools.builders.BuilderSWRLVariable;

import openllet.owlapi.SWRL;
import openllet.owlapi.XSD;

public class SWRLRuleFactory {

    public SWRLRule getSWRLAtomRule(OWLOntologyManager manager, OWLOntology ontology, PrefixManager pm) {

        OWLDataFactory df = manager.getOWLDataFactory();
        BuilderSWRLRule ruleBuilder = new BuilderSWRLRule(df);

        String ruleString = "Activity(a)^hasEvent(a,e)^ToothBrush(e)^hasStartTime(e,t1)^hasEndTime(e,t2)^subtract(th_d,t1,t2)^th_d<500";

        String[] atomStrings = ruleString.split("^");

        Map<String, List<String>> declaration = getOntologyDeclarations(ontology);

        // for (int i = 0; i < atomStrings.length; i++) {
        // getAtomType(atomStrings[i]);
        // }

        // ----------------class predicate, Activity, DrinkingWater, var ->
        // x--------------------------
        // creating the new activity and adding it into the ontology if necessary
        OWLClass newActivityClass = df.getOWLClass(":DrinkingWater", pm);

        // manager.addAxiom(ontology, axiom);
        // creating the class predicate

        // create the the activty class predicate
        SWRLClassAtom classAtom = createClassAtom("DrinkingWater(a)", "urn:swrl:var", pm, df);
        SWRLClassAtom objPropclassAtom = createClassAtom("ToothBrush(tb)", "urn:swrl:var", pm, df);
        SWRLDataPropertyAtom dataPropAtom = createDataPropertyAtom("hasStartTime(a,ts)", "urn:swrl:var", pm, df);
        SWRLBuiltInAtom builtInAtom = createBuiltInAtom("mod(e)", "urn:swrl:var", pm);
        SWRLDataRangeAtom dataRangeAtom = createDataRangeAtom("0<x<400", "urn:swrl:var", df);

        return ruleBuilder.withBody(objPropclassAtom).buildObject();
    }

    public Map<String, List<String>> getOntologyDeclarations(OWLOntology ontology) {

        Map<String, List<String>> declarations = new HashMap<>();
        declarations.put("Class", new ArrayList<String>());
        declarations.put("ObjectProperty", new ArrayList<String>());
        declarations.put("DataProperty", new ArrayList<String>());

        // List<String> declarations = new ArrayList<>();
        ontology.axioms().forEach(ax -> {
            if (ax.getAxiomType() == AxiomType.DECLARATION) {
                String str = ax.toString();
                String key = str.split("\\(")[1];
                String value = str.split("#")[1].split(">")[0];
                if (declarations.containsKey(key)) {
                    declarations.get(key).add(value);
                }
            }
        });

        return declarations;
    }

    public SWRLDataRangeAtom createDataRangeAtom(String atomStr, String swrlStr, OWLDataFactory df) {

        Set<OWLFacetRestriction> restrections = new HashSet<>();
        String variableName = "";

        String[] ss = atomStr.split("<");
        if (ss.length == 3) {
            variableName = ss[1];
            restrections.add(df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, df.getOWLLiteral(ss[2])));
            restrections.add(df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, df.getOWLLiteral(ss[0])));
        } else if (ss.length == 2) {
            try {
                Integer.parseInt(ss[0]);
                variableName = ss[0];
                restrections.add(df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, df.getOWLLiteral(ss[1])));
            } catch (NumberFormatException e) {
                variableName = ss[1];
                restrections.add(df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, df.getOWLLiteral(ss[0])));
            }
        }

        OWLDatatypeRestriction rs = df.getOWLDatatypeRestriction(XSD.INTEGER, restrections);
        return df.getSWRLDataRangeAtom(rs, SWRL.variable(IRI.create(swrlStr + "#" + variableName)));
    }

    public OWLSubClassOfAxiom createSubclassAxiom(String superclass, String subclass, PrefixManager pm,
            OWLDataFactory df) {

        OWLClass activityRootClass = df.getOWLClass(":Activity", pm);
        OWLClass newActivityClass = df.getOWLClass(":DrinkingWater", pm);
        OWLSubClassOfAxiom axiom = df.getOWLSubClassOfAxiom(newActivityClass, activityRootClass);
        return axiom;
    }

    public SWRLClassAtom createClassAtom(String classAtomStr, String swrlStr, PrefixManager pm, OWLDataFactory df) {

        String className = classAtomStr.split("\\(")[0];
        String variableName = classAtomStr.substring(classAtomStr.indexOf("(") + 1, classAtomStr.indexOf(")"));
        return SWRL.classAtom(df.getOWLClass(":" + className, pm),
                SWRL.variable(IRI.create(swrlStr + "#" + variableName)));
    }

    public SWRLDataPropertyAtom createDataPropertyAtom(String dataPropStr, String swrlStr,
            PrefixManager pm, OWLDataFactory df) {

        String propName = dataPropStr.split("\\(")[0];
        String classVariableName = dataPropStr.substring(dataPropStr.indexOf("(") + 1, dataPropStr.indexOf(","));
        String dataVariableName = dataPropStr.substring(dataPropStr.indexOf(",") + 1, dataPropStr.indexOf(")"));

        OWLDataPropertyExpression dataPropExpression = df.getOWLDataProperty(":" + propName, pm);
        SWRLVariable classVariable = SWRL.variable(IRI.create(swrlStr + "#" + classVariableName));
        SWRLVariable dataVariable = SWRL.variable(IRI.create(swrlStr + "#" + dataVariableName));

        return SWRL.propertyAtom(dataPropExpression, classVariable, dataVariable);
    }

    public SWRLObjectPropertyAtom createObjectPropertyAtom(String objPropStr, String swrlStr, PrefixManager pm,
            OWLDataFactory df) {

        String propName = objPropStr.split("\\(")[0];
        String classVariableName1 = objPropStr.substring(objPropStr.indexOf("(") + 1, objPropStr.indexOf(","));
        String classVariableName2 = objPropStr.substring(objPropStr.indexOf(",") + 1, objPropStr.indexOf(")"));

        OWLObjectPropertyExpression objPropExpression = df.getOWLObjectProperty(":" + propName, pm);
        SWRLVariable classVariable1 = SWRL.variable(IRI.create(swrlStr + "#" + classVariableName1));
        SWRLVariable classVariable2 = SWRL.variable(IRI.create(swrlStr + "#" + classVariableName2));

        return SWRL.propertyAtom(objPropExpression, classVariable1, classVariable2);
    }

    public SWRLBuiltInAtom createBuiltInAtom(String atomStr, String swrlStr, PrefixManager pm) {

        // extract args
        List<SWRLDArgument> args = new ArrayList<>();
        String[] ss = atomStr.split(",");
        int ii = ss[0].indexOf("(");
        ss[0] = ss[0].substring(ii + 1, ss[0].length());
        ss[ss.length - 1] = ss[ss.length - 1].substring(0, ss[ss.length - 1].length() - 1);

        for (int i = 0; i < ss.length; i++) {
            args.add(SWRL.variable(IRI.create(swrlStr + "#" + ss[i])));
        }

        // parse builtins
        String atomName = atomStr.split("\\(")[0];
        SWRLBuiltInsVocabulary builtInVocab = null;

        switch (atomName) {
            case "subtract":
                builtInVocab = SWRLBuiltInsVocabulary.SUBTRACT;
                break;
            case "add":
                builtInVocab = SWRLBuiltInsVocabulary.ADD;
                break;
            case "mod":
                builtInVocab = SWRLBuiltInsVocabulary.MOD;
                break;
            default:
                break;
        }

        return SWRL.builtIn(builtInVocab, args);
    }

}
