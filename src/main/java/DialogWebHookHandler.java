import Models.NewsArticlesModel;
import Models.NewsResponseModel;
import Utils.Constant;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DialogWebHookHandler implements RequestStreamHandler
{
    private JSONParser jsonParser = new JSONParser();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String jsonResponseString;

        String fallbackMessage = "Oops.. there was some server or internal problem, don't worry please say name again.";

        String nameFallback = "I could not find any news about ";

        try
        {
            JSONObject jsonRequestObject = (JSONObject) jsonParser.parse(bufferedReader);

            if (jsonRequestObject.get(Constant.queryResult) != null)
            {
                JSONObject jsonQueryResult = (JSONObject) jsonRequestObject.get(Constant.queryResult);

                if (jsonQueryResult.get(Constant.intent) != null)
                {
                    JSONObject jsonIntent = (JSONObject) jsonQueryResult.get(Constant.intent);

                    if (jsonIntent.get(Constant.displayName) != null)
                    {
                        String jsonDisplayName = (String) jsonIntent.get(Constant.displayName);

                        switch (jsonDisplayName)
                        {
                            case "GetNewsName" :

                                if (jsonQueryResult.get(Constant.parameters) != null)
                                {
                                    JSONObject jsonParameters = (JSONObject) jsonQueryResult.get(Constant.parameters);

                                    if (jsonParameters.get(Constant.name) != null)
                                    {
                                        String newsName = (String) jsonParameters.get(Constant.name);

                                        if (newsName != null && !newsName.equals("") && !newsName.equals("null"))
                                        {
                                            String newsResponse = getResponseForName(newsName);

                                            NewsResponseModel newsResponseModel = new Gson().fromJson(newsResponse,NewsResponseModel.class);

                                            if (newsResponseModel != null)
                                            {
                                                if (newsResponseModel.getStatus().equals("ok"))
                                                {
                                                    List<NewsArticlesModel> newsArticlesModelList = newsResponseModel.getArticles();

                                                    if (newsArticlesModelList != null && newsArticlesModelList.size() > 0)
                                                    {
                                                        StringBuilder stringBuilder = new StringBuilder();

                                                        for(int i=0; i<newsArticlesModelList.size(); i++)
                                                        {
                                                            if (i == newsArticlesModelList.size() - 1)
                                                            {
                                                                stringBuilder.append(String.valueOf(newsArticlesModelList.size())).append(". ");
                                                            }
                                                            else
                                                            {
                                                                stringBuilder.append(String.valueOf(i + 1)).append(". ");
                                                            }

                                                            NewsArticlesModel model = newsArticlesModelList.get(i);

                                                            if (model.getAuthor() != null)
                                                            {
                                                                stringBuilder.append("Author : ").append(model.getAuthor()).append("\n");
                                                            }

                                                            if (model.getTitle() != null)
                                                            {
                                                                stringBuilder.append("Title : ").append(model.getTitle()).append("\n");
                                                            }

                                                            if (model.getDescription() != null)
                                                            {
                                                                stringBuilder.append("Description : ").append(model.getDescription()).append("\n");
                                                            }

                                                            if (model.getUrl() != null)
                                                            {
                                                                stringBuilder.append("Official Site : ").append(model.getUrl()).append("\n");
                                                            }
                                                        }

                                                        String finalNews = "News about " + newsName + " : " + stringBuilder.toString() + ". If you want to get another news, simply say \" 'your news topic' \"";

                                                        JSONObject jsonObject = getTextResponseForName(finalNews);

                                                        jsonResponseString = jsonObject.toJSONString();
                                                    }
                                                    else
                                                    {
                                                        JSONObject jsonObject = getTextResponseForName(nameFallback + "\"" + newsName + "\"");

                                                        jsonResponseString = jsonObject.toJSONString();
                                                    }
                                                }
                                                else
                                                {
                                                    JSONObject jsonObject = getTextResponseForName(nameFallback + "\"" + newsName + "\"");

                                                    jsonResponseString = jsonObject.toJSONString();
                                                }
                                            }
                                            else
                                            {
                                                JSONObject jsonObject = getTextResponseForName(nameFallback + "\"" + newsName + "\"");

                                                jsonResponseString = jsonObject.toJSONString();
                                            }
                                        }
                                        else
                                        {
                                            JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                                            jsonResponseString = jsonObject.toJSONString();
                                        }
                                    }
                                    else
                                    {
                                        JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                                        jsonResponseString = jsonObject.toJSONString();
                                    }
                                }
                                else
                                {
                                    JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                                    jsonResponseString = jsonObject.toJSONString();
                                }

                                break;

                            case "Default Welcome Intent":

                                String welcomeResponse = "Hi, welcome to news trey. It's a pleasure to talk to you. " +
                                        "If you want news about any topic, i can give news about that topic. If you want more instructions or help simple say 'help' " +
                                        "Ok, now you can start to say any topic ";

                                JSONObject jsonObject = getTextResponseForName(welcomeResponse);

                                jsonResponseString = jsonObject.toJSONString();

                                break;

                            case "Default Help Intent" :

                                String helpResponse = "It pleasure to help you. \n" +
                                        "If you have any doubts or you don't know how to ask to 'News Trey', don't worry. \n" +
                                        "I clarify your doubts. If you tell a news topic, i can give the news about that topic." +
                                        "Ok, now you can start to say any topic ";

                                JSONObject helpObject = getTextResponseForName(helpResponse);

                                jsonResponseString = helpObject.toJSONString();

                                break;

                            default:
                                JSONObject defaultObject = getTextResponseForName(fallbackMessage);

                                jsonResponseString = defaultObject.toJSONString();

                                break;
                        }
                    }
                    else
                    {
                        JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                        jsonResponseString = jsonObject.toJSONString();
                    }
                }
                else
                {
                    JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                    jsonResponseString = jsonObject.toJSONString();
                }
            }
            else
            {
                JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                jsonResponseString = jsonObject.toJSONString();
            }
        }
        catch (ParseException e)
        {
            JSONObject jsonObject = getTextResponseForName(fallbackMessage);

            jsonResponseString = jsonObject.toJSONString();
        }

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        outputStreamWriter.write(jsonResponseString);
        outputStreamWriter.close();
    }

    private String getResponseForName(String newsName)
    {
        try
        {
            String apiKey = "280d1a7b22af44beb5800dd37ed0b874";

            URL urlDetail = new URL("https://newsapi.org/v2/everything?q=" + newsName + "&apiKey=" + apiKey + "&language=en");

            HttpsURLConnection connection = (HttpsURLConnection) urlDetail.openConnection();

            connection.setDoOutput(true);

            connection.setRequestMethod("GET");

            connection.setRequestProperty("Content-Type", "application/json");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            StringBuilder resultBuilder = new StringBuilder();

            String jsonOutput;

            while ((jsonOutput = bufferedReader.readLine()) != null)
            {
                resultBuilder.append(jsonOutput);
            }

            return resultBuilder.toString();
        }
        catch (IOException e)
        {
            return "I could not find any news about \"" + newsName + "\"";
        }
    }

    private JSONObject getTextResponseForName(String speechText)
    {
        JSONObject responseForName = new JSONObject();

        responseForName.put(Constant.fulfillmentText,speechText);

        JSONObject fulFillMessageObject = new JSONObject();

        JSONArray fulFillMessageArray = new JSONArray();

        JSONObject textResponseObject = new JSONObject();

        JSONArray textResponseArray = new JSONArray();

        textResponseArray.add(speechText);

        textResponseObject.put(Constant.text,textResponseArray);

        fulFillMessageObject.put(Constant.text,textResponseObject);

        fulFillMessageArray.add(fulFillMessageObject);

        responseForName.put(Constant.fulfillmentMessages,fulFillMessageArray);

        responseForName.put("source","");
        responseForName.put("payload",null);
        responseForName.put("outputContexts",null);
        responseForName.put("followupEventInput",null);

        return responseForName;
    }
}
