package old.poc.api

@Grab('org.neo4j:neo4j:2.1.5')
@Grab('org.neo4j.app:neo4j-server:2.1.5')
@Grab(group='org.neo4j.app', module='neo4j-server', version='2.1.5', classifier='static-web')


import org.neo4j.graphdb.*
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.schema.IndexDefinition
import org.neo4j.graphdb.schema.Schema

//import org.neo4j.server.WrappingNeoServerBootstrapper
import utils.enums.Labels

import java.util.concurrent.TimeUnit

def config = [
        "use_memory_mapped_buffers": "true",
        "neostore.nodestore.db.mapped_memory": "250M",
        "neostore.relationshipstore.db.mapped_memory": "1G",
        "neostore.propertystore.db.mapped_memory": "500M",
        "neostore.propertystore.db.strings.mapped_memory": "500M",
        "neostore.propertystore.db.arrays.mapped_memory": "0M",
        "cache_type": "none",
        "dump_config": "true",
        "read_only":"false"
]

//tag::main[]
store="C:\\Users\\pablo_thiele\\Documents\\Neo4j\\fullMMorpho"
//dir=new File(args[1])
//papers_file=new File(dir,"Paper.csv")
//author_paper_file=new File(dir,"PaperAuthor.csv")

println "Importing data from ${store} into ${store}"
/*
graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
        store )
        //.setConfig( GraphDatabaseSettings.read_only, "true" )
        .newGraphDatabase();
*/

// INDEXES FOR LABELS

GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( store );
IndexDefinition indexDefinitionPalavra;
IndexDefinition indexDefinitionTags;

try {
        println "Creating Indexes - start"

        Transaction tx = graphDb.beginTx()
        Schema schema = graphDb.schema();
        indexDefinitionPalavra = schema.indexFor(Labels.Palavra).on("value").create()
        indexDefinitionTags = schema.indexFor(Labels.MacMorphoTag).on("value").create()
        schema.awaitIndexOnline( indexDefinitionPalavra, 2, TimeUnit.MINUTES );
        schema.awaitIndexOnline( indexDefinitionTags, 2, TimeUnit.MINUTES );
        tx.success();
        println "Creating Indexes - end"
}catch (Exception xx){
        xx.printStackTrace()
}

/*
time = start = System.currentTimeMillis()
try {
        Transaction tx = graphDb.beginTx()
        Iterable<Node> node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"espingarda");
        println node.asList().get(0).getPropertyValues()
        Iterable<Relationship> rs = node.asList().get(0).getRelationships(Direction.OUTGOING, utils.enums.Types.IS_A)
        for(Relationship r : rs) {
                println r.getType().name()
        }
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"da");
        println node.asList().get(0).getPropertyValues()
        rs = node.asList().get(0).getRelationships(Direction.OUTGOING, utils.enums.Types.IS_A)
        for(Relationship r : rs) {
               // println r.getType().name()
        }
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"de");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"mil");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"para");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"esta");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"Paulo");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"nossa");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"faz");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()
        node = graphDb.findNodesByLabelAndProperty(utils.enums.Labels.Palavra,'value',"pés");
        println node.asList().get(0).getPropertyValues()
        println node.asList().get(0).getRelationships()

        tx.success();
        now = System.currentTimeMillis()
        println "Time used: ${(now-time)} ms"

}catch (Exception e){
        e.printStackTrace()
}finally{
        graphDb.shutdown()
}
*/

/*
int i = 0
try {
        Transaction tx = graphDb.beginTx()
        Iterable<Node> allNodes = graphDb.getAllNodes();

        //Iterable<Relationship> allRelationships = userJohn.getRelationships();
        //Set<Node> moviesForJohn = new HashSet<Node>();
      //  for(Relationship r : allRelationships){
    //            if(r.getType().name().equalsIgnoreCase("IS_A")){
     //                   Node movieNode = r.getEndNode();
     //                   moviesForJohn.add(movieNode);
     //           }
     //   }

        for(Node word : allNodes) {
                try {

                        println("User has seen movie: " + word.getProperty("value"));
                        i++

                } catch (Exception ex) {
                        ex.printStackTrace()
                }
        }

        tx.success();
}catch (Exception e){
        e.printStackTrace()
}
println("Total nodes from words found:" + i);
//*/