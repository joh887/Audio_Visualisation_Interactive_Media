import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.text.DecimalFormat; 
import controlP5.*; 
import beads.*; 
import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Building11AudioVisualisation extends PApplet {



// Day to be simulated as a string (yyyy-mm-dd)
// Must have default value
String dayString = "2021-09-28";

// DataStores are normalised tables to store the data in
DataStore airTemp;
DataStore humidity;
DataStore peopleIn;
DataStore peopleOut;
DataStore rainAmount;

// State indicates the state of the application as a whole.
enum State {
  DateSelect, Loading, Play, FadeOut
};
State state = State.DateSelect;

int frame = 0;  // The frame of the day's simulation (0 - 1440 where 0 = 00:00 and 1339 = 23:59 )
float frameFloat = 0.0f; // Frame represented as a float to keep track of frame when using a length multiplier.

// COLOURS
int clrBackground = 0;
int clrBackgroundAccent = 20;
int clrForeground = 255;

// Controls

ControlP5 cp5;

Slider lengthSlider;
float lengthMultiplier = 1.0f;
Button addMonth;
Button subMonth;
Button addDay;
Button subDay;
Button goButton;
int[] monthDays = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

// Background tower - Paul
PImage img;
PImage personImage;

public void setup() {

  
  frameRate(30);

  img = loadImage("utsbuilding11.png");
  personImage = loadImage("1.png");

  cp5 = new ControlP5(this);
  cp5.addSlider("arpGainSlider")
    .setLabel("Arp Gain")
    .setPosition(20, 20)
    .setSize(100, 20)
    .setRange(0, 100)
    .setValue(100)
    ;

  cp5.addSlider("bassGainSlider")
    .setLabel("Bass Gain")
    .setPosition(220, 20)
    .setSize(100, 20)
    .setRange(0, 100)
    .setValue(100)
    ;

  cp5.addSlider("arpIntensitySlider")
    .setLabel("Temp. Intensity")
    .setPosition(20, 50)
    .setSize(100, 20)
    .setRange(0.8f, 1.2f)
    .setValue(1.0f)
    ;

  cp5.addSlider("humidityIntensitySlider")
    .setLabel("Humidity Intensity")
    .setPosition(220, 50)
    .setSize(100, 20)
    .setRange(0.5f, 2.0f)
    .setValue(1.0f)
    ;


  // SETUP SCREEN

  addMonth = cp5.addButton("addMonthButton")
    .setLabel("+")
    .setPosition(width/2, height/2 - 35)
    .setSize(40, 20);
  ;
  subMonth = cp5.addButton("subMonthButton")
    .setLabel("-")
    .setPosition(width/2, height/2 + 25)
    .setSize(40, 20);
  ;
  addDay = cp5.addButton("addDayButton")
    .setLabel("+")
    .setPosition(width/2 + 60, height/2 - 35)
    .setSize(40, 20);
  ;
  subDay = cp5.addButton("subDayButton")
    .setLabel("-")
    .setPosition(width/2 + 60, height/2 + 25)
    .setSize(40, 20);
  ;
  lengthSlider = cp5.addSlider("speedSlider")
    .setLabel("Simulation Length (seconds)")
    .setPosition(width/2-250, height/2+100)
    .setSize(500, 50)
    .setRange(48, 192)
    .setValue(48)
    ;

  goButton = cp5.addButton("goButton")
    .setLabel("Start!")
    .setPosition(width/2 - 50, height/2 + 200)
    .setSize(100, 50)
    ;
}
boolean failLoading = false;
public void draw() {

  if (failLoading) return;

  background(clrBackground);

  switch (state) {
  case DateSelect : 
    fill(clrForeground);
    drawSetup();
    break;

  case Loading :
    drawDialogueMessage("Loading " + dayString);
    airTemp = new DataStore ("https://eif-research.feit.uts.edu.au/api/csv/?rFromDate=" + dayString + "T00%3A00%3A00&rToDate=" + dayString + "T23%3A59%3A59&rFamily=weather&rSensor=AT", "AirTemp");
    humidity = new DataStore("https://eif-research.feit.uts.edu.au/api/csv/?rFromDate=" + dayString + "T00%3A00%3A00&rToDate=" + dayString + "T23%3A59%3A59&rFamily=wasp&rSensor=ES_B_06_419_7C09&rSubSensor=HUMA", "Humidity");
    peopleIn = new DataStore("https://eif-research.feit.uts.edu.au/api/csv/?rFromDate=" + dayString + "T00%3A00%3A00&rToDate=" + dayString + "T23%3A59%3A59&rFamily=people_sh&rSensor=CB11.PC02.16.JonesStEast&rSubSensor=CB11.02.JonesSt+In", "PeopleIn");
    peopleOut = new DataStore("https://eif-research.feit.uts.edu.au/api/csv/?rFromDate=" + dayString + "T00%3A00%3A00&rToDate=" + dayString + "T23%3A59%3A59&rFamily=people_sh&rSensor=CB11.PC02.16.JonesStEast&rSubSensor=CB11.02.JonesSt+Out", "PeopleOut");
    rainAmount = new DataStore ("https://eif-research.feit.uts.edu.au/api/csv/?rFromDate=" + dayString + "T00%3A00%3A00&rToDate=" + dayString + "T23%3A59%3A59&rFamily=weather&rSensor=RT", "Rain");
    if (failLoading) {
      drawDialogueMessage("Failed loading!");
      return;
    } else {
      initialiseAudio();
      initialiseRain();

      state = State.Play;
      arpGain.setGain(1);
      bassGain.setGain(1);
    }
    break;

  case Play : 

    // Data
    airTemp.checkFrame(frame);
    humidity.checkFrame(frame);
    rainAmount.checkFrame(frame);
    peopleIn.checkFrame(frame);
    peopleOut.checkFrame(frame);

    // Audio
    updateAudio();

    // VISUALS
    // Background
    int timeval = frame;//int((float)list_timevalue.get(index)/60);
    makeTimeColor(timeval);
    background(r, g, b);

    updatePeople();

    frameFloat += 1.0f * lengthMultiplier;
    frame = (int)frameFloat;

    if (frame >= 1440) {
      state = State.FadeOut;
    }
    updateRain();

    // Tower
    image(img, width/2 - 343/2, height - 400, 343, 400);
    strokeWeight(14);
    stroke(0, light, 0);
    line(630, 650, 610, 440);
    line(680, 620, 660, 480);
    line(730, 620, 710, 480);
    noStroke();

    break;

  case FadeOut :
    updateAudio();
    break;
  }


  drawUI();

  // Progress bar
  fill(255);
  rect(0, 0, width * (float)frame/(1440), 10);
}

public void play() {
  drawDialogueMessage("Loading " + dayString);
  lengthMultiplier = map(lengthSlider.getValue(), 48, 192, 1.0f, 0.25f);
  lengthSlider.hide();
  addDay.hide();
  subDay.hide();
  addMonth.hide();
  subMonth.hide();
  goButton.hide();
  state = State.Loading;
}

public void keyPressed() {
  if (key == ENTER) {
    //println("Getting data for date " + dayString);
    play();
  }
  if (key == CODED) {
    if (keyCode == UP) {
      incrementDate(0, 0, 1);
    }
    if (keyCode == DOWN) {
      incrementDate(0, 0, -1);
    }
    if (keyCode == LEFT) {
      incrementDate(0, -1, 0);
    }
    if (keyCode == RIGHT) {
      incrementDate(0, 1, 0);
    }
  }
}

public void drawSetup () {
  textAlign(CENTER, CENTER);
  textSize(32);
  text(dayString, width/2, height/2);
  textAlign(LEFT);
}

public void drawUI() {
  // TOP BAR
  fill(clrBackgroundAccent);
  rect(0, 0, width, 100);

  // TIME
  fill(clrForeground);
  textAlign(RIGHT);
  textSize(32);
  text(frameToTimeString(frame), width - 20, 50);
  textSize(18);
  text(dayStringToWrittenDate(dayString), width - 20, 80);

  // PARAMETER TEXT
  if (state == State.Play) {
    DecimalFormat oneDp = new DecimalFormat("#.#");
    textAlign(LEFT);
    text("Temp.:" + oneDp.format(airTemp.currentValue) + "°C", 20, 90);
    text("Humidity:" + oneDp.format(humidity.currentValue) + "%", 220, 90);
    text("Rain:" + oneDp.format(rainAmount.delta), 420, 90);
  }
}

public void drawDialogueMessage (String s) {
  fill(clrBackgroundAccent);
  rect(width - 400, height - 50, 400, 50);

  fill(clrForeground);
  textSize(16);
  textAlign(LEFT);
  text(s, width - 380, height-25);
}

public String frameToTimeString(int i) {
  int h = i / 60;
  int m = i - h * 60;
  return String.format("%02d", h) + ":" + String.format("%02d", m);
}


final String[] monthWords = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
public String dayStringToWrittenDate(String s) {
  String[] split = s.split("-");
  String date = split[2];
  int dateInt = Integer.parseInt(date);
  // This looks ridiculous, but it's to make sure that the visual date shows single digit numbers without leading zeroes
  // e.g. "04" becomes "4"
  date = Integer.toString(dateInt);


  // Date suffix
  // If 11, 12, 13, just use "th". Otherwise base on the last digit.
  if (dateInt > 10 && dateInt < 14) {
    date = date.trim().concat("th");
  } else {
    switch (dateInt % 10) {
    case 1 : 
      date = date.trim().concat("st"); 
      break;
    case 2 : 
      date = date.trim().concat("nd"); 
      break;
    case 3 : 
      date = date.trim().concat("rd"); 
      break;
    default: 
      date = date.trim().concat("th"); 
      break;
    }
  }

  date = date.concat(" of ");
  date = date.concat(monthWords[Integer.parseInt(split[1]) - 1]);
  date = date.concat(", " + split[0]);

  return date;
}

public void incrementDate (int y, int m, int d) {
  String[] s = dayString.split("-");
  int oldM = Integer.parseInt(s[1]);
  int oldD = Integer.parseInt(s[2]);
  if (oldM + m > 12 || oldM + m < 1 || oldD + d > monthDays[oldM - 1] || oldD + d < 1) {
    return;
  }

  s[0] = Integer.toString(Integer.parseInt(s[0]) + y);
  s[1] = Integer.toString(Integer.parseInt(s[1]) + m);
  s[2] = Integer.toString(Integer.parseInt(s[2]) + d);
  if (Integer.parseInt(s[1]) < 10) s[1] = "0" + s[1];
  if (Integer.parseInt(s[2]) < 10) s[2] = "0" + s[2];
  dayString = s[0] + "-" + s[1] + "-" + s[2];
}



AudioContext ac;

public ArrayList<Float> arpSequence = new ArrayList<Float>();
public ArrayList<Float> bassSequence = new ArrayList<Float>();

int sequenceLength = 6;
int bassSequenceLength = 4;

int seqStep = 0;
int bassSeqStep = 0;

int octave = 2;
float bassOctave = 0.25f;

Gain arpGain;
Gain bassGain;

float filterFrequency;
int noteLength;

float temperatureIntensity = 1.0f;
float humidityIntensity = 1.0f;

float fadeOutMultiplier = 1.0f;

public void initialiseAudio() {
  
  initialiseSoundFiles();
  
  ac = AudioContext.getDefaultContext();

  Clock clock = new Clock(500);
  
  generateNewMelody(arpSequence, sequenceLength);
  generateNewMelody(bassSequence, bassSequenceLength);
  
  arpGain = new Gain(1);
  bassGain = new Gain(1);
  
  ac.out.addInput(arpGain);
  ac.out.addInput(bassGain);
        
  clock.addMessageListener(new Bead () {

    WavePlayer saw = new WavePlayer(440, Buffer.SAW);
    WavePlayer bassWave = new WavePlayer(440, Buffer.TRIANGLE);
    
    BiquadFilter lpf = new BiquadFilter(ac, 1, BiquadFilter.LP);
        
    public void messageReceived(Bead message) {
      
      Clock c = (Clock)message;
      
      if (c.getCount() == 0) {    
        lpf.addInput(saw);
      }
      
      if (c.getCount() % 64 == 0) {
        
        float bassNote = bassSequence.get(bassSeqStep) * bassOctave;
        bassWave.setFrequency(bassNote);
        
        bassSeqStep++;
        if (bassSeqStep >= bassSequenceLength) bassSeqStep = 0;
        
        Envelope gateEnv = new Envelope(0);
        Gain g = new Gain(1, gateEnv);
        gateEnv.addSegment(fadeOutMultiplier, 50);
        gateEnv.addSegment(0, 1500, new KillTrigger(g));
        g.addInput(bassWave);
        
        bassGain.addInput(g);
      }
      
      if (c.getCount() % 8 == 0) {

        float note = arpSequence.get(seqStep) * octave;
        saw.setFrequency(note);
        
        seqStep++;
        if (seqStep >= sequenceLength) seqStep = 0;

        
        // Filter envelope
        Envelope filterEnv = new Envelope(ac, 10000);
        filterEnv.setValue(filterFrequency * fadeOutMultiplier);
        filterEnv.addSegment((filterFrequency - 3000) * fadeOutMultiplier, noteLength);
        lpf.setFrequency(filterEnv).setQ(0.2f);


        // Gate
        Envelope gateEnv = new Envelope(0.2f);
        Gain g = new Gain(1, gateEnv);
        gateEnv.addSegment(0, noteLength, new KillTrigger(g));
        g.addInput(lpf);
        
        arpGain.addInput(g);
        
      }
    }
  }
  );


  ac.out.addDependent(clock);
  ac.start();
}

public void generateNewMelody(ArrayList<Float> sequence, int l) {
  
  sequence.clear();

  for (int i = 0; i < l; i++) {
    float pitch = Pitch.forceToScale((int)random(12), Pitch.pentatonic);
    float freq = Pitch.mtof(pitch + 2 * 12 + 32);
    sequence.add(freq);
  }
  
}

public void updateAudio() {
  if (state == State.Play) {
    float clampedAirTemp = min(max(airTemp.currentValue * temperatureIntensity, airTemp.minValue), airTemp.maxValue); 
    filterFrequency =     (int)map(clampedAirTemp, airTemp.minValue, airTemp.maxValue, 3000, 15000);
    
    float clampedHumidity = min(max(humidity.currentValue * humidityIntensity, humidity.minValue), humidity.maxValue);
    noteLength =          (int)map(clampedHumidity, humidity.minValue, humidity.maxValue, 10, 600); 
  } else if (state == State.FadeOut) {
    fadeOutMultiplier *= 0.98f;
  }
}

SoundFile inSound;
SoundFile outSound;

public void initialiseSoundFiles () {
  inSound = new SoundFile(this, "personin.wav");
  outSound = new SoundFile(this, "personout.wav");
}

public void playInSound () {
  inSound.play();
}

public void playOutSound () {
  outSound.play();
}

public void addMonthButton() {
  incrementDate(0, 1, 0);
}

public void subMonthButton() {
  incrementDate(0, -1, 0);
}
public void addDayButton() {
  incrementDate(0, 0, 1);
}
public void subDayButton() {
  incrementDate(0, 0, -1);
}
public void goButton () {
  play();
}

public void arpGainSlider (float val) {
  arpGain.setGain((float)val / 100);
}

public void bassGainSlider (float val) {
  bassGain.setGain((float)val / 100);
}

public void arpIntensitySlider (float val) {
  temperatureIntensity = val;
}

public void humidityIntensitySlider (float val) {
  humidityIntensity = val;
}
// Declan

// Class to fit tables into a standard format, allowing a value from 0-1440 (minutes) to represent the current frame.

class DataStore {

  Table table;             // A standard table to store the data set
  String name;             // The name of this data set
  
  float[] values;          // A float array storing the values of the table
  
  
  int[] valueChanges;      // An array which stores the frames where there is a new value
  int nextValueChange = 0; // The index of valueChange that we are up to (i.e. that has the soonest frame where there is a change)
  
  float currentValue;      // The value as of the last valueChange
  float delta;             // The change in value since last valueChange
  float minValue;          // The larest value in the set
  float maxValue;          // The smallest value in the set
  
  boolean finished = false; // Has the last value been reached?
  boolean changedThisFrame = false; // Was there a value change this frame?

  DataStore (String tableUrl, String name) {

    table = loadTable(tableUrl, "csv");
    
    if (table == null) {
      failLoading = true;
      return;
    }
    
    values = new float[table.getRowCount()];
    valueChanges = new int[table.getRowCount()];
    
    float currentMin = MAX_FLOAT;
    float currentMax = MIN_FLOAT;
    
    
    for (int i = 0; i < valueChanges.length; i++) {
      
      values[i] = table.getFloat(i, 1);
      
      if (values[i] > currentMax) currentMax = values[i];
      if (values[i] < currentMin) currentMin = values[i];
      
      //println(table.getString(i, 0));
      String timeString = table.getString(i, 0).substring(dayString.length() + 1);
      
      String[] timeNumbers = timeString.split(":");
      //println(timeString);
      valueChanges[i] = Integer.parseInt(timeNumbers[0].trim()) * 60 + Integer.parseInt(timeNumbers[1]);      
      //println(table.getString(i, 0).substring(dayString.length()) + ", assigned value " + valueChanges[i]);
      
    }
    
    minValue = currentMin;
    maxValue = currentMax;
    
    println("loaded table with " + values.length + " values and " + valueChanges.length + " value changes.");
    this.name = name;
  }
  
  // Run each frame with the frame number as i to keep the currentValue up to date. 
  public void checkFrame (int i) {
    if (finished) {changedThisFrame = false; return;}
    if (valueChanges[nextValueChange] <= i) {
      //println("Value change on " + name + " at " + i);
      changedThisFrame = true;
      delta = values[nextValueChange] - currentValue;
      currentValue = values[nextValueChange];
      nextValueChange++;
      if (nextValueChange >= valueChanges.length) finished = true;
    } else {
      changedThisFrame = false;
    }
  }
  
}

ArrayList<Person> People = new ArrayList<Person>();
ArrayList<Person> PeopleToRemove = new ArrayList<Person>();

final int peopleBuffer = 5;
final int speedMult = 2;
int peopleInTimer = 0;
int peopleInQueued = 0;
int peopleOutTimer = 0;
int peopleOutQueued = 0;

class Person {
  int xPos = 0;
  boolean leaving = false;
  
  Person (boolean leaving) {
    this.leaving = leaving;
    if (leaving) {
      xPos = width / 2;
      playOutSound();
    } else
      xPos = -100;
  }

  public void updatePerson () {
    if (!leaving) { 
      if (xPos>width-460) {
        xPos=xPos+2 * speedMult;
      } else {
        xPos=xPos+5 * speedMult;
      }
      if (xPos>width-250) {
        xPos=xPos+5 * speedMult;
      }
      if (xPos>width/2 - 100) {
        playInSound();
        PeopleToRemove.add(this);
      }
    } else {
      if (xPos>width-460 + width/2) {
        xPos=xPos+2 * speedMult;
      } else {
        xPos=xPos+5 * speedMult;
      }
      if (xPos>width-250 + width/2) {
        xPos=xPos+5 * speedMult;
      }
      if (xPos>width) {
        PeopleToRemove.add(this);
      }
    }
    
  image(personImage,xPos,height - 100,80,90);
  }
}

public void updatePeople() {
  
  if (peopleIn.changedThisFrame) {
    peopleInQueued = (int)peopleIn.currentValue;
    //println("People in value: " + (int)peopleIn.currentValue);
  }
  
  if (peopleInQueued > 0) {
    peopleInTimer++;
    if (peopleInTimer >= peopleBuffer) {
      peopleInTimer = 0;
      peopleInQueued--;
      People.add(new Person(false));
    }
  }
  if (peopleOut.changedThisFrame) {
    peopleOutQueued = (int)peopleIn.currentValue;
  }
  
  if (peopleOutQueued > 0) {
    peopleOutTimer++;
    if (peopleOutTimer >= peopleBuffer) {
      peopleOutTimer = 0;
      peopleOutQueued--;
      People.add(new Person(true));
    }
  }
  
  PeopleToRemove.clear();
  
  for (Person p : People) {
    p.updatePerson();
  }
  
  for (Person p : PeopleToRemove) {
    People.remove(p);
  }
  
}
// Min - Rain
// Algortihm Reference:  https://www.youtube.com/watch?v=KkyIDI6rQJI

int maxRainDrops = 250;
int rainSpeed = 25;
int heavyRainValue = 25;
Rain[] rain_drop = new Rain[maxRainDrops];

class Rain{
  float rain_x = random(width);
  float rain_y = random(-500, height);
  float rain_z = random(0,20);
  float rain_len = map (rain_z, 0, 20, 10, 20);
  float rain_speed= map(rain_z, 0, 20, 7, rainSpeed); // Faster as the rain drop is closer
  float rain_acceleration = 0.1f;
  
  boolean visible = true;
  
  Rain () {
    updateVisibility();
  }
  
  public void fall()
  {
    rain_y = rain_y + rain_speed;
    rain_speed = rain_speed + rain_acceleration;
    if (rain_y > height){
      rain_y = random(-200,-10);
      rain_speed = map(rain_z, 0, 20, 4, 10);
      updateVisibility();
    }
  }
  public void show(){
    if (!visible) return;
    float thick_by_z = map(rain_z, 0, 20, 1, 3); // Thicker as the rain drop is closer
    strokeWeight(thick_by_z);
    stroke(255,255,255);
    line(rain_x,rain_y,rain_x, rain_y + rain_len);
    noStroke();
  }
  
  public void updateVisibility () {
    visible = random(0, 1) < min(map(rainAmount.delta, 0, heavyRainValue, 0, maxRainDrops-1), maxRainDrops-1);
  }
}

public void initialiseRain () {
  for( int i = 0; i < rain_drop.length; i++){
    rain_drop[i] = new Rain();  
  }
}

public void updateRain () {
  for(int i = 0; i < maxRainDrops; i++){
    rain_drop[i].fall();
    rain_drop[i].show();
  }
}
// Min & Paul

int r = 0;
int g = 0;
int b = 0;
int light = 0;

int morningTime = 360;
int dayTime = 60 * 10;
int afternoonTime = 1080;
int nightTime = 1440;


public void makeTimeColor(int timeval)
{
  if(timeval < morningTime)
  {
    r = 0;
    g = 0;
    b = PApplet.parseInt((float)timeval / morningTime * 255);
    //light = 255 - (int((float)timeval / morningTime * 255 * 0.5));
  }
  else if(timeval >= morningTime && timeval < dayTime)
  {
    r = 0;
    g = PApplet.parseInt(150.0f / (dayTime-morningTime) * (timeval - morningTime));
    b = 255;
    //light = 255 - (int((float)timeval / morningTime * 255 * 0.5));
  }
  else if(timeval >= dayTime && timeval < afternoonTime)
  {
    r = PApplet.parseInt(255.0f/(afternoonTime-dayTime) * (timeval - dayTime));
    g = 150;
    b = 255 - PApplet.parseInt((255.0f - 90.0f)  /(afternoonTime - dayTime) * (timeval - dayTime));
  }
  else
  {
    r = 255 - PApplet.parseInt(255.0f / (nightTime-afternoonTime) * (timeval - afternoonTime));
    g = 150 - PApplet.parseInt(150.0f / (nightTime-afternoonTime) * (timeval - afternoonTime));
    b = 90 - PApplet.parseInt(90.0f / (nightTime-afternoonTime) * (timeval - afternoonTime));
    //light = int(255.0 / (nightTime-afternoonTime) * (timeval - afternoonTime));
  }
  
  if (timeval > 60 * 17) {
    light = (int)min(map(timeval, 60 * 17, 60 * 18, 0, 255), 255);
  } else {
    light = 0;
  }
}
  public void settings() {  size(1280, 720); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Building11AudioVisualisation" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
