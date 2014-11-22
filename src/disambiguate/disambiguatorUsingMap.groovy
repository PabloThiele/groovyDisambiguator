package disambiguate

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@GrabResolver(name="neo4j", root="http://m2.neo4j.org/")
@GrabResolver(name="restlet", root="http://maven.restlet.org/")
@Grab('org.neo4j:neo4j:2.1.5')

def slurper = new JsonSlurper()

def jsonFilePath = this.getClass().getResource( '/resources/StatsJson.txt' ).getPath()
def taggedFilePath = this.getClass().getResource( '/resources/lastTagged.txt' ).getPath()
//def jsonFile = this.getClass().getResource( '/resources/json_test.txt' ).getFile()

def jsonFile = new File(jsonFilePath)
def taggedFile = new File(taggedFilePath)

randomDisambiguation = true
random = new Random();

count = 0
time = start = System.currentTimeMillis()

def trace(output) {
    if (output || ++ count % 1000 == 0) {
        now = System.currentTimeMillis()
        println "$count rows ${(now-time)} ms"
        time = now
    }
}

println "Reading  files from " + jsonFilePath
    try {
        jsonStats = slurper.parse(jsonFile, StandardCharsets.UTF_8.name())
        println "All statistics populated on jsonStats"
    } catch (Exception ex) {
        ex.printStackTrace()
    } finally {
        trace(true)
    }

    def first = false
    println "Reading  tagged files from "
    def sentencePattern = ~/.?S#[0-9]+/
    def wordCountPattern = ~/\\w+/
    def row  = ''
    def disambiguatedData = new StringBuilder()

    try {
        taggedFile.eachLine(StandardCharsets.UTF_8.name(), {

            row = it

            // Ignore sentence token lines
            if(sentencePattern.matcher(it).matches()){
                /*
                println  'New sentence: ' + row

                if(first){
                    System.exit(1)
                }
                first = true*/
            } else {
            // Is needed to disambiguate? -- If wordCount > 2 , Yes
               // println "Actual row: " + row
                wordList = getWordList(row)
                wordCount = getWordList(row).size()

               // println "row count is: " + wordCount
                if(wordCount > 2){
                    if(randomDisambiguation){
                        row = getRandomDisambiguatedRow(wordList)
                    } else {
                        row = getBiasedDisambiguatedRow(wordList)
                    }
                    disambiguatedData.append('\t')
                }
            }
            disambiguatedData.append(row).append('\n')
        })
        println 'All file was disambiguated'
        println 'Creating file'

        if(randomDisambiguation){
            saveStringToFile(disambiguatedData.toString(), 'C:\\temp\\myRandomDisambiguation.txt', StandardCharsets.UTF_8)
        } else {
            saveStringToFile(disambiguatedData.toString(), 'C:\\temp\\myBiasedDisambiguation.txt', StandardCharsets.UTF_8)
        }

        println 'File created on:  C:\\temp\\mydisambiguation.txt'
    } catch (Exception ex) {
        ex.printStackTrace()
    } finally {
        trace(true)
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

def getWordList(stringOriginal){

    List<String> words = stringOriginal
    // It will collect all chars from string and tokenize by space
            .collect { it.charAt(0).letterOrDigit || it.charAt(0) == '+' ? it : ' ' }
            .join('').tokenize(' ')

    return words
}

def getRandomDisambiguatedRow(wordList){

    // Get the word
    def word = wordList.get(0)

    def tags = wordList - word

    return  word + ' ' + randomDisambiguate(tags)
}

def String randomDisambiguate(tags){
    def i = random.nextInt(tags.size())
    return tags[i]
}