package br.com.codenation.codenation;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.regex.Pattern;

import static jdk.nashorn.internal.objects.NativeString.toLowerCase;

@Controller
public class Application {
    @GetMapping
    @ResponseBody
    public void application() throws IOException, JSONException, NoSuchAlgorithmException {
        codenationFunction();
    }

    public void codenationFunction() throws JSONException, NoSuchAlgorithmException {
        RestTemplate restTemplate = new RestTemplate();
        String data = restTemplate.getForObject("https://api.codenation.dev/v1/challenge/dev-ps/generate-data?token=5486c70b4ffd77726e36bac5c2ce3b98b9607cca", String.class);
        JSONObject json = new JSONObject(String.valueOf(data));
        String decrypt = decrypt(json.getString("cifrado"),Integer.parseInt(json.getString("numero_casas")));
        json.put("decifrado", decrypt);

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(decrypt.getBytes());
        byte[] digest = sha1.digest();

        json.put("resumo_criptografico", String.format("%1$040x", new BigInteger(1, digest)));

        try{
            FileWriter file = new FileWriter("answer.json");
            file.write(json.toString());
            file.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
       send();
    }

    public void send(){
        try  {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://api.codenation.dev/v1/challenge/dev-ps/submit-solution?token=5486c70b4ffd77726e36bac5c2ce3b98b9607cca")
                    .queryString("token", "5486c70b4ffd77726e36bac5c2ce3b98b9607cca")
                    .field("answer", new File("answer.json"))
                    .asJson();
            System.out.println(jsonResponse.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String decrypt(String phrase, int number){
        String nfdNormalizedString = Normalizer.normalize(phrase, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        phrase = toLowerCase(pattern.matcher(nfdNormalizedString).replaceAll(""));
        StringBuilder phaseCripto = new StringBuilder();
        int temp = 0;

        for(int x = 0; x < phrase.length(); x++){
            if(phrase.charAt(x) >= 97 && phrase.charAt(x) <= 122){
                temp = (int)phrase.charAt(x) - number;
                if(temp < 97){
                    temp = (123 - 97 % temp);
                }
            }else{
                temp = (int)phrase.charAt(x);
            }
            phaseCripto.append((char)temp);
        }
        System.out.println("Frase decriptografada: " + phaseCripto);
        return phaseCripto.toString();
    }

}
