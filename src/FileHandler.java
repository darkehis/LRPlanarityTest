import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class FileHandler
{
	
	public static void writeJson(JSONObject object,String filename)
	{
        try (FileWriter file = new FileWriter("./graphs/" + filename + ".json"))
        {

            file.write(object.toString());
            file.flush();

        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
	}
	
	public static JSONObject readJson(String filename)
	{
		String graphString = readFile(filename);
		JSONObject object = null;
		try
		{
			object = new JSONObject(graphString);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return object;
		
	}
	
	public static String readFile(String filename) 
	{
		String result = "";
		String directory = "./graphs/";
		String extension = ".json";
		try {
			BufferedReader br = new BufferedReader(new FileReader(directory + filename + extension));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
