package old.poc.batch

import groovy.json.JsonSlurper
import utils.enums.Labels
import utils.enums.Types

@GrabResolver(name="neo4j", root="http://m2.neo4j.org/")
@GrabResolver(name="restlet", root="http://maven.restlet.org/")
@Grab('org.neo4j:neo4j:2.1.5')

def slurper = new JsonSlurper()

def jsonFilePath = this.getClass().getResource( '/resources/json_file_today.txt' ).getPath()
//def jsonFilePath = this.getClass().getResource( '/resources/json_test.txt' ).getFile()

def jsonFile = new File(jsonFilePath)

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
def HashMap<String, Object> palavras = new HashMap<String, Object>()
def HashMap<String, Object> tags = new HashMap<String, Object>()
def HashMap<String, Object> tagGroups = new HashMap<String, Object>()

def long tagId = 0
def uniqueTagGroups = false
HashMap<String, Object> startSentence = new HashMap<String, Object>()
startSentence.put('macMorphoTag','START')

HashMap<String, Object> endSentence = new HashMap<String, Object>()
endSentence.put('macMorphoTag','END')

def deCount = 0
def oCount = 0
count = 0
time = start = System.currentTimeMillis()

def trace(output) {
    if (output || ++ count % 1000 == 0) {
        now = System.currentTimeMillis()
        println "$count rows ${(now-time)} ms"
        time = now
    }
}

store="C:\\temp\\MachMorpho"

println "Importing data from ${jsonFile} into ${store}"

batch = org.neo4j.unsafe.batchinsert.BatchInserters.inserter(store,config)
batch.createDeferredSchemaIndex( Labels.MacMorphoTag ).on( 'tag' ).create();
batch.createDeferredSchemaIndex( Labels.Palavra ).on( "word" ).create();
batch.createDeferredSchemaIndex( Labels.TagGroup ).on( "group" ).create();

    try {
        jsonFile.eachLine("UTF-8", {

            def obj = slurper.parseText(it)
            def words = obj.sentenceWords
            def token = null

            // Cleaning the PU tag since not needed on comparison
            words = words.findAll { it.macMorphoTag != "PU" }
            // Clean the token in the sentence start

            // First word of the sentence
            for (i = 0; i < words.size; i++) {

                def int[] tagGroup = [0, 0, 0]

                // Get previous word of sentence
                if ((i - 1) >= 0) {
                    previousWordTag = words[i - 1]
                    if (!tags[previousWordTag.macMorphoTag]) {
                        batch.createNode(++tagId,[tag:previousWordTag.macMorphoTag], Labels.MacMorphoTag)
                        tags[previousWordTag.macMorphoTag] = tagId
                    }
                } else {
                    previousWordTag = startSentence
                    if (!tags[previousWordTag.macMorphoTag]) {
                        batch.createNode(++tagId, [tag: previousWordTag.macMorphoTag], Labels.MacMorphoTag)
                        tags[previousWordTag.macMorphoTag] = tagId
                    }
                }

                // Actual word used
                actualWordTag = words[i]
                if (!tags[actualWordTag.macMorphoTag]) {
                    batch.createNode(++tagId, [tag: actualWordTag.macMorphoTag], Labels.MacMorphoTag)
                    tags[actualWordTag.macMorphoTag] = tagId
                }

                // Next word used
                if ((i + 1) < words.size) {
                    nextWordTag = words[i + 1]
                    if (!tags[nextWordTag.macMorphoTag]) {
                        batch.createNode(++tagId, [tag: nextWordTag.macMorphoTag], Labels.MacMorphoTag)
                        tags[nextWordTag.macMorphoTag] = tagId
                    }
                } else {
                    nextWordTag = endSentence
                    if (!tags[nextWordTag.macMorphoTag]) {
                        batch.createNode(++tagId, [tag: nextWordTag.macMorphoTag], Labels.MacMorphoTag)
                        tags[nextWordTag.macMorphoTag] = tagId
                    }
                }

                /**
                 * Check de count and o count
                 */

                if(actualWordTag.word == 'de'){
                   deCount++
                }

                if(actualWordTag.word == 'o'){
                    oCount++
                }

                if (!palavras[actualWordTag.word]) {
                    batch.createNode(++tagId, [word: actualWordTag.word], Labels.Palavra)
                    palavras[actualWordTag.word] = tagId
                }

                tagGroup[0] = tags[previousWordTag.macMorphoTag]
                tagGroup[1] = tags[actualWordTag.macMorphoTag]
                tagGroup[2] = tags[nextWordTag.macMorphoTag]

                def groupKey = getGroupKey(tagGroup)


                if (uniqueTagGroups && !tagGroups[groupKey]) {
                    batch.createNode(++tagId, [group: tagGroup], Labels.TagGroup)
                    tagGroups[groupKey] = tagId
                } else {
                    batch.createNode(++tagId, [group: tagGroup], Labels.TagGroup)
                    tagGroups[groupKey] = tagId
                }


                // Create Word->TagGroup relationship
                batch.createRelationship(palavras[actualWordTag.word],  tagGroups[groupKey], Types.HAS_A, NO_PROPS)

                if (token) {
                    batch.createRelationship(token, tagGroups[groupKey], Types.TO, NO_PROPS)
                    batch.createRelationship(tagGroups[groupKey], token, Types.FROM, NO_PROPS)
                }

                token = tagGroups[groupKey]
            }
            trace()
        })
    } catch (Exception ex) {
        ex.printStackTrace()
    } finally {
        batch.shutdown()
        trace(true)
        println "Total $count Lines ${palavras.size()} Words and ${tags.size()} Tags took ${(now - start) / 1000} seconds."
        println "Total de: " + deCount
        println "Total o: " + oCount
    }

def getGroupKey(tagGroup){
    def StringBuilder key = new StringBuilder()
    def isFirst = true
    tagGroup.each{
        if(isFirst){
            key.append(it)
            isFirst = false
        }else{
            key.append("_").append(it)
        }
    }
    return key.toString()
}