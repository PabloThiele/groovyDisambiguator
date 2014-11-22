package old.poc.rest
/*import groovy.json.JsonSlurper
//@GrabResolver(name = 'neo4j-releases', root = 'http://m2.neo4j.org/releases/')
//@Grab(group = 'org.neo4j', module = 'neo4j-rest-graphdb', version = '2.1.5')
//@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.9.1')

import org.neo4j.graphdb.Label
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.ResourceIterable
import org.neo4j.rest.graphdb.RestGraphDatabase

class Gram {
    def gdb = new RestGraphDatabase("http://localhost:7474/db/data")

    def json = '''
    [
        {"id":"eu","tags":["pronome", "algum"]},
        {"id":"estou","tags":["verbo", "algum"]},
        {"id":"confuso","tags":["adjetivo"]},
        {"id":"sobre","tags":["preposição", "outro"]},
        {"id":"as","tags":["artigo", "algum", "outro"]},
        {"id":"classes","tags":["substantivo"]},
        {"id":"gramaticais","tags":["adjetivo"]},
        {"id":"do","tags":["preposição"]},
        {"id":"text","tags":["substantivo"]}
    ]
    '''

    def run() {
        def obj = new JsonSlurper().parseText(json)
        obj.each {
            def node = createNode(it.id, 'palavra')
            if (it.tags) {
                def token = null
                it.tags.each { clazz ->
                    def classe = createNode(clazz, 'tag')
                    RelationshipType re = new RelationshipType() {
                        @Override
                        String name() {
                            return "IS_A"
                        }
                    }
                    node.createRelationshipTo(classe, re)

                    if(token){
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
        }
    }

    private createNode(id, label) {
        def labelO = new Label() {
            @Override
            public String name() {
                return label
            }
        }
        def node
        ResourceIterable<org.neo4j.graphdb.Node> nodes = gdb.findNodesByLabelAndProperty(labelO, 'id', id)
        if(nodes.asList()) {
            node = nodes.asList().get(0)
        } else {
            node = gdb.createNode()
            node.setProperty('id', id)
            node.addLabel(labelO)
        }
        return node
    }
}

new Gram().run()*/