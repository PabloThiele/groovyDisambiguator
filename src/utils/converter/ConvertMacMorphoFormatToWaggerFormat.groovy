package utils.converter

import org.apache.commons.io.FileUtils

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Created by Pablo_Thiele on 11/20/2014.
 */
def macMorphoFilePath = this.getClass().getResource( '/resources/macmorpho-test.txt' ).getPath()

def macMorphoFile = new File(macMorphoFilePath)

breakLine = System.getProperty("line.separator");
sentenceCount = 1
dataBuilder = new StringBuilder()

addNewSentence(sentenceCount)

macMorphoFile.eachLine("UTF-8", {
    // Split by space
    wordList = it.split(/\s/)
    //wordList = wordList.findAll { it !=~/.*[^._PU]/ }
    wordList = wordList - wordList.findAll{ it =~ /[^.]_PU/ }
    addLinesBasedOnList(wordList)
})

saveStringToFile(dataBuilder.toString(), 'C:\\temp\\waggerFormat.txt', StandardCharsets.UTF_8)

def addNewSentence(count){
    sentenceCount++
    dataBuilder.append('S#').append(count).append(breakLine)
}

def addLinesBasedOnList(wordList){
    def pattern = ~/[.]_PU/
    def newSentence = false

    wordList.each { word ->
        if(pattern.matcher(word).matches() && word == wordList.last() ){
            addNewSentence(sentenceCount)
            newSentence = true
        }
        if(!newSentence){
            wordTag = word.split('_')
            word = wordTag[0].toString()
            dataBuilder.append('\t').append(word.toLowerCase()).append(' ').append(wordTag[1]).append(breakLine)
        }
        newSentence = false
    }
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