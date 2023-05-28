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
  void checkFrame (int i) {
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
