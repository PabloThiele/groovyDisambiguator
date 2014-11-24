package GraphInserter
/**
 * Created by Pablo_Thiele on 11/17/2014.
 */
import groovy.json.JsonSlurper
import org.neo4j.graphdb.DynamicLabel
import org.neo4j.graphdb.Label
import utils.enums.Labels
import utils.enums.Types

@GrabResolver(name="neo4j", root="http://m2.neo4j.org/")
@GrabResolver(name="restlet", root="http://maven.restlet.org/")
@Grab('org.neo4j:neo4j:2.1.5')


def slurper = new JsonSlurper()
//def jsonFile = new File('./resources/json_test.txt');
def jsonFilePath = this.getClass().getResource( '/resources/json_file_merged_tags.txt' ).getPath()
//def jsonFilePath = this.getClass().getResource( '/resources/json_file_today.txt' ).getPath()
def jsonFile = new File(jsonFilePath)
//def jsonFile = new File('./resources/json_file_today.txt');

def config = [
        "use_memory_mapped_buffers": "true",
        "neostore.nodestore.db.mapped_memory": "250M",
        "neostore.relationshipstore.db.mapped_memory": "1G",
        "neostore.propertystore.db.mapped_memory": "500M",
        "neostore.propertystore.db.strings.mapped_memory": "500M",
        "neostore.propertystore.db.arrays.mapped_memory": "50M",
        "cache_type": "none",
        "dump_config": "true"
]

def Map NO_PROPS=[:]

// cache
def HashMap<String, Object> palavras = new HashMap<String, Object>()
def HashMap<String, Object> tags = new HashMap<String, Object>()
def HashMap<String, Object> tagGroups = new HashMap<String, Object>()

def long tagId = 0
HashMap<String, Object> startSentence = new HashMap<String, Object>()
startSentence.put('macMorphoTag','START')

HashMap<String, Object> endSentence = new HashMap<String, Object>()
endSentence.put('macMorphoTag','END')


Label macMorphoTagLabel = DynamicLabel.label( 'MacMorphoTag' );
Label palavraLabel = DynamicLabel.label( 'Palavra' );
Label tagGroupLabel = DynamicLabel.label( 'TagGroup' );

def Label[] macMorphoTagArray = [macMorphoTagLabel]
def Label[] palavraArray = [palavraLabel]
def Label[] tagGroupArray = [tagGroupLabel]

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
store="C:\\temp\\MacMorphoMergedTags"

println "Importing data from ${jsonFile} into ${store}"

batch = org.neo4j.unsafe.batchinsert.BatchInserters.inserter(store,config)
batch.createDeferredSchemaIndex( macMorphoTagLabel ).on( 'tag' ).create();
batch.createDeferredSchemaIndex( macMorphoTagLabel ).on( 'uid' ).create();
batch.createDeferredSchemaIndex( palavraLabel ).on( "word" ).create();
batch.createDeferredSchemaIndex( palavraLabel ).on( "uid" ).create();
batch.createDeferredSchemaIndex( tagGroupLabel ).on( "group" ).create();
batch.createDeferredSchemaIndex( tagGroupLabel ).on( "uid" ).create();

try {
    jsonFile.eachLine("UTF-8", {

        def obj = slurper.parseText(it)
        def words = obj.sentenceWords
        def token = null

        // Cleaning the PU tag since not needed on comparison
        words = words.findAll { it.macMorphoTag != "PU" }
        // Clean the token in the sentence start

        def firstWordOnSentence = true

        //def segmentedWords = words.collate(3)

        // words holds the full sentence in order on Array
        // println "##################"
        // println "###NEW SENTENCE###"
        // println "##################"
        def actualWordTag = 0
        def previousWordTag = 0
        def nextWordTag = 0


        // First word of the sentence
        for (i = 0; i < words.size; i++) {

            def int[] tagGroup = [0, 0, 0]

            // Get previous word of sentence
            if ((i - 1) >= 0) {
                previousWordTag = words[i - 1]
                if (!tags[previousWordTag.macMorphoTag]) {
                    batch.createNode(++tagId,[uid: tagId, tag:previousWordTag.macMorphoTag], Labels.MacMorphoTag)
                    tags[previousWordTag.macMorphoTag] = tagId
                    // println "Id: "+ tags[previousWordTag.macMorphoTag] +" Word: " + previousWordTag.macMorphoTag
                }
                // println "Previous " + previousWordTag.word + " " + previousWordTag.macMorphoTag
            } else {
                previousWordTag = startSentence
                if (!tags[previousWordTag.macMorphoTag]) {
                    batch.createNode(++tagId, [uid: tagId, tag: previousWordTag.macMorphoTag], Labels.MacMorphoTag)
                    tags[previousWordTag.macMorphoTag] = tagId
                }
                //  println "Previous is NULL"
            }

            // Actual word used
            actualWordTag = words[i]
            if (!tags[actualWordTag.macMorphoTag]) {
                batch.createNode(++tagId, [uid: tagId, tag: actualWordTag.macMorphoTag], Labels.MacMorphoTag)
                tags[actualWordTag.macMorphoTag] = tagId
            }
            //  println "Actual " + actualWordTag.word + " " + actualWordTag.macMorphoTag

            if ((i + 1) < words.size) {
                nextWordTag = words[i + 1]
                if (!tags[nextWordTag.macMorphoTag]) {
                    batch.createNode(++tagId, [uid: tagId, tag: nextWordTag.macMorphoTag], Labels.MacMorphoTag)
                    tags[nextWordTag.macMorphoTag] = tagId
                }
                //    println "Next " + nextWordTag.word + " " + nextWordTag.macMorphoTag
            } else {
                nextWordTag = endSentence
                if (!tags[nextWordTag.macMorphoTag]) {
                    batch.createNode(++tagId, [uid: tagId, tag: nextWordTag.macMorphoTag], Labels.MacMorphoTag)
                    tags[nextWordTag.macMorphoTag] = tagId
                }
                //      println "Next Is NULL"
            }

            if (!palavras[actualWordTag.word]) {
                batch.createNode(++tagId, [uid: tagId, word: actualWordTag.word], Labels.Palavra)
                palavras[actualWordTag.word] = tagId
            }

            tagGroup[0] = tags[previousWordTag.macMorphoTag]
            tagGroup[1] = tags[actualWordTag.macMorphoTag]
            tagGroup[2] = tags[nextWordTag.macMorphoTag]

            def groupKey = getGroupKey(tagGroup)
            //if (!tagGroups[groupKey]) {
            batch.createNode(++tagId, [uid: tagId, group: tagGroup], Labels.TagGroup)
            tagGroups[groupKey] = tagId
            //} else {
            // println "Tag group already exists!!"
            //}

            // Create TagGroup->Tag relationship
            /**
             *  Ways to get the from relationship
             *
             *  match n-[:HAS_A]-m-[:PREVIOUS]-j where n.value = 'de' return n,j, count(*)
             *  match n-[:HAS_A]-m-[:ACTUAL]-j where n.value = 'de' return n,j, count(*)
             *  match n-[:HAS_A]-m-[:NEXT]-j where n.value = 'de' return n,j, count(*)
             *
             */
            //batch.createRelationship(tagGroups[groupKey], tagGroup[0], utils.enums.Types.PREVIOUS, NO_PROPS)
            //batch.createRelationship(tagGroups[groupKey], tagGroup[1], utils.enums.Types.ACTUAL, NO_PROPS)
            //batch.createRelationship(tagGroups[groupKey], tagGroup[2], utils.enums.Types.NEXT, NO_PROPS)

            // Create Word->TagGroup relationship
            batch.createRelationship(palavras[actualWordTag.word],  tagGroups[groupKey], Types.HAS_A, NO_PROPS)

            // println "The node for word: " + actualWordTag.word + " should be " + tagGroup

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

