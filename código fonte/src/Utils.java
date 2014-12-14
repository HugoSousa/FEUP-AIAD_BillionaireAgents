import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Utils {
	
	@SuppressWarnings("unchecked")
	public static String JSONEncode(HashMap<String, Object> elements){
		JSONObject obj = new JSONObject();
		
		for (Entry<String, Object> entry : elements.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    obj.put(key, value);
		}
	    
	    StringWriter out = new StringWriter();
	    try {
			obj.writeJSONString(out);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    
	    return out.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> JSONDecode(String content){
		JSONParser parser=new JSONParser();
		HashMap<String, Object> obj = null;
		try {
			obj = (HashMap<String, Object>) parser.parse(content);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
}
