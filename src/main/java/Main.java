// import necessary java libraries
import java.io.*;
import java.util.*;

//import CSVReader and dependencies
import com.opencsv.CSVReader;
import java.nio.charset.StandardCharsets;

// import JSONObject
import org.json.*;

class Main {
  public static void main(String[] args) throws Exception {
    String file = "data/ml-latest-small/movies.csv";
    /*
      genre: [full data count, last 5 years count] 
    */
    HashMap<String, Integer[]> genreCount = new HashMap<>();
    /*
      year: [genres]
    */
    // HashMap<Integer, ArrayList<String>> genreYearCount = new HashMap<>();
    HashMap<Integer, ArrayList<String>> genreYearCount = new HashMap<>();
    try (FileInputStream fis = new FileInputStream(file);
    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
    CSVReader reader = new CSVReader(isr)) {
      String[] nextLine; // store line for processing
      
      reader.readNext(); // skip first line

      // read in line from file
      while ((nextLine = reader.readNext()) != null) {
        String[] elements = nextLine[2].split("\\|"); // remove | from genres and store in array

        List<String> elementsList = Arrays.asList(elements);
        ArrayList<String> genres = new ArrayList<String>(elementsList);
        int year;

        // parse year
        if (nextLine[1].endsWith(")")) {
          year = Integer.parseInt(nextLine[1].substring(nextLine[1].length() - 5, nextLine[1].length() - 1));
        } else if (nextLine[1].endsWith(") ")) {
          year = Integer.parseInt(nextLine[1].substring(nextLine[1].length() - 6, nextLine[1].length() - 2));
        } else {
          year = 0;
        }

        // get count for each genre
        for (String g : genres) {
          if (!genreCount.containsKey(g)) {
            if (2020 - year <= 5 ){
              Integer[] r = {1,1};
              genreCount.put(g, r);
            } else {
              Integer[] r = {1,0};
              genreCount.put(g, r);
            }
          } else {
            if (2020 - year <= 5 ){
              Integer[] count = genreCount.get(g);
              Integer[] r = {count[0] + 1, count[1] + 1};
              genreCount.put(g, r);
            } else {
              Integer[] count = genreCount.get(g);
              Integer[] r = {count[0] + 1, count[1]};
              genreCount.put(g, r);
            }
          }

          // get genre count by year
          if (!genreYearCount.containsKey(year)) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(g);
            genreYearCount.put(year, temp);
          } else {
            ArrayList<String> temp = genreYearCount.get(year);
            temp.add(g);
            genreYearCount.put(year, temp);
          }
        }
      }
    }

    System.out.println("\nPART 1 - Genre Count (genre, full data count, last 5 year count\n"); 
    ArrayList<String> genres = new ArrayList<>();
    JSONObject jo = new JSONObject();

    for (String gen : genreCount.keySet()) {
      String key = gen.toString();
      genres.add(key);
      Integer[] value = genreCount.get(gen);
      Map m = new LinkedHashMap();
      // add values to map
      m.put("complete", value[0]);
      m.put("last 5 years", value[1]);

      jo.put(gen, m); // add map to json object with genre as key
    }
    // print output
    System.out.println(jo.toString(2));
    
    System.out.println("\n---\nPART 2 - Number of movies in each genre per year\n");
    JSONObject json = new JSONObject();
    // generate json object
    for (Integer yr : genreYearCount.keySet()) {
      ArrayList dt = genreYearCount.get(yr);
      Map m = new LinkedHashMap();
      for (String g : genres) {
        m.put(g, Collections.frequency(dt, g)); // generate count for each genre and add to map
      }
      json.put(yr.toString(), m); // add count of genres to each year
    }

    // print output
    System.out.println(json.toString(2));

    // write output to file
    PrintWriter p = new PrintWriter("output/genreCount.json");
    PrintWriter pw = new PrintWriter("output/genreYearCount.json");
    p.write(jo.toString(2));
    pw.write(json.toString(2));

    // close printwriter
    p.flush();
    pw.flush();
    p.close();
    pw.close();
  }
}