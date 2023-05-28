// Min & Paul

int r = 0;
int g = 0;
int b = 0;
int light = 0;

int morningTime = 360;
int dayTime = 60 * 10;
int afternoonTime = 1080;
int nightTime = 1440;


void makeTimeColor(int timeval)
{
  if(timeval < morningTime)
  {
    r = 0;
    g = 0;
    b = int((float)timeval / morningTime * 255);
    //light = 255 - (int((float)timeval / morningTime * 255 * 0.5));
  }
  else if(timeval >= morningTime && timeval < dayTime)
  {
    r = 0;
    g = int(150.0 / (dayTime-morningTime) * (timeval - morningTime));
    b = 255;
    //light = 255 - (int((float)timeval / morningTime * 255 * 0.5));
  }
  else if(timeval >= dayTime && timeval < afternoonTime)
  {
    r = int(255.0/(afternoonTime-dayTime) * (timeval - dayTime));
    g = 150;
    b = 255 - int((255.0 - 90.0)  /(afternoonTime - dayTime) * (timeval - dayTime));
  }
  else
  {
    r = 255 - int(255.0 / (nightTime-afternoonTime) * (timeval - afternoonTime));
    g = 150 - int(150.0 / (nightTime-afternoonTime) * (timeval - afternoonTime));
    b = 90 - int(90.0 / (nightTime-afternoonTime) * (timeval - afternoonTime));
    //light = int(255.0 / (nightTime-afternoonTime) * (timeval - afternoonTime));
  }
  
  if (timeval > 60 * 17) {
    light = (int)min(map(timeval, 60 * 17, 60 * 18, 0, 255), 255);
  } else {
    light = 0;
  }
}
