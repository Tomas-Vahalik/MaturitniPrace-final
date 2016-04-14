package alej.prog.vahalik;
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
/**Třída PaintReceiver vykresluje stisknutí klávesy na obrazovku. Vždy, když zachytí MIDI událost z třídy
 * <a href="Recorder.html">Recorder</a>,
 * ujistí se, že daná událost vyjadřuje stisknutí klávesy a nakreslí obdélník na obrazovku. Barva obdélníku závisí na tom, v jaké oktávě leží stisklá klávesa. Jeho výška je daná silou úderu.**/
public class PaintReceiver extends Canvas implements Receiver{

  private int key = 0;
  private int octave = 0;
  private int [] keys = new int [88];
  private int lineY = 200;
  
  private Color [] colors  = new Color [9];
    
  private int screenSize = 1000;
  private int collumSize = screenSize/88;
  
  /** Dostává MIDI události a ujišťuje se, jestli se jedná o stisknutí klávesy */ 
  public void send(MidiMessage message, long timeStamp){
   //když receiver dostane MIDI event, zjistí jaká klávesa byla stisknuta a podle toho vykreslí obdélník
    byte[] info = message.getMessage();  
      
     if(info[0] == -112 && info[2] !=0 && info[1] >= 21){
       key = info[1];
        keys [key-21]= info[2];
        repaint();        
    } 
  
  }
  /** Vykresluje odelníky na obrazovku*/
  
   public void paint(Graphics g){
   
    if(key <=109 && key >= 21){   
   
     g.drawLine(0,lineY,screenSize,lineY);
     g.drawLine(0,0,screenSize,0);
          
     for(int i = 0; i < keys.length; i++){
       //obdélník bude barevný podle toho, v jaké oktávě leží stisknutá klávesa
        g.setColor(getColorForOctave(i+21));
        int collumHeight = 2*keys[i];
        g.fillRect(collumSize*i,lineY-collumHeight,collumSize,collumHeight);
       
        if(keys[i]>=2){
          keys[i]-=2;
        }
        else{
         keys[i] = 0;
        }     
      }     
    }    
  }
   /** Rozhodne o barvě obdelníku*/
   public Color getColorForOctave(int k){
         octave = (k / 12)-1;
         return colors[octave];
   } 
   public void close(){
  
  }  
  public void colorInit(){
    colors[0]= new Color(0,0,0);
    colors[1]= new Color(51,0,255);
    colors[2]= new Color(0,153,153);
    colors[3]= new Color(255,0,51);
    colors[4]= new Color(153,153,102);
    colors[5]= new Color(255,204,0);
    colors[6]= new Color(51,204,255);
    colors[7]= new Color(102,255,0);
    colors[8]= new Color(255,255,0);
 
  }

}
