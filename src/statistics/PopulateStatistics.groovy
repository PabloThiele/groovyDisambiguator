package statistics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel
import org.neo4j.graphdb.Label
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.Transaction
import utils.enums.TagList
import utils.model.TagStatistic
import utils.model.WordStatistic

import java.math.RoundingMode
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@GrabResolver(name="neo4j", root="http://m2.neo4j.org/")
@GrabResolver(name="restlet", root="http://maven.restlet.org/")
@Grab('org.neo4j:neo4j:2.1.5')
@Grab(group='com.google.code.gson', module='gson', version='1.7.1')
@Grab(group='commons-io', module='commons-io', version='2.4')


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

// cache
palavras = new HashMap<String, Object>()
tags = new HashMap<String, Object>()

HashMap<String, Object> startSentence = new HashMap<String, Object>()
startSentence.put('macMorphoTag','START')

HashMap<String, Object> endSentence = new HashMap<String, Object>()
endSentence.put('macMorphoTag','END')

tagMap = new HashMap<Integer, String>()
wordStatisticMap = new HashMap<String, WordStatistic>();

count = 0
time = start = System.currentTimeMillis()

def trace(output) {
    if (output || ++ count % 1000 == 0) {
        now = System.currentTimeMillis()
        println "$count rows ${(now-time)} ms"
        time = now
    }
}

store="C:\\temp\\MacMorphoUIDTest"
//store="C:\\temp\\testDBTestCount"

db = new GraphDatabaseFactory().newEmbeddedDatabase( store );

println "Getting statistics data from ${jsonFile} and ${store}"

println "Create a new Engine..."
engine = new ExecutionEngine( db );
println "Was created a new Engine..."

println "Create a Tag Map..."
populateTagMap()
println "TagMap created !!"

ExecutionResult result;

    try {
        Transaction tx  = db.beginTx()
        jsonFile.eachLine("UTF-8", {

            def obj = slurper.parseText(it)
            def words = obj.sentenceWords

            // Cleaning the PU tag since not needed on comparison
            //println "Removing PU items"
            words = words.findAll { it.macMorphoTag != "PU" }

            // First word of the sentence
          try {

                for (i = 0; i < words.size; i++) {
                    // Check if it's a new Word or not
                    wordKey = words[i].word

                    if(wordStatisticMap.get(wordKey) == null) {
                        result = engine.execute(getHitCountQuery(wordKey))
                        wordStatisticMap.put(wordKey, populateStatistic(wordKey, result))
                    }
                }
              //  tx.success()
            }catch (Exception e){
                e.printStackTrace()
            }

            tx.success()
            trace()
        })

        createStatsJson(wordStatisticMap)

    } catch (Exception ex) {
        ex.printStackTrace()
    } finally {
        trace(true)
        println "Total $count Lines ${palavras.size()} Words and ${tags.size()} Tags took ${(now - start) / 1000} seconds."
        println "Total de PALAVRAS UNICAS: " + wordStatisticMap.size()

    }

def populateTagMap() {
    ExecutionResult result
    try {
        println "Getting all tags from GraphDB"
        Transaction tx = db.beginTx()
        result = engine.execute('MATCH (n:MacMorphoTag) return n.tag as tag , n.uid as uid')
        for (Map<String, Object> row : result) {
            tagMap.put(row.get('uid'), row.get('tag'))
        }
        //println result.dumpToString()
        /*
        for (Map.Entry<Integer, String> entry : tagMap.entrySet()) {
            println entry.getKey()
            println entry.getValue()
        }
        */
        tx.success()
        println tagMap.size() + "  tags received and stored on tagMap."
    } catch (Exception e) {
        e.printStackTrace()
    }
}

def String getHitCountQuery(word){
    String cleanWord = word
    cleanWord = cleanWord.replaceAll('"',"")
    cleanWord = cleanWord.replace("\\", "\\\\");
    StringBuilder builder = new StringBuilder()
    builder.append("MATCH (n:Palavra)-[:HAS_A]-(g:TagGroup)")
    builder.append(" WHERE n.word =").append("\"").append(cleanWord).append("\"")
    builder.append(" WITH count(n) AS hitNumber, g.group[1] as tagId RETURN hitNumber, tagId")
    return builder.toString()
}

def WordStatistic populateStatistic(word, queryResult){
    def WordStatistic wordStatistic = new WordStatistic()

    // wordStatistic.word = word

    for (Map<String, Object> row : queryResult) {
        wordStatistic.totalHits += row.get('hitNumber')

        wordStatistic.tagStatistic.add(
                new TagStatistic(
                        getTagNameById(row.get('tagId')),
                                row.get('hitNumber'),
                                row.get('tagId')
                        )
        )
    }

    for (TagStatistic tagStats : wordStatistic.tagStatistic) {
       def percentage = ((tagStats.hitNumber * 100) / wordStatistic.totalHits )
       tagStats.percentageUsed = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.CEILING)
       // Round the result to use 2 decimal
       //tagStats.percentageUsed = tagStats.percentageUsed

    }

    return wordStatistic
}

def TagList getTagNameById(tagId){
    String tagName = "UNKNOWN"

    if(tagMap != null && tagId != null){
        Long longId = tagId
        tagName = tagMap.get(longId)
//        println "TAG FOUND is: " + tagName
    }
    return TagList.valueOf(tagName)
}

def createStatsJson(obj){

    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    String json = gson.toJson(obj);

    String location = "C:\\temp\\StatsJson.txt"

    saveStringToFile(json, location, StandardCharsets.UTF_8);

}

def saveStringToFile(String data, String location, Charset charset){
    File file = new File(location);
    try {
        FileUtils.writeStringToFile(file, data, charset);
        System.out.println("The file is located on:");
        System.out.println(file.getAbsolutePath());
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}