package alej.prog.vahalik;
import javax.swing.*;
import java.awt.*;
/** Třída RecordApp se stará o zobrazení celé aplikace.*/
public class RecordApp extends JFrame
{
   public RecordApp(){
      setTitle("Java Midi Recorder");
      setSize(1000,500);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Recorder recorder = new Recorder();

      JPanel panel = recorder;
      PaintReceiver painter = recorder.getPaintReceiver();
      if(painter != null){
      painter.colorInit();
   }
   setLayout(new GridLayout(2,1));
   this.add(panel);
   if(painter != null){
      this.add(painter);   
   }
    
  }
	
  public static void main(String [] args){
    JFrame frame = new RecordApp();
    frame.setVisible(true);
    frame.setResizable(false);
   }

}
