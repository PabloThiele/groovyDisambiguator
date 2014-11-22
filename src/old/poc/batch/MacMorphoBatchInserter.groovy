package old.poc.batch

import groovy.json.JsonSlurper
import utils.enums.Labels
import utils.enums.Types


def slurper = new JsonSlurper()

//def jsonFile = new File('./resources/json_file_today.txt');
def jsonFile = new File('./resources/json_test.txt');
//def jsonFile = new File('./resources/macmorpho-train-json.txt');

// [keys,value].transpose().collectEntries()
//def toMap(line) { line.columns.inject([:]){ m,k,v -> m.put(k,line.values[v]);m } }

def config = [
        "use_memory_mapped_buffers": "true",
        "neostore.nodestore.db.mapped_memory": "250M",
        "neostore.relationshipstore.db.mapped_memory": "1G",
        "neostore.propertystore.db.mapped_memory": "500M",
        "neostore.propertystore.db.strings.mapped_memory": "500M",
        "neostore.propertystore.db.arrays.mapped_memory": "0M",
        "cache_type": "none",
        "dump_config": "true"
]

def Map NO_PROPS=[:]

// cache
def palavras = [:]
def tagGroup = [2]
def tags = [:]

count = 0
time = start = System.currentTimeMillis()

def trace(output) {
    if (output || ++ count % 1000 == 0) {
        now = System.currentTimeMillis()
        println "$count rows ${(now-time)} ms"
        time = now
    }
}

//tag::main[]
store="C:\\bea\\Neo4j\\fullMMorphoTest"
//dir=new File(args[1])
//papers_file=new File(dir,"Paper.csv")
//author_paper_file=new File(dir,"PaperAuthor.csv")

println "Importing data from ${jsonFile} into ${store}"

batch = org.neo4j.unsafe.batchinsert.BatchInserters.inserter(store,config)
    try {
    jsonFile.eachLine("UTF-8",{

        def obj =  slurper.parseText(it)
        def words = obj.sentenceWords
        // Clean the token in the sentence start
        def token = null

        words.each {

            def palavra = it.word

            if (!palavras[palavra]) {
                palavras[palavra] = batch.createNode([value: palavra], Labels.Palavra)
            }

            if (it.macMorphoTag) {
//                println "Palavra: " + it.word + "  -  Tag: " + it.macMorphoTag

                def tag = it.macMorphoTag

                if (!tags[tag]) {
                    tags[tag] = batch.createNode([value:it.macMorphoTag], Labels.MacMorphoTag)
                }

                batch.createRelationship(palavras[palavra], tags[tag], Types.IS_A, NO_PROPS)

                if (token) {
                    batch.createRelationship(token, palavras[palavra], Types.TO, NO_PROPS)
                    batch.createRelationship(token, palavras[palavra], Types.TO, NO_PROPS)
                }

                token = palavras[palavra]
            }
        }
        trace()
    })

//    batch.createDeferredConstraint(utils.enums.Labels.Author).assertPropertyIsUnique("name").create()
//    batch.createDeferredSchemaIndex(utils.enums.Labels.Paper).on("title").create()
} finally {
    batch.shutdown()
    trace(true)
    println "Total $count Lines ${palavras.size()} Words and ${tags.size()} Tags took ${(now-start)/1000} seconds."
}

