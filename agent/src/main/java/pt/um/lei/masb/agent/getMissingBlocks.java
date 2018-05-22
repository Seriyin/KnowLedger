package pt.um.lei.masb.agent;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.um.lei.masb.blockchain.Block;
import pt.um.lei.masb.blockchain.BlockChain;

import java.util.Random;

public class getMissingBlocks extends Behaviour {
    private Codec codec = new SLCodec();
    private BlockChain bc;
    private Ontology blOntology;

    public getMissingBlocks(BlockChain bc){
        try {
            blOntology=new BlockOntology();
        } catch (BeanOntologyException e) {
            e.printStackTrace();
        }
        this.bc=bc;
    }

    @Override
    public void action() {
        Random random = new Random();
        int rnd=0,numR=0,numBl,previous=-1;
        boolean upToDate=false;

        DFAgentDescription dfd = new DFAgentDescription();
        DFAgentDescription[] agentList = new DFAgentDescription[0];
        try {
            agentList = DFService.search(myAgent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        while(!upToDate){
            do{
                rnd = random.nextInt(agentList.length);
            }while(rnd==previous);

            DFAgentDescription agent=agentList[rnd];
            var msg = new ACLMessage(ACLMessage.REQUEST);

            msg.addReceiver(agent.getName());
            msg.setContent(bc.getLastBlock().getHeader().getBlockHeight());

            //Receive number of missing blocks
            ACLMessage num= myAgent.blockingReceive(3000);

            if (num!=null) {
                numBl = Integer.parseInt(num.getContent());
                if (numBl!=0) {
                    //Receive blocks
                    var mb = MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()),
                            MessageTemplate.MatchOntology(blOntology.getName()));
                    while(numR!=numBl) {
                        var blmsg = myAgent.blockingReceive(mb, 3000);
                        try {
                            if (blmsg != null) {
                                ContentElement blce = myAgent.getContentManager().extractContent(blmsg);
                                Block bl = (Block) blce;
                                bc.addBlock(bl);
                            }else{
                                break;
                            }
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                        numR++;
                    }
                    if (numR==numBl) upToDate=true;
                }else{
                    upToDate=true;
                }
            }
            previous=rnd;
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
