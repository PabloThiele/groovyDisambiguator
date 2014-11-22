package old.poc.batch
/*import groovy.json.JsonSlurper
//@GrabResolver(name = 'neo4j-releases', root = 'http://m2.neo4j.org/releases/')
//@Grab(group = 'org.neo4j', module = 'neo4j-rest-graphdb', version = '2.1.5')
//@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.9.1')

import org.neo4j.graphdb.Label
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.ResourceIterable
import org.neo4j.rest.graphdb.RestGraphDatabase

class Put {
    def gdb = new RestGraphDatabase("http://localhost:7474/db/data")
    def slurper = new JsonSlurper()

    def jsonFile = new File('./resources/macmorpho-train-json.txt');

    def run() {
        jsonFile.eachLine("UTF-8",{

            def obj =  slurper.parseText(it)
            def words = obj.sentenceWords
            // Clean the token in the sentence start
            def token = null

            words.each {
                def node = createNode(it.word, 'palavra')

                if (it.macMorphoTag) {

                    println "Palavra: " + it.word + "  -  Tag: " + it.macMorphoTag

                    def classe = createNode(it.macMorphoTag, 'macMorphoTag')
                    RelationshipType re = new RelationshipType() {
                        @Override
                        String name() {
                            return "IS_A"
                        }
                    }
                    node.createRelationshipTo(classe, re)

                    if (token) {
                        RelationshipType fromTo = new RelationshipType() {
                            @Override
                            String name() {
                                return "TO"
                            }
                        }
                        node.createRelationshipTo(token, fromTo)
                    }
                    token = node

                }
            }
        })
        println "Execution done."
    }

    private createNode(id, label) {
        def labelO = new Label() {
            @Override
            public String name() {
                return label
            }
        }
        def node
        //ResourceIterable<org.neo4j.graphdb.Node> nodes = gdb.findNodesByLabelAndProperty(labelO, 'id', id)
        //if(nodes.asList()) {
        //    node = nodes.asList().get(0)
        //} else {
            node = gdb.createNode()
            node.setProperty('id', id)
            node.addLabel(labelO)
        //}
        return node
    }
}

new Put().run()
*/