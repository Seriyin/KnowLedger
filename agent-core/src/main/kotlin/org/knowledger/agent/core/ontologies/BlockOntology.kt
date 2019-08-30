package org.knowledger.agent.core.ontologies

import jade.content.onto.BeanOntology

object BlockOntology : BeanOntology(
    "JBlockOntology", LedgerOntology
) {

    const val ONTOLOGY_NAME = "JBlockOntology"

    init {
        add("org.knowledger.agent.core.ontologies.block")
    }
}
