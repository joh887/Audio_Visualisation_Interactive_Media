import beads.*;
import processing.sound.*;

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

void initialiseAudio() {
  
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
        lpf.setFrequency(filterEnv).setQ(0.2);


        // Gate
        Envelope gateEnv = new Envelope(0.2);
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

void generateNewMelody(ArrayList<Float> sequence, int l) {
  
  sequence.clear();

  for (int i = 0; i < l; i++) {
    float pitch = Pitch.forceToScale((int)random(12), Pitch.pentatonic);
    float freq = Pitch.mtof(pitch + 2 * 12 + 32);
    sequence.add(freq);
  }
  
}

void updateAudio() {
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

void initialiseSoundFiles () {
  inSound = new SoundFile(this, "personin.wav");
  outSound = new SoundFile(this, "personout.wav");
}

void playInSound () {
  inSound.play();
}

void playOutSound () {
  outSound.play();
}
