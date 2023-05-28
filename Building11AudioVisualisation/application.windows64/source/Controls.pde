
void addMonthButton() {
  incrementDate(0, 1, 0);
}

void subMonthButton() {
  incrementDate(0, -1, 0);
}
void addDayButton() {
  incrementDate(0, 0, 1);
}
void subDayButton() {
  incrementDate(0, 0, -1);
}
void goButton () {
  play();
}

void arpGainSlider (float val) {
  arpGain.setGain((float)val / 100);
}

void bassGainSlider (float val) {
  bassGain.setGain((float)val / 100);
}

void arpIntensitySlider (float val) {
  temperatureIntensity = val;
}

void humidityIntensitySlider (float val) {
  humidityIntensity = val;
}
