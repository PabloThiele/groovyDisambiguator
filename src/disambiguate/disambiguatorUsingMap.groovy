package disambiguate

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import utils.enums.TagList
import utils.model.TagStatistic

import java.math.RoundingMode
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.SimpleDateFormat

@GrabResolver(name="neo4j", root="http://m2.neo4j.org/")
@GrabResolver(name="restlet", root="http://maven.restlet.org/")
@Grab('org.neo4j:neo4j:2.1.5')

def slurper = new JsonSlurper()

def jsonFilePath = this.getClass().getResource( '/resources/prettyStatisticsJson.txt' ).getPath()
def taggedFilePath = this.getClass().getResource( '/resources/lastTagged.txt' ).getPath()
//def jsonFile = this.getClass().getResource( '/resources/json_test.txt' ).getFile()

def jsonFile = new File(jsonFilePath)
def taggedFile = new File(taggedFilePath)

defaultPercentageValue = 0.1
randomDisambiguation = false
notAbleToDisambiguateCount = 0
oneTagWordCount = 0
disambiguatedWordCount = 0
tagFromStatisticDontExistsOnTaggedFileCount = 0

random = new Random();

count = 0
time = start = System.currentTimeMillis()

def trace(output) {
    if (output || ++ count % 1000 == 0) {
        now = System.currentTimeMillis()
        println "$count words ${(now-time)} ms"
        time = now
    }
}

println "Reading  files from " + jsonFilePath
    try {
        jsonStats = slurper.parse(jsonFile, StandardCharsets.UTF_8.name())
        println "All statistics populated on jsonStats"
    } catch (Exception ex) {
        ex.printStackTrace()
    }

    def first = false
    println "Reading  tagged files from " + taggedFilePath
    def sentencePattern = ~/.?S#[0-9]+/
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
        println 'Creating new disambiguated file'

        String dateTime = new SimpleDateFormat("_yyyy-MM-dd_hh-mm-ss'.txt'").format(new Date());

        if(randomDisambiguation){

            saveStringToFile(disambiguatedData.toString(), 'C:\\temp\\myRandomDisambiguation' + dateTime, StandardCharsets.UTF_8)
            println "Created new random disambiguated file"
        } else {
            saveStringToFile(disambiguatedData.toString(), 'C:\\temp\\myBiasedDisambiguation' + dateTime, StandardCharsets.UTF_8)
            println "Created new biased disambiguated file"
        }
    } catch (Exception ex) {
        ex.printStackTrace()
    } finally {

        println 'Words that not found on stats - and not disambiguated by statistic (used random instead): ' + notAbleToDisambiguateCount
        println 'Words with only one tag  - and not needed to disambiguate: ' + oneTagWordCount
        println 'Words that have multiple tags  found on stats - WAS disambiguated: ' + disambiguatedWordCount
        println 'Words that have multiple tags  found on stats - BUT NO TAG FOUND ON TAGGED FILE (Stats have a tag that don\'t was tagged by wagger): ' + tagFromStatisticDontExistsOnTaggedFileCount
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

def getBiasedDisambiguatedRow(wordList){
    // Get the word
    def word = wordList.get(0)
    def tags = wordList - word
    return  word + ' ' + biasedDisambiguation(tags, jsonStats.getAt(word))
}

def String biasedDisambiguation(tags, statistics){
    if(statistics != null){
        // println "Word exists on stats: " + statistics
        return getTagByStatistic(tags, statistics.tagStatistic)
        /*
        if(statistics.tagStatistic.size() > 1){
            // Multiple tags found, disambiguate
            return getTagByStatistic(tags, statistics.tagStatistic)
        } else {
           // Only one tag found, use it
            oneTagWordCount++
           return statistics.tagStatistic[0].tagName
        }*/
    } else {
        //println 'Word not found on statistics - Not able to disambiguate --> Used random to pick one tag'
        notAbleToDisambiguateCount++
        return randomDisambiguate(tags)
    }
}

def getTagByStatistic (tags, statistics){

    def adjustedStatistics = getAdjustedStatistics(statistics)
    //def totalPercentage = adjustedStatistics.sum{it.percentageUsed}
    def randomDouble = random.nextDouble()
    def percentageSum = 0
    def tagMap = getTagMap(tags)
    def tagResult = null

    for (i = 0; i < adjustedStatistics.size(); i++) {
        percentageSum += adjustedStatistics[i].percentageUsed;
        percentageSum =  BigDecimal.valueOf(percentageSum).setScale(2, RoundingMode.CEILING)

        if (randomDouble <= percentageSum) {
            // Matched tag, so we need to verify if it's exists on tagMap
            if(tagMap.getAt(adjustedStatistics[i].tagName) != null){
                disambiguatedWordCount++
                //return adjustedStatistics[i].tagName
                tagResult = adjustedStatistics[i].tagName
                break
            }//else{
            //}
        }
    }

    if(tagResult == null){
        tagResult = randomDisambiguate(tags)
        tagFromStatisticDontExistsOnTaggedFileCount++

    }

    return tagResult
}

def getTagMap(tagList){
    def tagMap = [:]
    tagList.each{
        tagMap.put(it,it)
    }
    return tagMap
}

def getAdjustedStatistics(statistics){
    if(statistics.size > 1){
        return statistics
    } else {
        if(statistics[0].tagName == 'N'){
            tagsToUse = ['V', 'ADJ']
        } else if(statistics[0].tagName == 'V'){
            tagsToUse = ['N', 'ADJ']
        } else if(statistics[0].tagName == 'ADJ'){
            tagsToUse = ['V', 'N']
        } else {
            tagsToUse = ['V', 'N', 'ADJ']
        }

        defaultStats = getDefaultTagStatistics(tagsToUse)

        statistics[0].percentageUsed = statistics[0].percentageUsed - defaultPercentageValue
        return statistics + defaultStats
    }
}

def getDefaultTagStatistics(tagsToUse){

    List<TagStatistic> defaultTags = new ArrayList<TagStatistic>()

    for(int i=0; i < tagsToUse.size(); i++){
        defaultTags.add(new TagStatistic(TagList.valueOf(tagsToUse[i]), 1L, 0, defaultPercentageValue / tagsToUse.size()))
    }
    return defaultTags
}