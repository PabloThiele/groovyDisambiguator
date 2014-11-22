package utils.converter

import org.apache.commons.io.FileUtils

import java.nio.charset.Charset

/**
 * Created by Pablo_Thiele on 11/20/2014.
 */
def jsonFilePath = this.getClass().getResource( '/resources/StatsJson.txt' ).getPath()
def jsonFilePathUTF8 = this.getClass().getResource( '/resources/StatsJsonUTF8.txt' ).getPath()

def f=new File(jsonFilePath).getText('ISO-8859-1')


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