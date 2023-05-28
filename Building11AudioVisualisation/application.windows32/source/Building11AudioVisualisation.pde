import java.text.DecimalFormat;

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
color clrBackground = 0;
color clrBackgroundAccent = 20;
color clrForeground = 255;

// Controls
import controlP5.*;
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

void setup() {

  size(1280, 720);
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
void draw() {

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

void play() {
  drawDialogueMessage("Loading " + dayString);
  lengthMultiplier = map(lengthSlider.getValue(), 48, 192, 1.0, 0.25);
  lengthSlider.hide();
  addDay.hide();
  subDay.hide();
  addMonth.hide();
  subMonth.hide();
  goButton.hide();
  state = State.Loading;
}

void keyPressed() {
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

void drawSetup () {
  textAlign(CENTER, CENTER);
  textSize(32);
  text(dayString, width/2, height/2);
  textAlign(LEFT);
}

void drawUI() {
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

void drawDialogueMessage (String s) {
  fill(clrBackgroundAccent);
  rect(width - 400, height - 50, 400, 50);

  fill(clrForeground);
  textSize(16);
  textAlign(LEFT);
  text(s, width - 380, height-25);
}

String frameToTimeString(int i) {
  int h = i / 60;
  int m = i - h * 60;
  return String.format("%02d", h) + ":" + String.format("%02d", m);
}


final String[] monthWords = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
String dayStringToWrittenDate(String s) {
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

void incrementDate (int y, int m, int d) {
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
