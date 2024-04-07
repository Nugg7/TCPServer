import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

import java.io.FileWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
public class JSONManagerServer {
    static JSONObject doc;
    JSONParser jsonParser = new JSONParser();

    private String username;
    private UUID uuid;

    public JSONManagerServer(){
        try (FileReader reader = new FileReader("Products.json"))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            doc = (JSONObject) obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public JSONObject parse(String s) throws ParseException {
        JSONObject json = (JSONObject) jsonParser.parse(s);
        return json;
    }

    public void putProducts(JSONObject prod){
        JSONArray products = (JSONArray) doc.get("products");
        products.add(prod);
        write();
    }

    private void write(){
        try (FileWriter file = new FileWriter("Products.json")) {
            //We can write any JSONArray or JSONObject instance to the file
            file.write(doc.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clearProducts(){
        JSONArray products = (JSONArray) doc.get("products");
        products.clear();
        write();
    }
}
