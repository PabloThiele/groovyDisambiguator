package statistics

import org.apache.commons.io.FileUtils
import utils.model.WordTag

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

/**
 * Created by Pablo_Thiele on 11/26/2014.
 */

def tagMapStats = [:]
def wordsToCheck = [
        'DE'      ,
        'De'      ,
        'de'      ,
        'DA'      ,
        'Da'      ,
        'da'      ,
        'Das'     ,
        'das'     ,
        'Do'      ,
        'do'      ,
        'DOS'     ,
        'Dos'     ,
        'dos'     ,
        'Em'      ,
        'em'      ,
        'Na'      ,
        'na'      ,
        'Nas'     ,
        'nas'     ,
        'No'      ,
        'no'      ,
        'Nos'     ,
        'nos'     ,
        'A'       ,
        'a'       ,
        'As'      ,
        'as'      ,
        'O'       ,
        'o'       ,
        'Os'      ,
        'os'      ,
        'Pelo'    ,
        'pelo'    ,
        'Por'     ,
        'por'     ,
        'Para'    ,
        'para'    ,
        'Pelos'   ,
        'pelos'   ,
        'Pela'    ,
        'pela'    ,
        'Pelas'   ,
        'pelas' ]

def disambiguatedFilePath = this.getClass().getResource( '/resources/myBiasedDisambiguation_2014-11-25_11-01-14.txt' ).getPath()

def disambiguatedFile = new File(disambiguatedFilePath)

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

println "Reading  tagged files from " + disambiguatedFilePath
def sentencePattern = ~/.?S#[0-9]+/
def row  = ''
def disambiguatedData = new StringBuilder()

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

                def String word = wordList.get(0).trim()
                def String tag  = wordList.get(1).trim()

                if(wordsToCheck.contains(word)){
                    Map map = tagMapStats[word]
                    if(map != null ){
                        if(map[tag] != null){
                            def count = map[tag] + 1
                            map[tag] = count
                        } else {
                            map.put(tag,1)
                        }

                    } else {
                        tagMapStats.put(word , [ (tag) : 1])
                    }
                }
            }
        }

    })

    tagMapStats.each {
        println it
    }
    println 'All file was read'
    println 'Creating new disambiguated report file'

    String dateTime = new SimpleDateFormat("_yyyy-MM-dd_hh-mm-ss'.txt'").format(new Date());

    //saveStringToFile(disambiguatedData.toString(), 'C:\\temp\\statsReport' + dateTime, StandardCharsets.UTF_8)

} catch (Exception ex) {
    ex.printStackTrace()
} finally {

    trace(true)
}

def getWordList(stringOriginal){

    List<String> words = stringOriginal
    // It will collect all chars from string and tokenize by space
            .collect {it}
            .join('').tokenize(' ')

    return words
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