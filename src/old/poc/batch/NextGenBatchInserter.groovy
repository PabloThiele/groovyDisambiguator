package old.poc.batch

import groovy.json.JsonSlurper
import utils.enums.Labels


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
def tags = [:]
def tagGroups = [:]

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
store="C:\\Users\\pablo_thiele\\Documents\\Neo4j\\agoravai"
//dir=new File(args[1])
//papers_file=new File(dir,"Paper.csv")
//author_paper_file=new File(dir,"PaperAuthor.csv")

println "Importing data from ${jsonFile} into ${store}"

batch = org.neo4j.unsafe.batchinsert.BatchInserters.inserter(store,config)
try {
    jsonFile.eachLine("UTF-8", {

        def obj = slurper.parseText(it)
        def words = obj.sentenceWords

        // Cleaning the PU tag since not needed on comparison
        words =  words.findAll { it.macMorphoTag != "PU" }
        // Clean the token in the sentence start

        def firstWordOnSentence = true
        def int[] tagGroup = [0,0,0]
        //def segmentedWords = words.collate(3)

        // words holds the full sentence in order on Array
        println "##################"
        println "###NEW SENTENCE###"
        println "##################"
        def actualWordTag = null
        def previousWordTag = null
        def nextWordTag = null
        def startSentence = [macMorphoTag:"START"]
        def endSentence = [macMorphoTag:"END"]

        // First word of the sentence
        for (i = 0; i < words.size; i++) {

            // Get previous word of sentence
            if ((i - 1) >= 0) {
                previousWordTag = words[i - 1]
                if (!tags[previousWordTag.macMorphoTag]) {
                    tags[previousWordTag.macMorphoTag] = batch.createNode([value:previousWordTag.macMorphoTag], Labels.MacMorphoTag)
                }
               // println "Previous " + previousWordTag.word + " " + previousWordTag.macMorphoTag
            }else{
                previousWordTag = startSentence
                if (!tags[previousWordTag.macMorphoTag]) {
                    tags[previousWordTag.macMorphoTag] = batch.createNode([value:previousWordTag.macMorphoTag], Labels.MacMorphoTag)
                }
              //  println "Previous is NULL"
            }

            // Actual word used
            actualWordTag = words[i]
            if (!tags[actualWordTag.macMorphoTag]) {
                tags[actualWordTag.macMorphoTag] = batch.createNode([value:actualWordTag.macMorphoTag], Labels.MacMorphoTag)
            }
          //  println "Actual " + actualWordTag.word + " " + actualWordTag.macMorphoTag

            if ((i + 1) < words.size) {
                nextWordTag = words[i + 1]
                if (!tags[nextWordTag.macMorphoTag]) {
                    tags[nextWordTag.macMorphoTag] = batch.createNode([value:nextWordTag.macMorphoTag], Labels.MacMorphoTag)
                }
           //    println "Next " + nextWordTag.word + " " + nextWordTag.macMorphoTag
            }else{
                nextWordTag = endSentence
                if (!tags[nextWordTag.macMorphoTag]) {
                    tags[nextWordTag.macMorphoTag] = batch.createNode([value:nextWordTag.macMorphoTag], Labels.MacMorphoTag)
                }
          //      println "Next Is NULL"
            }

            if (!palavras[actualWordTag.word]) {
                palavras[actualWordTag.word] = batch.createNode([value: actualWordTag.word], Labels.Palavra)
            }

            tagGroup[0] = 1
            tagGroup[1] = palavras[actualWordTag.word]
            tagGroup[2] = 3


            if (!tagGroups[tagGroup]) {
                tagGroups[tagGroup] = batch.createNode([value:tagGroup], Labels.MacMorphoTagGroup)
            }

            println "The node for word: " + actualWordTag.word + " should be " + tagGroup

        }
        trace()
    })
} catch (Exception ex){
    ex.printStackTrace()
} finally {
   batch.shutdown()
    trace(true)
    println "Total $count Lines ${palavras.size()} Words and ${tags.size()} Tags took ${(now-start)/1000} seconds."
}