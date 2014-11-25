package comparator

/**
 * Created by Pablo_Thiele on 11/25/2014.
 */
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import utils.model.WordTag

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

@GrabResolver(name="neo4j", root="http://m2.neo4j.org/")
@GrabResolver(name="restlet", root="http://maven.restlet.org/")
@Grab('org.neo4j:neo4j:2.1.5')

def slurper = new JsonSlurper()

def disambiguatedFilePath = this.getClass().getResource( '/resources/myRandomDisambiguation_2014-11-25_04-47-13.txt' ).getPath()
//def disambiguatedFilePath = this.getClass().getResource( '/resources/myBiasedDisambiguation_2014-11-25_11-01-14.txt' ).getPath()
def originalFilePath = this.getClass().getResource( '/resources/macmorpho-test.txt' ).getPath()
//def jsonFile = this.getClass().getResource( '/resources/json_test.txt' ).getFile()

def disambiguatedFile = new File(disambiguatedFilePath)
def originalFile = new File(originalFilePath)

count = 0
matchCount = 0
notMatchCount = 0
time = start = System.currentTimeMillis()

def trace(output) {
    if (output || ++ count % 1000 == 0) {
        now = System.currentTimeMillis()
        println "$count words ${(now-time)} ms - With " + matchCount + ' corrected tags and '+ notMatchCount + ' differ tags  ' //-- Percentual is: ' + (match * 100 ) / count +'% of matched values and ' + (notMatch * 100) / count  + '% not matched summing on: '+ ((notMatch * 100) / count + (match * 100) / count)
        time = now
    }
}

println 'Reading  files from ' + disambiguatedFilePath + ' and comparing to: ' +  originalFilePath

println "Reading  tagged files from " + disambiguatedFilePath
def sentencePattern = ~/.?S#[0-9]+/
def row  = ''
def disambiguatedData = new StringBuilder()
wordTagList = new ArrayList<WordTag>()
index = 0

try {
    originalFile.eachLine(StandardCharsets.UTF_8.name(), {

        row = it

        wordList = getWordList(row)
        puList = wordList.findAll { it =~ "_PU" }
        words = wordList - puList
        def values = null

        words.each {
            values = it.split('_')
            wordTagList.add( new WordTag(values[0], values[1]))
        }
    })

    String dateTime = new SimpleDateFormat("_yyyy-MM-dd_hh-mm-ss'.txt'").format(new Date());
    println 'Total de word tag list: ' + wordTagList.size()

} catch (Exception ex) {
    ex.printStackTrace()
} finally {
    trace(true)
}

try {
    disambiguatedFile.eachLine(StandardCharsets.UTF_8.name(), {

        row = it
        // Ignore sentence token lines
        if(!sentencePattern.matcher(row).matches()){
            // Is needed to disambiguate? -- If wordCount > 2 , Yes
            // println "Actual row: " + row
            wordList = getWordList(row)
            wordCount = wordList.size()
            count++
            // println "row count is: " + wordCount
            if(wordCount != 2){
                throw new IllegalAccessException(" Cannot find multiple tags on disambiguated file!!!")
            }else{
                row = getComparedRow(wordList)
            }
        }
        disambiguatedData.append(row).append('\n')
    })
    println 'All file was disambiguated'
    println 'Creating new disambiguated file'

    String dateTime = new SimpleDateFormat("_yyyy-MM-dd_hh-mm-ss'.txt'").format(new Date());

    saveStringToFile(disambiguatedData.toString(), 'C:\\temp\\myComparation' + dateTime, StandardCharsets.UTF_8)
    println 'Created new merged/compare file'

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
            .collect {it}
            .join('').tokenize(' ')

    return words
}

def getComparedRow(wordList){
    // Get the word
    def match = false
    def word = wordList.get(0).trim()
    def biasedTag = wordList.get(1).trim()

    def taggedWordTag = wordTagList[index]
    def foundTag = null
    def actualIndex = index

    if(index < wordTagList.size()) {

        def indexPlus1 = index + 1
        def indexPlus2 = index + 2
        def indexPlus3 = index + 3
        def indexPlus4 = index + 4
        def indexPlus5 = index + 5
        def indexPlus6 = index + 6
        def indexPlus7 = index + 7
        def indexPlus8 = index + 8
        def indexPlus9 = index + 9
        def indexPlus10 = index + 10

        if (word.equalsIgnoreCase(wordTagList[index].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index++
        } else if ( indexPlus1 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus1].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus1 + 1
        } else if ( indexPlus2 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus2].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus2 + 1
        } else if ( indexPlus3 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus3].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus3 + 1
        } else if ( indexPlus4 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus4].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus4 + 1
        } else if ( indexPlus5 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus5].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus5 + 1
        } else if ( indexPlus6 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus6].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus6 + 1
        } else if ( indexPlus7 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus7].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus7 + 1
        } else if ( indexPlus8 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus8].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus8 + 1
        } else if ( indexPlus9 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus9].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus9 + 1
        } else if ( indexPlus10 < wordTagList.size() && word.equalsIgnoreCase(wordTagList[indexPlus10].word.replace('-', ''))) {
            foundTag = taggedWordTag.tag
            index = indexPlus10 + 1
        } else {
            println 'not equals - original was: ' + taggedWordTag.tag + ' biased was: ' +biasedTag
        }

    } else {
        println 'got the limits using'
        println index
        println wordList
        println wordTagList[index]
    }

    if(biasedTag.equalsIgnoreCase(foundTag)){
      //  println 'Tags matched!'
        match = true
    }else {
      //  println 'Differ tags found !'
    }

    if(match){
        matchCount++
        return  '\t' + word + ' ' + foundTag + ' MATCH'
    }else{
        notMatchCount++
        return  '\t' + word + ' original_tag: ' + taggedWordTag.tag + ' biased_tag: '+ biasedTag + ' DO_NOT_MATCH'
    }
}