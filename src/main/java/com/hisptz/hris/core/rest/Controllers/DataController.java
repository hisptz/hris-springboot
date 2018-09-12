package com.hisptz.hris.core.rest.Controllers;

import com.hisptz.hris.core.rest.ErrorHandling.Error;
import com.hisptz.hris.core.rest.ErrorHandling.HttpStatus;
import com.hisptz.hris.core.rest.ErrorHandling.HttpStatusCode;
import com.hisptz.hris.core.rest.ErrorHandling.Status;
import com.hisptz.hris.core.rest.QueryStructure.ApiQuery;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Guest on 8/29/18.
 */
@Component
@RestController
@RequestMapping("/api")
public class DataController {
    private ApiQuery query;
    private List<Map<String, String>> results;
    private Page<Map<String, String>> thisPage;
    private Pageable pageRequest;
    private List<Map<String, String>> errors = new ArrayList<>();
    private Error error;
    private int start;
    private int end;

    @GetMapping("{model}.json")
    public Page<Map<String, String>> get(@PathVariable("model") String model, @RequestParam(required = false) String fields, @RequestParam(required = false) String filters, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page){
        query = createQuery(model,fields,filters);
        results = perfomQuery(query.toString(), query);

        if (errors != null && errors.containsAll(results)){
            List<Map<String, String>> temp = new ArrayList<>();
            temp.addAll(errors);
            errors.clear();
            return new PageImpl<>(temp);
        }

        if (size == null || size <= 0 || size > results.size()){
            size = results.size() ;
        }

        if (page == null){
            page = 0;
        }

        if (results.size() == 0){
            size = 2;
        }
        pageRequest = createPageRequest(page,size, sort);

        start = size*(page + 1) - size;
        end = size*(page+1) < results.size() ? size*(page+1) : results.size();

        // sort the data here
        thisPage = new PageImpl<>(results.subList(start, end), pageRequest, results.size());

        return thisPage;
    }

    private ApiQuery createQuery(String model, String fields, String filters){
        ApiQuery query = new ApiQuery(model,filters);
        List<String> fieldsList  = new ArrayList<>();
        String[] myfields;

        if (fields != null) {
            myfields = fields.split(",");

            for (String field : myfields) {
                fieldsList.add(field);
            }
        }

        query.setFields(fieldsList);

        return query;
    }

    private  Pageable createPageRequest(int page, int size, String sort) {
        pageRequest = new PageRequest(page, size, Sort.Direction.DESC, "id");
        return pageRequest;
    }

    public List<Map<String, String>> perfomQuery(String graphqlQuery, ApiQuery query){
        JSONObject myResponse = new JSONObject();
        String query_url = "http://localhost:8080/graphql";
        List<String> fields = query.getFields();
        List<Map<String, String>> lists = new ArrayList<>();


        try {
            myResponse = connectToResourse(graphqlQuery, query_url);


            JSONArray mainData = myResponse.getJSONObject("data").getJSONArray(query.getModel());

            List<JSONObject> objs = new ArrayList<>();
            for (int i = 0; i < mainData.length(); i++) {
                if (mainData.getJSONObject(i) != null)
                    objs.add(mainData.getJSONObject(i));
            }


            for (JSONObject jsonList: objs) {
                Map<String, String> eachList = new HashMap<>();
                for (String field: fields){
                    eachList.put(field, jsonList.getString(field));
                }
                lists.add(eachList);
            }

            //return myResponse;
            //return data;
            return lists;
        } catch (Exception e){
            error = new Error(HttpStatus.ERROR, "The model "+ query.getModel() +" doesn't exist in the schema. Please check the schema definition", HttpStatusCode.HTTP_STATUS_CODE_403, Status.ERROR);
            errors.add(error.getErrorMap());
        }
        //return myResponse;
        //return data;
        return lists;
    }

    public JSONObject connectToResourse(String graphqlQuery, String query_url){
        String result = "";
        JSONObject object = new JSONObject();
        try {
            URL url = new URL(query_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(graphqlQuery.getBytes("UTF-8"));
            os.close();


            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            result = IOUtils.toString(in, "UTF-8");
            object = new JSONObject(result);
        } catch (Exception e){
            error = new Error(HttpStatus.ERROR, e.getLocalizedMessage(), HttpStatusCode.HTTP_STATUS_CODE_403, Status.ERROR);
            errors.add(error.getErrorMap());
        }
        return object;
    }
    
    @PostMapping("{model}.json")
    public Page<Map<String, String>> create(@PathVariable("model") String model, @RequestParam(required = false) String fields, @RequestParam(required = false) String filters, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page){

        return new PageImpl<Map<String, String>>(new ArrayList<>());
    }

    @PutMapping("{model}.json")
    public Page<Map<String, String>> update(@PathVariable("model") String model, @RequestParam(required = false) String fields, @RequestParam(required = false) String filters, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page){

        return new PageImpl<Map<String, String>>(new ArrayList<>());
    }
    // Page performQuery(String query, String requestType)
    // localhost:8080/api/users.json?fields=id,name&filters=name:eq:Vincent;AND;id:in:[wyte,wyeiw]
    // curl --header "Content-Type:application/json" --request POST --data '{"query":"{Users{id}}"}' http://localhost:8080/graphql

    //TODO: Send a dynamic error according to the result of the graphql call
    // TODO: Implement mutations creating and updating a model
}
