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
  float rain_acceleration = 0.1;
  
  boolean visible = true;
  
  Rain () {
    updateVisibility();
  }
  
  void fall()
  {
    rain_y = rain_y + rain_speed;
    rain_speed = rain_speed + rain_acceleration;
    if (rain_y > height){
      rain_y = random(-200,-10);
      rain_speed = map(rain_z, 0, 20, 4, 10);
      updateVisibility();
    }
  }
  void show(){
    if (!visible) return;
    float thick_by_z = map(rain_z, 0, 20, 1, 3); // Thicker as the rain drop is closer
    strokeWeight(thick_by_z);
    stroke(255,255,255);
    line(rain_x,rain_y,rain_x, rain_y + rain_len);
    noStroke();
  }
  
  void updateVisibility () {
    visible = random(0, 1) < min(map(rainAmount.delta, 0, heavyRainValue, 0, maxRainDrops-1), maxRainDrops-1);
  }
}

void initialiseRain () {
  for( int i = 0; i < rain_drop.length; i++){
    rain_drop[i] = new Rain();  
  }
}

void updateRain () {
  for(int i = 0; i < maxRainDrops; i++){
    rain_drop[i].fall();
    rain_drop[i].show();
  }
}
