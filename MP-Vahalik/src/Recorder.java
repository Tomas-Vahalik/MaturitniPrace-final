package alej.prog.vahalik;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.net.*;

/**<p> Třída Recorder zajišťuje funkčnost celé aplikace. Stará se především o propojení hudebního nástroje s počítačem
 *a nahrávání zvukových stop do souboru a o vytvoření ovládacích prvků pro aplikaci. 
 * </p>
 * <p>
 *Nahrávání funguje tak, že tato třída zaznamenává MIDI události vysílané připojeným nástrojem a ukládá je do stop.
 *Vždy když zachytí MIDI událost, pošle ji dále do třídy 
 *<a href="PaintReceiver.html">PaintReceiver</a>, která se stará o její vizualizaci.
 *</p>
 *<p>
 *Změny nástroje dosáhneme tak, že na začátek každé stopy vložíme speciální MIDI událost, která říká, jakým nástrojem má být stopa uložena.
 *</p>
 *<p>
 *Při nahrávání více stop se jejich synchronizace dosáhne tak, že se každý MIDI event v nové stopě posune 
     dozadu o čas, který uběhl od začátku nahrávání stopy. Toto posunutí zajišťuje metoda tickMove.
  </p>      
 **/
 
public class Recorder extends JPanel implements ActionListener{
  
  private JLabel listOfDevices;
  private JLabel listOfInstruments;  
  private JLabel statusLabel;
  private JLabel fileLabel;
  private JLabel trackLabel;
    
  private JButton record;
  private JButton stop;
  private JButton play;
  private JButton write;
    
  private JTextField status;
  private JTextField fileField;
  private JTextField trackField;
  
  private JFileChooser fileChoose = new JFileChooser();
  
  private JButton selectFile;
  private JButton createFile;
 
  /** soubor do kterého se bude nahrávat*/
  private File recordFile;
  
  private ImageIcon playImg = new ImageIcon("pictures/play.png");
  private ImageIcon recImg = new ImageIcon("pictures/record.png");
  private ImageIcon stopImg = new ImageIcon("pictures/stop.png");
  private ImageIcon saveImg = new ImageIcon("pictures/save.png");
 
 
  /** timer slouží k tomu, aby se správně vykreslovalo stisknutí kláves*/
  private javax.swing.Timer timer; 
  private int delay = 20;
  
  private JComboBox devices;
  private JComboBox Instruments;
  
  private MidiDevice.Info[] infos;
  private MidiDevice inputDevice;
  private Sequencer sequencer;
     
  private Transmitter transmitter;
  private Receiver receiver;
  private Sequence seq;
  private Track newTrack;
  /** obdrží informaci o stisknutí klávesy a zobrazí ji na obrazovce*/
  private PaintReceiver myReceiver = new PaintReceiver();
  private Transmitter transmitter2;
  
  private Instrument[] instr;
  private int selectedInstrument = 0;
  
  private boolean buttonsAdded = false;
  private boolean canRecord = true;
  
  
  private int tracks = 0;
  /** */
  double devMili = 0;
  private int BPM = 120;
  //****************************************************************************                     
   public Recorder() {
    	addMidi();
      
   }
  //******************************************************************************
  public void actionPerformed( ActionEvent  e){
  
    Object source = e.getSource();
    
    if(source == devices){
       addMidi();
    }
   
    if(source == Instruments){
       selectedInstrument = Instruments.getSelectedIndex();
    }
  
    if (source == record){
      if(canRecord){
	 record();	
      }
		 
    }
   
    if (source == stop){
	 stopRec();
     }
   
    if(source == write){
         write();
                  
    }
   
    if(source == play){       
        play();
       
    }
    if(source == selectFile){
       int returnVal = fileChoose.showOpenDialog(this);
       if (returnVal == JFileChooser.APPROVE_OPTION) {
         recordFile = fileChoose.getSelectedFile(); 
         fileField.setText("Selected file: " + recordFile.getName());
         try{
          seq =  MidiSystem.getSequence(recordFile) ;
          tracks = seq.getTracks().length;
          trackField.setText("" + tracks);
           status.setText("Ready");    
         }
         catch(Exception NotAMidiFile){
          status.setText("This file does not contain any tracks");
          trackField.setText(""); 
         }         
        }    
      
    }
    if(source == createFile){
       int returnVal = fileChoose.showSaveDialog(this);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
           recordFile = fileChoose.getSelectedFile();
           try{
             recordFile.createNewFile();
             fileField.setText("Selected file: " + recordFile.getName()); 
             trackField.setText(""); 
             status.setText("Ready");  
           }
           catch(IOException createFileFail){
             status.setText("Could not create file"); 
           }
       }            
    }     
 
    if(source == timer){
      if(myReceiver != null){
       myReceiver.repaint();
      }      
    }
	}
  /**Zahájí nahrávání do zvoleného souboru. */
  public void record(){    
                 
     try{                 
       if(recordFile != null){
          status.setText("recording");
          seq = new Sequence(Sequence.PPQ,24);
          seq =  MidiSystem.getSequence(recordFile) ;
          tracks = seq.getTracks().length;              
       }
       else{
          status.setText("Please select a file to record");
          JOptionPane.showMessageDialog(this, "Please select a file to record ","File not selected", JOptionPane.ERROR_MESSAGE);
          return;
       }       
      }
      catch(Exception IOE){          
      }
      
      try{
           
        sequencer.setSequence(seq);
        //vytvoří se nová stopa     
        newTrack = seq.createTrack();        
        //na začátek stopy se zadá informace, jakým nástrojem bude stopa nahrávána
         ShortMessage instrumentChange = new ShortMessage();
         instrumentChange.setMessage(ShortMessage.PROGRAM_CHANGE, 0, selectedInstrument,0);
         newTrack.add(new MidiEvent(instrumentChange,0));
        
         //začne se nahrávat
         sequencer.setTickPosition(0); 
     
         sequencer.recordEnable(newTrack, -1);
         sequencer.startRecording();
         //uloží se čas, v jakém se začalo nahrávat
         devMili = (double)inputDevice.getMicrosecondPosition()/1000;    
      
     }   
     catch(Exception IMDE){
      status.setText("Could not select this instrument");
      
     }
     record.setEnabled(false); 
     write.setEnabled(false);
     play.setEnabled(false);
     stop.setEnabled(true);        
    
  }
  /** Ukončí nahrávání nebo přehrávání*/
  public void stopRec(){
       if(status.getText().equals("recording")){
          write.setEnabled(true);
        }
        if(canRecord){
         status.setText("Ready"); 
        }
        else{
         status.setText("Midi device not suitable for record");
        }
        stop.setEnabled(false); 
        record.setEnabled(true); 
        play.setEnabled(true);
    
       //přestane se nahrávat            
       sequencer.stop();             
       sequencer.recordDisable(newTrack);
       //nahraná stopa se posune tak, aby byla souběžně s ostatními  
       tickMove(newTrack);
      //převede se na jiný kanál, aby mohla být přehrána jiným nástrojem             
       channelMove(newTrack,tracks); 
    
  }
  /** Zapíše nahraná data do souboru*/
  public void write(){
        //Nahraná sekvence se zapíše do souboru 
        Sequence tmp = sequencer.getSequence();             
        try{             
           MidiSystem.write(tmp, 1, recordFile);
           seq =  MidiSystem.getSequence(recordFile) ;
           tracks = seq.getTracks().length;
           trackField.setText("" + tracks);  
        }
         catch(Exception exc){
             status.setText("File not found");
         }
         write.setEnabled(false);
  }
  /** Přehraje zvolený soubor*/
  public void play(){
        //Přehraje se hudba ze souboru
        try{       
        sequencer.open();
        
        InputStream is = new BufferedInputStream(new FileInputStream(recordFile));
       
        sequencer.setSequence(is);
        sequencer.setTickPosition(0);       
       
	sequencer.start();
        status.setText("playing");
             
       }
        catch(Exception exce){
        status.setText("Please select a file with at least 1 midi track in it ");
         JOptionPane.showMessageDialog(this, "Please select a file with at least 1 midi track in it ","Unable to play", JOptionPane.ERROR_MESSAGE);
        }
        stop.setEnabled(true);
        write.setEnabled(false);
  }
 //******************************************************************** 
  public void addMidi(){
    infos = MidiSystem.getMidiDeviceInfo();
     
     try{
       instr = MidiSystem.getSynthesizer().getDefaultSoundbank().getInstruments();
       
       if(!buttonsAdded){ 
        addButtons();       
       }
       if(inputDevice == null){  
        inputDevice = MidiSystem.getMidiDevice(infos[1]);
       }
       else{
        inputDevice = MidiSystem.getMidiDevice(infos[devices.getSelectedIndex()]);       
       }
       sequencer = MidiSystem.getSequencer();
       
       transmitter = inputDevice.getTransmitter();
       receiver = sequencer.getReceiver();
       transmitter.setReceiver(receiver);
       seq = new Sequence(Sequence.PPQ,24); 
       
       sequencer.setSequence(seq);
       sequencer.setTickPosition(0);
       
       inputDevice.open();
       sequencer.open();  
     
       transmitter2 = inputDevice.getTransmitter();
       transmitter2.setReceiver(myReceiver);       
          
       canRecord = true;
       status.setText("Ready");         
     }      
    catch(Exception e){
      status.setText("Midi device not suitable for record");
      canRecord = false;
     
    }    
  
  }  
 //************************************************************************
 /** Vytvoří tlačítka a ostatní ovládací prvky aplikace*/
 public void addButtons(){
    this.setLayout(new BorderLayout());     
    
    //Panels:
    JPanel topPanel = new JPanel();		
    topPanel.setLayout(new GridLayout(0,2,0,15)); 
      
    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new FlowLayout( FlowLayout.CENTER ));
     
    JPanel devicePanel = new JPanel();
    devicePanel.setLayout(new FlowLayout( FlowLayout.LEFT,30,10));
   
    
    JPanel filePanel = new JPanel();
    filePanel.setLayout(new FlowLayout( FlowLayout.LEFT,30,10));
    
    JPanel instrumentPanel = new JPanel();     
    instrumentPanel.setLayout(new FlowLayout( FlowLayout.LEFT,30,0));
    
    JPanel fileSelectPanel = new JPanel();
    fileSelectPanel.setLayout(new FlowLayout( FlowLayout.LEFT,85,0));
    
    JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new FlowLayout( FlowLayout.LEFT,30,0));
    
     JPanel trackPanel = new JPanel();
     trackPanel.setLayout(new FlowLayout( FlowLayout.LEFT,30,0));
     
   //Labels: 
    statusLabel = new JLabel("Status: ");          
    listOfDevices = new JLabel("Select a device: ");
    listOfDevices.setPreferredSize(new Dimension(130,20));     
    listOfInstruments = new JLabel("Select an instrument: ");
    listOfInstruments.setPreferredSize(new Dimension(130,20));     
    fileLabel = new JLabel("File: ");
    trackLabel = new JLabel("Tracks in this file");
    
    //Combo boxes: 
    devices = new JComboBox();
    for(MidiDevice.Info i : infos){
      devices.addItem(i.getName());
    }
    devices.setSelectedIndex(1);
    devices.addActionListener(this);
    devices.setPreferredSize(new Dimension(200,20));
    
    Instruments = new JComboBox();
     if(instr != null){
      for(Instrument i : instr){
        Instruments.addItem(i.getName());
      }
     }
      
     Instruments.addActionListener(this);
     Instruments.setPreferredSize(new Dimension(200,20));
          
    //Buttons:
    record = new JButton("",recImg);
    record.addActionListener(this);
    
    stop = new JButton("",stopImg);
    stop.addActionListener(this);
    stop.setEnabled(false);
    
    play = new JButton("",playImg);
    play.addActionListener(this);
    
    write = new JButton("",saveImg);
    write.addActionListener(this);
    write.setEnabled(false);
    
    selectFile = new JButton("select");
    selectFile.addActionListener(this);
    createFile = new JButton("create");
    createFile.addActionListener(this);
    
    File workingDirectory = new File(System.getProperty("user.dir"));
    fileChoose.setCurrentDirectory(workingDirectory); 
    
    // Text fields   
    status = new JTextField("Ready", 30);
    status.setEditable(false);
    
    fileField = new JTextField("", 20);
    fileField.setEditable(false);
    
    trackField = new JTextField("",3);
    trackField.setEditable(false);
       
   
    // timer
    timer = new javax.swing.Timer(delay, this);
    timer.start();
//********************************************************************  
    devicePanel.add(listOfDevices);
    devicePanel.add(devices);    
      
    filePanel.add(fileLabel);
    filePanel.add(fileField);     
      
    instrumentPanel.add(listOfInstruments);
    instrumentPanel.add(Instruments); 
      
    fileSelectPanel.add(selectFile);
    fileSelectPanel.add(createFile); 
     
    statusPanel.add(statusLabel);
    statusPanel.add(status);   
      
    trackPanel.add(trackLabel);
    trackPanel.add(trackField);

    topPanel.add(devicePanel);    
    topPanel.add(filePanel);
    topPanel.add(instrumentPanel);    
    topPanel.add(fileSelectPanel);
    topPanel.add(statusPanel);
    topPanel.add(trackPanel);                             
    
    bottomPanel.add(record);
    bottomPanel.add(stop);
    bottomPanel.add(write);
    bottomPanel.add(play); 
    
    this.add(topPanel,BorderLayout.PAGE_START);
    this.add(bottomPanel);    
      
    buttonsAdded = true;
    myReceiver.repaint();
 
 }
 //************************************************************************
 /** Slouží k tomu, aby se nahrané stopy uložily souběžně vedle sebe.
  *Toho se docílí tak, že se každý MidiEvent ve stopě posune o čas, ve který se stopa začala nahrávat.
  *@param t stopa, kterou chceme posunout
  **/
 public void tickMove(Track t){
         
     double pos_mili = 0;
     double pos_tick = 0;
     double tmin = BPM*24;
     double tmili = tmin/60000;
             
     double newMili = 0;
     double newTickd = 0; 
     int newTick = 0; 
    
             
     for(int i = 1; i < t.size();i++){
       
        pos_tick = t.get(i).getTick();
        pos_mili = pos_tick/tmili;
          
        newMili = pos_mili - devMili;
        newTickd = newMili * tmili;
        newTick = (int)newTickd;
                    
        t.get(i).setTick((long)newTick-8);
                       
     } 
 } 
 
 //**********************************************************
 /** Převede nahranou stopu na jiný kanál, aby mohla být přehrána jiným nástrojem 
  *@param t stopa, kterou chceme posunout
  *@param ch kanál, na který chceme stopu posunout
  **/
 public void channelMove(Track t,int ch){
            
      for(int i = 0; i < t.size();i++){
        
          long tick =  t.get(i).getTick();
          MidiMessage message =  t.get(i).getMessage();
          if (message instanceof ShortMessage){
            ShortMessage msg = (ShortMessage) message;
            int command =   msg.getCommand();
            int data1 =  msg.getData1();
            int data2 = msg.getData2();
            try{         
              
               msg.setMessage(command, ch, data1, data2);
            }
            catch(InvalidMidiDataException setMessageFail){
              status.setText("Reached maximum number of tracks");
            }
          
          }
     }  
 }
 //*************************************************************
 public PaintReceiver getPaintReceiver(){
  return myReceiver;
 }

}
