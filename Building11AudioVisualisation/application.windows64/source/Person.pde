
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

  void updatePerson () {
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

void updatePeople() {
  
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
